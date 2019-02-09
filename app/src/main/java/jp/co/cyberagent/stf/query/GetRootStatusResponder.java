package jp.co.cyberagent.stf.query;

import android.content.Context;

import com.google.protobuf.GeneratedMessageLite;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import jp.co.cyberagent.stf.proto.Wire;

public class GetRootStatusResponder extends AbstractResponder {
    public GetRootStatusResponder(Context context) {
        super(context);
    }

    @Override
    public GeneratedMessageLite respond(Wire.Envelope envelope) {
        boolean root_flag = isDeviceRooted();

        return Wire.Envelope.newBuilder()
            .setId(envelope.getId())
            .setType(Wire.MessageType.GET_WIFI_STATUS)
            .setMessage(Wire.GetWifiStatusResponse.newBuilder()
                .setSuccess(true)
                .setStatus(root_flag)
                .build()
                .toByteString())
            .build();
    }

    @Override
    public void cleanup() {
        // No-op
    }

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}
