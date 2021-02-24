package com.example.snarkportingtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;


public class SubActivity extends AppCompatActivity {
//    ProgressDialog progressDialog;
    String loc =  "/data/data/com.example.snarkportingtest/files/" ;

//     Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    public void CopyFromPackage(int resID, String target) throws IOException
    {
        FileOutputStream lOutputStream = openFileOutput(target, Context.MODE_PRIVATE);
        InputStream lInputStream = getResources().openRawResource(resID);
        int readByte;
        byte[] buff = new byte[999999];

        while (( readByte = lInputStream.read(buff))!=-1)
        {
            lOutputStream.write(buff,0, readByte);
        }

        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }
    public void CopyIfNotExist(int resID, String target) throws IOException
    {
        File targetFile = new File(target);
        if (!targetFile.exists())
        {
            CopyFromPackage(resID,targetFile.getName());
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

//        progressDialog = ProgressDialog.show(SubActivity.this, "SNARK-CHECK...", null, true, true);

        Intent start_intent = getIntent();
        String task = (String) start_intent.getExtras().get("task");
        String mode = (String) start_intent.getExtras().get("mode");
        if(task.equals("vote")) {
            Log.d("task", task);
            try {
                String text = loc + task + "arith.txt";
                CopyIfNotExist(R.raw.votearith, text);
//            Log.d("test", "onCreate: "+text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                String text = loc + task + "in.txt";
                CopyIfNotExist(R.raw.votein, text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(task.equals("register")) {
            Log.d("task", task);
            try {
                String text = loc + task + "arith.dat";
                CopyIfNotExist(R.raw.registerarith, text);
//            Log.d("test", "onCreate: "+text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                String text = loc + task + "in.dat";
                CopyIfNotExist(R.raw.registerin, text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(task.equals("tally")) {
            Log.d("task", task);
            try {
                String text = loc + task + "arith.dat";
                CopyIfNotExist(R.raw.tallyarith, text);
//            Log.d("test", "onCreate: "+text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                String text = loc + task + "in.dat";
                CopyIfNotExist(R.raw.tallyin, text);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

//         Example of a call to a native method
//        String result = stringFromJNI(task, mode);
//        tv.setText(result);

        final String result = stringFromJNI(task, mode, loc);

//        progressDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(SubActivity.this);
        builder.setTitle("proof 확인").setMessage("Proof 확인 : "+result).setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                intent.putExtra("result", result);
                finish();
            }
        }).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI(String task, String mode, String loc);
}
