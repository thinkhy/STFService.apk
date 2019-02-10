package jp.co.cyberagent.stf;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.co.cyberagent.stf.api.APIClient;
import jp.co.cyberagent.stf.io.FileHelper;
import jp.co.cyberagent.stf.query.GetRootStatusResponder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static jp.co.cyberagent.stf.io.FileHelper.fileName;
import static jp.co.cyberagent.stf.io.FileHelper.path;

public class IdentityActivity extends Activity {
    private static final String TAG = "IdentityActivity";

    public static final String ACTION_IDENTITY = "jp.co.cyberagent.stf.ACTION_IDENTIFY";

    public static final String EXTRA_SERIAL = "serial";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        LinearLayout layout = new LinearLayout(this);
        layout.setKeepScreenOn(true);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        layout.setBackgroundColor(Color.RED);
        layout.setPadding(16, 16, 16, 16);
        layout.setGravity(Gravity.CENTER);

        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        String serial = intent.getStringExtra(EXTRA_SERIAL);

        if (serial == null) {
            serial = getProperty("ro.serialno", "unknown");
        }

        layout.addView(createLabel("SERIAL"));
        layout.addView(createData(serial));
        layout.addView(createLabel("MODEL"));
        layout.addView(createData(getProperty("ro.product.model", "unknown")));
        layout.addView(createLabel("VERSION"));
        layout.addView(createData(Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")"));
        layout.addView(createLabel("OPERATOR"));
        layout.addView(createData(tm.getSimOperatorName()));
        layout.addView(createLabel("PHONE"));
        layout.addView(createData(tm.getLine1Number()));
        layout.addView(createLabel("IMEI"));
        layout.addView(createData(tm.getDeviceId()));
        layout.addView(createLabel("ICCID"));
        layout.addView(createData(tm.getSimSerialNumber()));

        layout.addView(createLabel("Rooted"));
        layout.addView(createData(String.valueOf(GetRootStatusResponder.isDeviceRooted())));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ensureVisibility();
        setContentView(layout);

        removeActions();
        requestPermissions();
    }

    private View createLabel(String text) {
        TextView titleView = new TextView(this);
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextColor(Color.parseColor("#c1272d"));
        titleView.setTextSize(16f);
        titleView.setText(text);
        return titleView;
    }

    private View createData(String text) {
        TextView dataView = new TextView(this);
        dataView.setGravity(Gravity.CENTER);
        dataView.setTextColor(Color.WHITE);
        dataView.setTextSize(24f);
        dataView.setText(text);
        return dataView;
    }

    private void ensureVisibility() {
        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        unlock();

        WindowManager.LayoutParams params = window.getAttributes();
        params.screenBrightness = 1.0f;
        window.setAttributes(params);
    }

    private String getProperty(String name, String defaultValue) {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            Method get = SystemProperties.getMethod("get", String.class, String.class);
            return (String) get.invoke(SystemProperties, name, defaultValue);
        }
        catch (ClassNotFoundException e) {
            Log.e(TAG, "Class.forName() failed", e);
            return defaultValue;
        }
        catch (NoSuchMethodException e) {
            Log.e(TAG, "getMethod() failed", e);
            return defaultValue;
        }
        catch (InvocationTargetException e) {
            Log.e(TAG, "invoke() failed", e);
            return defaultValue;
        }
        catch (IllegalAccessException e) {
            Log.e(TAG, "invoke() failed", e);
            return defaultValue;
        }
    }

    @SuppressWarnings("deprecation")
    private void unlock() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardManager.newKeyguardLock("InputService/Unlock").disableKeyguard();
    }

    public static class IntentBuilder {
        @Nullable private String serial;

        public IntentBuilder() {
        }

        public IntentBuilder serial(@NonNull String serial) {
            this.serial = serial;
            return this;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context.getApplicationContext(), IdentityActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (serial != null) {
                intent.putExtra(IdentityActivity.EXTRA_SERIAL, serial);
            }
            return intent;
        }
    }

    private void requestPermissions() {
        int REQUEST_PERMISSIONS = 0x01234;
        int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);
        }
    }

    private void removeActions() {
        String fileContent = FileHelper.ReadFile();
        if (fileContent.contains("starting")) {
            String[] separated = fileContent.split(" ");
            String id = separated[1];
            id = id.substring(1, id.length()-1);

            APIClient.getAPIService().removeAction(id).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    deleteLogFile();

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Deleted action successfully");
                    } else {
                        Log.d(TAG, "Request failed, Please try again!");
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    if (!call.isCanceled()) {
                        deleteLogFile();
                        Log.d(TAG, "Request failed");
                    }
                }
            });
        }
    }

    private void deleteLogFile() {
        File file = new File(path + fileName);
        if (file.exists()) {
            if (file.delete()) {
                Log.d(TAG, "file Deleted");
            } else {
                Log.d(TAG, "file not Deleted");
            }
        }
    }
}
