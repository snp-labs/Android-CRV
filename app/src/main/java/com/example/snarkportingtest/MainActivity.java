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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 777;

    public static Context context_main;
    public static int standardSize_X, standardSize_Y;
    public float density;

//    public final static String ip = "192.168.219.100";    // 집
    public final static String ip = "192.168.0.168";      // 한양대
    public final static int port = 9999;


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


    private FirebaseAuth mAuth;

    private Handler mHandler;
    private Socket socket;
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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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
                    SignIn(et_id.getText().toString() + "@vote.com", et_pwd.getText().toString());
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

        //firebase message service
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Message", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("Message", msg);
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic("test")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d("cloud message", msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        // 스나크 확인용
        btn_snark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                intent.putExtra("task","tally");
                intent.putExtra("mode","setup");
                startActivityForResult(intent, SNARK_CODE);
            }
        });

        // DB 확인용
        btn_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int random_salt = (int)(Math.random() * 1000);
                Log.d("tag_salt", ""+random_salt);

                DBHelper helper;
                helper = new DBHelper(getApplicationContext(), "userdb.db",null, 1);
                db = helper.getWritableDatabase();
                helper.onUpgrade(db,1,1);
                //for test
                if(i==0) {
                    helper.onUpgrade(db,1,1);
                    ContentValues values = new ContentValues();
                    values.put("vote_id", 1);
                    values.put("pub_key", "test_pk_1");
                    values.put("salt", random_salt);
                    values.put("voted", "0");
                    db.insert("pk", null, values);
                    i++;
                }

//
                Cursor c = db.rawQuery("select * from pk;", null);
                if(c.getCount()>0) {
                    Log.d("tag_pkcheck", ""+c.getCount());
                } else {
                    Log.d("tag_pkcheck", "nothing");
                }
                if(c.moveToFirst()) {
                    while(!c.isAfterLast()){
                        Log.d("TAG_READ_pk", "" + c.getInt(c.getColumnIndex("salt")));
                        c.moveToNext();
                    }
                }
            }
        });
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
                connect(user_id);
            } else {
                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == SNARK_CODE) {
            Toast.makeText(getApplicationContext(), "스나크 확인 : "+data.getExtras().get("result"), Toast.LENGTH_SHORT).show();
        }
    }

    //firebase DB add user uid(아마 불필요할듯)
    private void connect(final String id){
        mHandler = new Handler();
        Thread checkUpdate = new Thread() {
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    Log.d("서버 접속됨", "서버 접속됨");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // output에 보낼꺼 넣음
                    dis = new DataInputStream(socket.getInputStream());     // input에 받을꺼 넣어짐

                    dos.writeUTF("signup");
                    dos.writeUTF(id);
                    Log.d("data sending ID :", id);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    byte[] read_data = new byte[4];
                    dis.read(read_data);
                    user_DB_update = new String(read_data);
                    Log.d("user_DB_update", user_DB_update);
//                    Log.d("id_check_status", String.valueOf(id_check_status.equals("fail")));
                }catch (Exception e){
                }
            }
        };
        checkUpdate.start();

        try {
            checkUpdate.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            Pattern ps = Pattern.compile("^[ㄱ-ㅎ가-힣]*$");

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

    private void SignIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGNIN", "signInWithEmail:success");
                            Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent intent = new Intent(MainActivity.this, UsermainActivity.class);
                            str = et_id.getText().toString();
                            intent.putExtra("str", str);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGNIN", "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

    private class DB_check extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            votedetails.clear();
            progressDialog = ProgressDialog.show(MainActivity.this, "Please wait...DB Loading...", null, true, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String serverUrl = (String) strings[0];

            String postParameters = "voter=a1 & votelist=" + vote_id_list.toString().replaceAll(" |\\[|\\]","");
            Log.d("TAG_DB", "POST param :: " + postParameters);

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.setDoOutput(true);       //데이터를 쓸 지 설정
//                httpURLConnection.setDoInput(true);        //데이터를 읽어올지 설정
//                httpURLConnection.setRequestProperty("Content-Type","application/json");
//                httpURLConnection.setRequestProperty("Accept","application/json");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8")); // user id 들어가야함
//                outputStream.write(("&votelist=" + vote_id_list.toString().replaceAll(" |\\[|\\]","")).getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("TAG_DB", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return new String("Error: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            jsonString = s;
            Log.d("tag_db_total", s);
            doParse();
            progressDialog.dismiss();
        }

        private void doParse() {
            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("votelist");

                for(int i = 0; i<jsonArray.length(); i++){
                    Votedetail votedetail = new Votedetail();
                    JSONObject item = jsonArray.getJSONObject(i);

                    ContentValues values = new ContentValues();
                    values.put("vote_id", item.getInt("vote_id"));
                    values.put("title", item.getString("title"));
                    values.put("admin", item.getString("admin"));
                    values.put("start_date", item.getString("start"));
                    values.put("end_date", item.getString("end"));
                    values.put("type", item.getString("type"));
                    values.put("note", item.getString("note"));
                    db.insert("votelist", null, values);
                }
                String sql = "select * from votelist;";
                Cursor c = db.rawQuery(sql, null);
                if(c.moveToFirst()) {
                    while(!c.isAfterLast()){
                        int vote_id;
                        Log.d("TAG_READ_usermain", "" + c.getInt(c.getColumnIndex("vote_id")) + c.getString(c.getColumnIndex("title")));
                        c.moveToNext();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG_DB_error",e.getMessage());
            }
        }
    }
}