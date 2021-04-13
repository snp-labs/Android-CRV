package com.example.snarkportingtest;

import android.app.AsyncNotedAppOp;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.snarkportingtest.MainActivity.ip;

// 알림, 설정 버튼 설정하기!!!!!!!!!
public class UsermainActivity extends AppCompatActivity implements VotelistAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Votedetail> votelist;

    Toolbar toolbar;

    private static final int REQUEST_CODE = 777;

    TextView tv_votetest;
    TextView tv_uservotelist;

    private String user_id;
//    private String[] votelist = {};

    // Mysql DB
    private String jsonString;
    private ArrayList<Integer> arr_votelist;

    // sqlite DB
    DBHelper helper;
    SQLiteDatabase db;
    private ArrayList<Integer> vote_id_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermain);

        FindViewID();   // layout view 아이디 연결
        TextSizeSet();  // text size 자동조절

        setSupportActionBar(toolbar);

        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        votelist = new ArrayList<>(); // Votelist 객체를 담을 어레이 리스트(어댑터 쪽으로)
        vote_id_list = new ArrayList<>();

        adapter = new VotelistAdapter(votelist, this, this);
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));  // 투표목록 구분선
        votelist.clear();

        Intent getintent = getIntent();
        user_id = (String) getintent.getExtras().get("user_id");

        Intent makeinput = new Intent(UsermainActivity.this, makevoteinput.class);
        makeinput.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        makeinput.putExtra("param", "PP");
        startActivity(makeinput);

    }

    @Override
    protected void onStart() {
        super.onStart();

//        databaseReference = database.getReference("User"); // DB 테이블 연결 - user data 확인(votelist)
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.child(user_id).getValue(User.class);
//                votelist = user.getVotelist().split(",");
//                Log.d("votelist", votelist[0] + votelist[1]);
//                ReadDB("Votelist");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // DB를 가져오던 중 에러 발생시
//                Log.e("UsermainActivity", String.valueOf(databaseError.toException()));
//            }
//        });
        // sqlite check

        helper = new DBHelper(getApplicationContext(), "userdb.db",null, 1);
        db = helper.getWritableDatabase();

        //select table - read DB
        votelist.clear();
        vote_id_list.clear();

//        Cursor c = db.rawQuery("select * from votelist;", null);
//        if(c.moveToFirst()) {
//            while(!c.isAfterLast()){
//                Log.d("TAG_READ_votelist", "" + c.getInt(c.getColumnIndex("vote_id")));
//                vote_id_list.add(c.getInt(c.getColumnIndex("vote_id")));        // server DB와 비교하기 위함
//
//                Votedetail votedetail = new Votedetail();
//                votedetail.setVote_id(c.getInt(c.getColumnIndex("vote_id")));
//                votedetail.setTitle(c.getString(c.getColumnIndex("title")));
//                votedetail.setCreated(c.getString(c.getColumnIndex("admin")));
//                votedetail.setStart(c.getString(c.getColumnIndex("start_date")));
//                votedetail.setEnd(c.getString(c.getColumnIndex("end_date")));
//                votedetail.setType(c.getString(c.getColumnIndex("type")));
//                votedetail.setNote(c.getString(c.getColumnIndex("note")));
//
//                votelist.add(votedetail);       // 현재 폰 DB에 있는 투표정보
//                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
//                c.moveToNext();
//            }
////                    Log.d("Tag_sql", "제발"+vote_id_list.toString());
//        }
//        Log.d("TAG_SQLITE", "suc");

        // Mysql DB connect - Read votelist
        DB_check task = new DB_check();
//        task.execute("http://"+ip+":80/project/votervotelist_read.php");   // 집 ip
//        task.execute("http://192.168.0.168:80/project/votervotelist_read.php");     // 한양대 ip
        task.execute("http://"+ip+":8080/votelist_read.php");   // 국민대 ip
    }

    // 뒤로가기 하단 버튼 클릭시 로그아웃
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    // recyclerview 투표 선택시 투표 화면 이동
    @Override
    public void onItemClick(int position) {
        Votedetail votedetail = votelist.get(position);
        Intent intent = new Intent(UsermainActivity.this, VoteActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("vote", votedetail);
        startActivityForResult(intent, REQUEST_CODE);


    }

    // 투표 화면에서 돌아올 때(toastbox)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText(getApplicationContext(), "투표 완료", Toast.LENGTH_SHORT).show();
                // 투표 선택 정보 확인
                String candidate = String.valueOf(data.getExtras().getString("voted"));
                String title = String.valueOf(data.getExtras().getString("title"));
                tv_votetest.setText(title+" : "+candidate);
            } else {
                Toast.makeText(getApplicationContext(), "투표 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitem, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_renew:
                onStart();
                break;
            case R.id.action_notice:
                Toast.makeText(this, "공지사항", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UsermainActivity.this, NoticeActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                Toast.makeText(this, "설정",Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }


    // Mysql DB
    private class DB_check extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(UsermainActivity.this, "Please wait...DB Loading...", null, true, true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String serverUrl = (String) strings[0];

            String postParameters = "voter="+user_id+" & votelist=" + vote_id_list.toString().replaceAll(" |\\[|\\]","");    // user id 들어가야함
            Log.d("TAG_DB", "POST param :: " + postParameters);

            try {
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
                outputStream.write(postParameters.getBytes("UTF-8"));
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

                Log.d("INCOMING MSG : ", sb.toString());
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
            Log.d("TAG_DB_total", s);
            doParse();
            progressDialog.dismiss();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        private void doParse(){
            try{
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("votelist");

                for(int i = 0; i<jsonArray.length(); i++){
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

                    Votedetail votedetail = new Votedetail();
                    votedetail.setVote_id(item.getInt("vote_id"));
                    votedetail.setTitle(item.getString("title"));
                    votedetail.setCreated(item.getString("admin"));
                    votedetail.setStart(item.getString("start"));
                    votedetail.setEnd(item.getString("end"));
                    votedetail.setType(item.getString("type"));
                    votedetail.setNote(item.getString("note"));

                    votelist.add(votedetail);
                    adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
                }
                String sql = "select * from votelist;";
                Cursor c = db.rawQuery(sql, null);
                if(c.moveToFirst()) {
                    while(!c.isAfterLast()){
                        int vote_id;
//                        Log.d("TAG_READ_usermain", "" + c.getInt(c.getColumnIndex("vote_id")) + c.getString(c.getColumnIndex("title")));
                        c.moveToNext();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG_DB_error",e.getMessage());
            }
        }
    }

    private void FindViewID() {
        toolbar = findViewById(R.id.toolbar);

        tv_votetest = findViewById(R.id.tv_votetest);
        tv_uservotelist = findViewById(R.id.tv_uservotelist);

        recyclerView = findViewById(R.id.rv_uservotelist); // 아이디 연결
    }
    private void TextSizeSet() {
        tv_uservotelist.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/12));
    }

}

