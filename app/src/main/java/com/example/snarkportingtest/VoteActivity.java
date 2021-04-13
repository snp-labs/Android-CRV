package com.example.snarkportingtest;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.snarkportingtest.MainActivity.ip;

public class VoteActivity extends AppCompatActivity implements IngCandidateAdapter.OnItemClickListener {

    TextView tv_votedetailtitle;
    TextView tv_votedetailterm;
    TextView tv_votedetailtype;
    TextView tv_votedetailnote;

    Button btn_votecandidateinfo;
    Button btn_voteback;
    Button btn_votecomplete;

    Toolbar toolbar;

    private String voted;

    private String user_id;
    private int voted_position;
    private String[] votelist;

    private RecyclerView rv_votecandidatelist;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Candidate> candidates;


    private Handler mHandler;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String id_check_status = "";

    private Date now = new Date();
    private Date start = null;
    private Date end = null;

    private String vote_state;

    // Mysql DB
    private String jsonString;
    private int vote_id;

    // sqlite DB
    DBHelper helper;
    SQLiteDatabase db;
    private Boolean check_pk = false; //온오프라인 투표인지 확인하기 위해 pk가 있는지 확인하기 위한 변수
    EditText et_pwd;
    private Boolean real_key;
    ContentValues values;
    Intent finishintent = new Intent();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        FindViewID();   // layout view 아이디 연결
        TextSizeSet();  // text size 자동 조절

        setSupportActionBar(toolbar);



        Intent getintent = getIntent();
        user_id = (String) getintent.getExtras().get("user_id");
        Votedetail votedetail = (Votedetail) getintent.getExtras().get("vote");

        vote_id = votedetail.getVote_id();

        Intent inputintent = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent.putExtra("param","e_id");
        String[] in = new String[8];
        for(int i = 0 ; i < 8 ; i++)
            in[i] = (Integer.toString(0));
        in[7] = (Integer.toString(vote_id));
        inputintent.putExtra("values",in);
        startActivity(inputintent);

        rv_votecandidatelist.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        rv_votecandidatelist.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));  // 투표목록 구분선
        layoutManager = new LinearLayoutManager(this);
        rv_votecandidatelist.setLayoutManager(layoutManager);
        candidates = new ArrayList<>(); // Candidate 객체를 담을 어레이 리스트(어댑터 쪽으로)
        candidates.clear(); // 기존 배열 초기화


        //sqlite DB check pk_list
        helper = new DBHelper(getApplicationContext(), "userdb.db",null, 1);
        db = helper.getWritableDatabase();

        Cursor c = db.rawQuery("select vote_id from pk where vote_id="+vote_id+";", null);

        if(c.getCount()>0) {
            Log.d("tag_pkcheck", ""+c.getCount());
            check_pk = true;
        } else {
            Log.d("tag_pkcheck", "nothing");
            check_pk = false;
        }
        // 비밀번호 입력
        et_pwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        et_pwd.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);


        // 투표정보 화면 표시
        tv_votedetailtitle.setText(votedetail.getTitle());
        tv_votedetailterm.setText(votedetail.getStart()+"-"+votedetail.getEnd());
        tv_votedetailtype.setText(votedetail.getType());
        tv_votedetailnote.setText(votedetail.getNote());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            now = dateFormat.parse(dateFormat.format(now));
            start = dateFormat.parse(votedetail.getStart());
            end = dateFormat.parse(votedetail.getEnd());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int now_start = now.compareTo(start);
        int now_end = now.compareTo(end);
        if(now_start >= 0) {
            if(now_end > 0) {
                // 시작 - 종료 - 현재 // 종료된 투표 // 투표 완료에 따른 버튼 변경이나 화면 조정 필요
                adapter = new EndCandidateAdapter(candidates, this);
                vote_state = "end";
            } else {
                Log.d("check_pk", check_pk.toString());
                // 시작 - 현재 - 종료 // 진행중 투표 --> 투표 키 등록이 되어있으면 투표가능
                if(check_pk) {  // 투표키 등록이 되어잇으면 투표가능
                    adapter = new IngCandidateAdapter(candidates, this, this);
                } else {  // 투표키 등록이 없으면 투표, 키등록 불가
                    adapter = new EndCandidateAdapter(candidates, this);
                }
                vote_state = "ing";
            }
        } else {
            // 현재 - 시작 - 종료 // 시작전 투표 --> 투표 키 등록 가능해야함
            adapter = new BeforeCandidateAdapter(candidates, this);
            btn_votecomplete.setEnabled(true);
            btn_votecomplete.setText("투표키 등록");
            if(!check_pk) {
            }
            vote_state = "before";
        }

        rv_votecandidatelist.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결

        // Mysql DB 연동
        DB_check task = new DB_check();
