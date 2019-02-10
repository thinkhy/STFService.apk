package jp.co.cyberagent.stf.io;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileHelper {
    public final static String FILE_NAME = "device_operations.log";
    public final static String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" ;
    final static String TAG = FileHelper.class.getName();

    public static  String ReadFile(){
        String line = null;

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            fileInputStream = new FileInputStream (new File(PATH + FILE_NAME));
            inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            line = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        }
        catch(IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (Exception ignore) {}
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (Exception ignore) {}
            }
        }
        return line;
    }

    public static boolean saveToFile(String data){
        FileOutputStream fileOutputStream =  null;
        try {
            new File(PATH).mkdir();
            File file = new File(PATH + FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
            String format = simpleDateFormat.format(new Date());

            fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true;
        }
        catch(FileNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        }
        catch(IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
        finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignore) {}
            }
        }
        return  false;
    }
}
