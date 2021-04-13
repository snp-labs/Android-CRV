package com.example.snarkportingtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 777;

    public static Context context_main;
    public static int standardSize_X, standardSize_Y;
    public float density;

//    public final static String ip = "192.168.219.100";    // 집
//    public final static String ip = "192.168.0.168";      // 한양대
//    public final static int port = 9999;
    public final static String ip = "222.111.165.26";  //국민대
    public final static int port = 9090;


    Button btn_login;
    Button btn_signup;
    Button btn_admin;

    TextView tv_login;

    EditText et_id;
    EditText et_pwd;

    String str;

    Toolbar toolbar;

    // 스나크 확인용
    Button btn_snark;
    private static final int SNARK_CODE = 888;

    //DB 확인용
    TextView tv_test;
    Button btn_db;
    SQLiteDatabase db;
    int i =0;
    ArrayList<Integer> vote_id_list;
    ArrayList<Votedetail> votedetails;
    private String jsonString;


    private Handler mHandler;
    private DataOutputStream dos;
    private DataInputStream dis;

    private String user_DB_update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStanderSize();
        context_main = this;

        FindViewID();   // layout view 아이디 연결
        TextSizeSet();  // text size 자동조절

        setSupportActionBar(toolbar);



        // EditText Filter 설정(ID, PW 입력창)
        et_id.setFilters(new InputFilter[]{new EngNumInputFilter(), new InputFilter.LengthFilter(16)});
        et_pwd.setFilters(new InputFilter[]{new EngNumInputFilter2(), new InputFilter.LengthFilter(16)});

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_id.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                } else if(et_pwd.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String result = new CustomTask().execute(et_id.getText().toString(), et_pwd.getText().toString(), "login").get();
                        Log.d("msg output", result);
                        if(result.equals(et_pwd.getText().toString())){
                                 //Sign in success, update UI with the signed-in user's information
                            Log.d("SIGNIN", "signInWithEmail:success");
                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this, UsermainActivity.class);
                            str = et_id.getText().toString();
                            intent.putExtra("user_id", str);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGNIN", "signInWithEmail:failure");
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e) {}
                }

            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UsersignupActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        btn_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminloginActivity.class);
                startActivity(intent);
            }
        });


        // 스나크 확인용
        btn_snark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("task","vote");
                intent.putExtra("mode","verify");
                startActivityForResult(intent, SNARK_CODE);
            }
        });

        btn_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, makevoteinput.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("param","e_id");
                String[] in = new String[8];
                for(int i = 0 ; i < 8 ; i++)
                    in[i] = Integer.toString(0);
                in[7] = Integer.toString(1);

                intent.putExtra("values",in);
                startActivity(intent);
            }
        });

        // DB 확인용
//        btn_db.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                int random_salt = (int)(Math.random() * 1000);
//                Log.d("tag_salt", ""+random_salt);
//
//                DBHelper helper;
//                helper = new DBHelper(getApplicationContext(), "userdb.db",null, 1);
//                db = helper.getWritableDatabase();
//                helper.onUpgrade(db,1,1);
//                //for test
//                if(i==0) {
//                    helper.onUpgrade(db,1,1);
//                    ContentValues values = new ContentValues();
//                    values.put("vote_id", 1);
//                    values.put("pub_key", "test_pk_1");
//                    values.put("salt", random_salt);
//                    values.put("voted", "0");
//                    db.insert("pk", null, values);
//                    i++;
//                }
//
////
//                Cursor c = db.rawQuery("select * from pk;", null);
//                if(c.getCount()>0) {
//                    Log.d("tag_pkcheck", ""+c.getCount());
//                } else {
//                    Log.d("tag_pkcheck", "nothing");
//                }
//                if(c.moveToFirst()) {
//                    while(!c.isAfterLast()){
//                        Log.d("TAG_READ_pk", "" + c.getInt(c.getColumnIndex("salt")));
//                        c.moveToNext();
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        et_id.setText("a1");
        et_pwd.setText("12341234");
        et_id.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                String user_id = (String) data.getExtras().get("ID");
//                connect(user_id);
            } else {
                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == SNARK_CODE) {
            Toast.makeText(getApplicationContext(), "스나크 확인 : "+data.getExtras().get("result"), Toast.LENGTH_SHORT).show();
        }
    }



    // EditText Filter 설정(영어+숫자, 영어+숫자+특수문자, 한글, 숫자)
    protected static class EngNumInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]*$");

            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    }
    protected static class EngNumInputFilter2 implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9!@#$%^&*()]*$");

            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    }
    protected static class KorInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[ㄱ-ㅣ가-힣]*$");

            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    }
    protected static class NumInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[0-9]*$");

            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    }
    class CustomTask extends AsyncTask<String, String, String> {
        String sendMsg, receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://222.111.165.26:8080/login.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                sendMsg = "mode="+strings[2]+"&user_id="+strings[0]+"&user_pw="+strings[1];
                OutputStream outs = conn.getOutputStream();
                outs.write(sendMsg.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    public Point getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        return size;
    }
    public void getStanderSize() {
        Point ScreenSize = getScreenSize(this);
        density  = getResources().getDisplayMetrics().density;

        standardSize_X = (int) (ScreenSize.x / density);
        standardSize_Y = (int) (ScreenSize.y / density);
    }

    private void FindViewID() {
        toolbar = findViewById(R.id.toolbar);

        btn_login = findViewById(R.id.btn_login);
        btn_admin = findViewById(R.id.btn_admin);
        btn_signup = findViewById(R.id.btn_signup);

        tv_login= findViewById(R.id.tv_login);

        et_id = findViewById(R.id.et_id);
        et_pwd = findViewById(R.id.et_pwd);

        btn_snark = findViewById(R.id.btn_snark);
        btn_db = findViewById(R.id.btn_db);
        tv_test = findViewById(R.id.tv_test);

    }
    private void TextSizeSet() {
        tv_login.setTextSize((float) (standardSize_X/12));
        et_id.setTextSize((float) (standardSize_X/20));
        et_pwd.setTextSize((float) (standardSize_X/20));

        btn_login.setTextSize((float) (standardSize_X/20));
        btn_signup.setTextSize((float) (standardSize_X/20));
        btn_admin.setTextSize((float) (standardSize_X/20));
    }

}