//        task.execute("http://"+ip+":8080/project/candidate_read.php");   // 집 ip
//        task.execute("http://192.168.0.168:80/project/candidate_read.php");     // 한양대 ip
        task.execute("http://"+ip+":8080/candidate_read.php");   // 국민대 ip
        btn_votecandidateinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VoteActivity.this, CandidateinfoActivity.class);
                intent.putExtra("candidates", candidates);
                startActivity(intent);
            }
        });

        // 뒤로가기 버튼
        btn_voteback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        // 투표 완료 버튼 (TAG_ADMIN_SDK)

        btn_votecomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VoteActivity.this);

                et_pwd.setText("");

                if(et_pwd.getParent() != null){
                    ((ViewGroup)et_pwd.getParent()).removeView(et_pwd);
                }
                builder.setView(et_pwd);
                if(check_pk && vote_state == "ing") {
                    builder.setTitle("투표확인").setMessage("\"" + voted + "\" 에게 투표하시겠습니까?").setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String sk = et_pwd.getText().toString();
                            //connect("vote");
                            int msg;
                            String voted_status=null, snark_status = null;
                            try {
                                msg = candidates.get(voted_position).getCandidate_id();
                                snark_status = encrypt_vote(msg);
                            }catch (Exception e){Log.d("exception", e.toString());}
                            try{
                                voted_status = new connect().execute("vote").get();
                            } catch (Exception e){Log.d("exception", e.toString());}

//                            Toast.makeText(getApplicationContext(), voted_status, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK, finishintent);
                            finishintent.putExtra("result", "true");

                            finish();
                        }
                    }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setCancelable(false);
                }
                else {
                    builder.setTitle("투표키 등록").setMessage("투표키 등록 테스트").setPositiveButton("등록", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String sk = et_pwd.getText().toString();
                            if(sk.length()<6){
                                Toast.makeText(getApplicationContext(),"6자리 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                            } else {
                                real_key = true;
                                int random_salt = (int) (Math.random() * 10000);

                                String pk = sha256(sk+random_salt);

                                values = new ContentValues();
                                values.put("vote_id", vote_id);
                                values.put("pub_key", pk);
                                values.put("salt", random_salt);
                                values.put("voted", "0");
//                                db.insert("pk", null, values);

                                //connect("register_key");
                                try {
                                    id_check_status = new connect().execute("register_key", user_id, tv_votedetailtitle.getText().toString(), pk, Boolean.toString(real_key)).get();
                                } catch (Exception e){}

                                if(id_check_status.equals("succ")) {
                                    Toast.makeText(getApplicationContext(), "키 등록 완료", Toast.LENGTH_SHORT).show();
                                    db.insert("pk", null, values);
                                }
                                else if(id_check_status.equals("done")){
                                    Toast.makeText(getApplicationContext(), "already registered", Toast.LENGTH_SHORT).show();
                                    db.insert("pk", null, values);

                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "키 등록 실패", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setCancelable(false);
                }
                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }
        });

    }



    @Override
    public void onItemClick(int position) {
        // 누구에게 투표 햇는지 저장하는 방식 고민
        voted = candidates.get(position).getName();


        voted_position = position;
//        Log.d("voted_position", String.valueOf(voted_position));

        if(check_pk) {
            btn_votecomplete.setEnabled(true);
            btn_votecomplete.setText("'"+voted+"' 투표");
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        btn_voteback.performClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(vote_state == "before") {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menuitem_fakekey, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_fakekey:
                AlertDialog.Builder builder = new AlertDialog.Builder(VoteActivity.this);

                et_pwd.setText("");

                if(et_pwd.getParent() != null){
                    ((ViewGroup)et_pwd.getParent()).removeView(et_pwd);
                }
                builder.setView(et_pwd);

                builder.setTitle("투표키 등록").setMessage("가짜 투표키 등록 테스트").setPositiveButton("등록", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sk = et_pwd.getText().toString();
                        if(sk.length()<6){
                            Toast.makeText(getApplicationContext(),"6자리 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                        } else {
                            real_key = false;
                            int random_salt = (int) (Math.random() * 10000);

                            String pk = sha256(sk+random_salt);

                            values = new ContentValues();
                            values.put("vote_id", vote_id);
                            values.put("pub_key", pk);
                            values.put("salt", random_salt);
                            values.put("voted", "0");
//                            db.insert("pk", null, values);
                            try {
                                id_check_status = new connect().execute("register_key", user_id, tv_votedetailtitle.getText().toString(), pk, Boolean.toString(real_key)).get();
                            } catch (Exception e){}
                            if(id_check_status.equals("succ")) {
                                Toast.makeText(getApplicationContext(), "키 등록 완료", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "키 등록 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
        return true;
    }


    class connect extends AsyncTask<String, String, String> { ////php
        String sendMsg, receiveMsg;
        ProgressDialog mProgressDialog;
        @Override

        protected void onPreExecute() {

            // Create a progressdialog

            mProgressDialog = new ProgressDialog(VoteActivity.this);

            mProgressDialog.setTitle("Loading...");

            mProgressDialog.setMessage("Connecting Server...");

            mProgressDialog.setCanceledOnTouchOutside(false);

            mProgressDialog.setIndeterminate(false);

            mProgressDialog.show();

        }
        @Override
        protected String doInBackground(String... strings) {
            if (strings[0].equals("register_key")) {
                try {
                    String str;
                    URL url = new URL("http://222.111.165.26:8080/vote_mobile.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.connect();

                    sendMsg = "mode=" + strings[0] + "&user_id=" + strings[1] + "&vote_title=" + strings[2] + "&publickey=" + strings[3] + "&real=" + strings[4];
                    Log.d("send msg", sendMsg);
                    OutputStream outs = conn.getOutputStream();
                    outs.write(sendMsg.getBytes("UTF-8"));
                    outs.flush();
                    outs.close();

                    if (conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer = new StringBuffer();
                        while ((str = reader.readLine()) != null) {
                            buffer.append(str);
                        }
                        receiveMsg = buffer.toString();
                        Log.d("received message", receiveMsg);
                    } else {
                        Log.i("통신 결과", conn.getResponseCode() + "에러");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (strings[0].equals("vote")) {
                try {
                    String boundary = "*****";
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String filename = "/data/data/com.example.snarkportingtest/files/vote_Proof.dat";
                    String str;
                    URL url = new URL("http://222.111.165.26:8080/vote_mobile.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    conn.setRequestProperty("Connection", "Keep-Alive");

                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");

                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    conn.setRequestProperty("uploaded_file", filename);


                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.connect();

//                    sendMsg = "mode=" + strings[0];
//                    Log.d("send msg", sendMsg);
//                    OutputStream outs = conn.getOutputStream();
//                    outs.write(sendMsg.getBytes("UTF-8"));
//                    outs.flush();
//                    outs.close();
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"mode\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes("vote");
                    dos.writeBytes(lineEnd);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"vote_name\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    Log.d("vote_id", String.valueOf(vote_id));
                    dos.writeBytes(String.valueOf(vote_id));
                    dos.writeBytes(lineEnd);

                    // 이미지 전송
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + filename + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    FileInputStream mFileInputStream = new FileInputStream(filename);

                    int bytesAvailable = mFileInputStream.available();
                    int maxBufferSize = 1024;
                    int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                    byte[] buffer = new byte[bufferSize];
                    int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

                    //Log.d("Test", "reimage byte is " + bytesRead);

                    // read image
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = mFileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                    }

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // close streams
                    Log.d("connect" , "File is written");
                    mFileInputStream.close();
                    dos.flush();

                    if (conn.getResponseCode() == conn.HTTP_OK) {
                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer1 = new StringBuffer();
                        while ((str = reader.readLine()) != null) {
                            buffer1.append(str);
                        }
                        receiveMsg = buffer1.toString();
                        Log.e("received message", receiveMsg);
                    } else {
                        Log.i("통신 결과", conn.getResponseCode() + "에러");
                    }

                } catch (Exception e) {
                    Log.d("httperror", e.toString());

                }
            }
            finishintent.putExtra("voted", voted);
            finishintent.putExtra("title", tv_votedetailtitle.getText());
            return receiveMsg;
        }
        @Override

        protected void onPostExecute(String s) {

            mProgressDialog.dismiss();

        }
    }


    // Mysql DB
    private class DB_check extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(VoteActivity.this, "Please wait...DB Loading...", null, true, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String serverUrl = (String) strings[0];

            try {
                String selectData = "vote_id=" + vote_id;
                URL url = new URL(serverUrl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
                httpURLConnection.setDoInput(true);                         // 서버에서 읽기 모드 지정
                httpURLConnection.setDoOutput(true);                       // 서버로 쓰기 모드 지정
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(selectData.getBytes("UTF-8"));
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
            doParse();
            progressDialog.dismiss();
            Log.d("TAG_DB_total", s);

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        private void doParse(){
            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("candidate");

                for(int i = 0; i<jsonArray.length(); i++){
                    Candidate candidate = new Candidate();
                    JSONObject item = jsonArray.getJSONObject(i);
                    candidate.setCandidate_id(item.getInt("candidate_id"));
                    candidate.setVote_id(item.getInt("vote_id"));
                    candidate.setName(item.getString("name"));
                    candidate.setGroup(item.getString("group"));
                    candidate.setProfile(item.getString("profile"));
                    candidate.setNote(item.getString("note"));

                    candidates.add(candidate);
                    adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
                }
                // 기권 추가하기
                candidates.add(new Candidate("https://firebasestorage.googleapis.com/v0/b/voteapptest-325df.appspot.com/o/%EA%B8%B0%EA%B6%8C.png?alt=media&token=130b56a0-ef2b-43cd-b4e6-2c3a4f50b7c6","기권",null, null));
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG_DB_error",e.getMessage());
            }
        }
    }

    private void FindViewID() {
        toolbar = findViewById(R.id.toolbar);

        tv_votedetailtitle = findViewById(R.id.tv_votedetailtitle);
        tv_votedetailterm = findViewById(R.id.tv_votedetailterm);
        tv_votedetailtype = findViewById(R.id.tv_votedetailtype);
        tv_votedetailnote = findViewById(R.id.tv_votedetailnote);

        btn_votecandidateinfo = findViewById(R.id.btn_votecandidateinfo);
        btn_voteback = findViewById(R.id.btn_voteback);
        btn_votecomplete = findViewById(R.id.btn_votecomplete);

        rv_votecandidatelist = findViewById(R.id.rv_votecandidatelist);

        // 투표 dialog edittext
         et_pwd = new EditText(VoteActivity.this);
    }
    private void TextSizeSet() {
        tv_votedetailtitle.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        tv_votedetailterm.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        tv_votedetailtype.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        tv_votedetailnote.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        btn_votecandidateinfo.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        btn_voteback.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        btn_votecomplete.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));

        et_pwd.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
    }
    public static String sha256(String str) {
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++)
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            SHA = sb.toString();
        }catch(Exception e) {
            e.printStackTrace(); SHA = null;
        }
        return SHA;
    }

    private String encrypt_vote(int msg) throws Exception {

        BigInteger G = null, S = null, U = null, T = null;
        BigInteger rand = new BigInteger("1231231212312542673123124124879879879817259871293845798123754981237549312324");
        String inFilePath = "/data/data/com.example.snarkportingtest/files/votein.txt";
        FileReader fileReader = null;
        BufferedReader scanner = null;
        int counter = 0;
        String line = null;
        try {
            fileReader = new FileReader(new File(inFilePath));
            scanner = new BufferedReader(fileReader);
            while ((line = scanner.readLine()) != null) {
                counter++;
//                Log.d("readline", Integer.toString(counter) + "\t" + line.split(" ")[1]);
                if (counter == 2) {
                    G = new BigInteger(line.split(" ")[1], 16);
                } else if (counter == 3) {
                    U = new BigInteger(line.split(" ")[1], 16);
                } else if (counter == 12) {
                    S = new BigInteger(line.split(" ")[1], 16);
                } else if (counter == 13) {
                    T = new BigInteger(line.split(" ")[1], 16);
                }
                else if (counter > 14) {
                    break;
                }
            }
        } catch (Exception e){Log.d("err", e.toString());}
//        Log.d("pp points", G.toString()+"\n"+U.toString()+"\n"+S.toString()+"\n"+T.toString()+"\n");

        byte[] candidate = new byte[32];
        for(int i = 0 ; i < 32  ; i++)
            candidate[i] = 0;
        //10000000 00000000 00000000 00000000
        //    28      29      30      31
        if(msg>100) msg -=100;
        candidate[2 * (16 - msg - 1) ] = (byte) 0x80;
        BigInteger m = new BigInteger(candidate);
//        Log.d("message hex", FIELD_PRIME.toString(16));
        BigInteger FIELD_PRIME = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");
        Log.d("message hex", FIELD_PRIME.toString(16));

        EncActivity enc1 = new EncActivity(G, rand, S, m);
        Log.d("ENC points", enc1.getOutput().toString(16));
        String[] in = new String[3];
        in[0] = rand.mod(FIELD_PRIME).toString(16);
        in[1] = rand.divide(FIELD_PRIME).toString(16);
        in[2] = enc1.getycoordinates()[0].toString(16);
        Log.d("instring", in[0] + "\n" + in[1] + "\n" + in[2] + "\n");
        Intent inputintent = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent.putExtra("param","enc1x");
        inputintent.putExtra("values",in);
        startActivity(inputintent);


        String[] in1 = new String[254];
        in1 = enc1.getdoubleTable1();
        Intent inputintent1 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent1.putExtra("param","ec1x d");
        inputintent1.putExtra("values",in1);
        startActivity(inputintent1);

        String[] in2 = new String[254];
        in2 = enc1.getaddTable1();
        Intent inputintent2 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent2.putExtra("param","ec1x a");
        inputintent2.putExtra("values",in2);
        startActivity(inputintent2);



        String[] in3 = new String[1];
        in3 = enc1.getsubTable1();
        Intent inputintent3 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent3.putExtra("param","ec1x s");
        inputintent3.putExtra("values",in3);
        startActivity(inputintent3);


        String[] in4 = new String[3];

        in4[0] = m.mod(FIELD_PRIME).toString(16);
        in4[1] = m.divide(FIELD_PRIME).toString(16);
        in4[2] = enc1.getycoordinates()[1].toString(16);
        Intent inputintent4 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent4.putExtra("param","enc1y");
        inputintent4.putExtra("values",in4);
        startActivity(inputintent4);


        String[] in5 = new String[254];
        in5 = enc1.getdoubleTable2();
        Intent inputintent5 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent5.putExtra("param","ec1y d");
        inputintent5.putExtra("values", in5);
        startActivity(inputintent5);

        String[] in6 = new String[254];
        in6 = enc1.getaddTable2();
        Intent inputintent6 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent6.putExtra("param","ec1y a");
        inputintent6.putExtra("values", in6);
        startActivity(inputintent6);



        String[] in7 = new String[1];
        in7 = enc1.getsubTable2();
        Intent inputintent7 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent7.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent7.putExtra("param","ec1y s");
        inputintent7.putExtra("values", in7);
        startActivity(inputintent7);

        String[] in8 = new String[3];
        Log.d("enc circuit", G.toString() +"\n"+S.toString()+"\n"+ m.toString());
        in8 = enc1.getwitness();
        Intent inputintent8 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent8.putExtra("param","op1");
        inputintent8.putExtra("values", in8);
        startActivity(inputintent8);


        Log.d("where", "input1 write done\n" + enc1.getOutput().toString());


        EncActivity enc2 = new EncActivity(U, rand, T, m);
        String[] in20 = new String[3];
        in20[0] = rand.mod(FIELD_PRIME).toString(16);
        in20[1] = rand.divide(FIELD_PRIME).toString(16);
        in20[2] = enc2.getycoordinates()[0].toString(16);
        Log.d("instring", in20[0] + "\n" + in20[1] + "\n" + in20[2] + "\n");
        Intent inputintent20 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent20.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent20.putExtra("param","enc2x");
        inputintent20.putExtra("values", in20);
        startActivity(inputintent20);


        String[] in21 = new String[254];
        in21 = enc2.getdoubleTable1();
        Intent inputintent21 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent21.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent21.putExtra("param","ec2x d");
        inputintent21.putExtra("values", in21);
        startActivity(inputintent21);

        String[] in22 = new String[254];
        in22 = enc2.getaddTable1();
        Intent inputintent22 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent22.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent22.putExtra("param","ec2x a");
        inputintent22.putExtra("values", in22);
        startActivity(inputintent22);



        String[] in23 = new String[1];
        in23 = enc2.getsubTable1();
        Intent inputintent23 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent23.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent23.putExtra("param","ec2x s");
        inputintent23.putExtra("values", in23);
        startActivity(inputintent23);


        String[] in24 = new String[3];

        in24[0] = m.mod(FIELD_PRIME).toString(16);
        in24[1] = m.divide(FIELD_PRIME).toString(16);
        in24[2] = enc2.getycoordinates()[1].toString(16);
        Intent inputintent24 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent24.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent24.putExtra("param","enc2y");
        inputintent24.putExtra("values", in24);
        startActivity(inputintent24);


        String[] in25 = new String[254];
        in25 = enc2.getdoubleTable2();
        Intent inputintent25 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent25.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent25.putExtra("param","ec2y d");
        inputintent25.putExtra("values", in25);
        startActivity(inputintent25);

        String[] in26 = new String[254];
        in26 = enc2.getaddTable2();
        Intent inputintent26 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent26.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent26.putExtra("param","ec2y a");
        inputintent26.putExtra("values", in26);
        startActivity(inputintent26);

        Log.d("where", "where");

        String[] in27 = new String[1];
        in27 = enc2.getsubTable2();
        Intent inputintent27 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent27.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent27.putExtra("param","ec2y s");
        inputintent27.putExtra("values", in27);
        startActivity(inputintent27);
        Log.d("where", "where");

        String[] in28 = new String[3];
        Log.d("enc circuit", G.toString() +"\n"+S.toString()+"\n"+ m.toString());
        in28 = enc2.getwitness();
        Intent inputintent28 = new Intent(VoteActivity.this, makevoteinput.class);
        inputintent28.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        inputintent28.putExtra("param","op2");
        inputintent28.putExtra("values", in28);
        startActivity(inputintent28);

        Intent intent = new Intent(VoteActivity.this, SubActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("task","vote");
        intent.putExtra("mode","all");
        startActivityForResult(intent, 888);
        Log.d("enc done", "enc done");
        return "succ";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 888){
            if(resultCode == RESULT_OK){
                Toast.makeText(VoteActivity.this, "Result: " + data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
            } else {   // RESULT_CANCEL
                Toast.makeText(VoteActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}