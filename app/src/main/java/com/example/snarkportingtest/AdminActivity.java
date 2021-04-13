package com.example.snarkportingtest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdminActivity extends AppCompatActivity implements AdminVotelistAdapter.OnItemClickListener {

    private static final int REQUEST_CODE = 777;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Votedetail> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    TextView tv_adminmain;
    TextView tv_adminid;
    TextView tv_votelist;

    Button btn_admincreatevote;

    Toolbar toolbar;

    Date now;
    int today_y,today_m,today_d;
    int[] start_date, end_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        FindViewID();   // layout view 아이디 연결
        TextSizeSet();  // text size 자동조절

        toolbar.setTitle("VoteApp - Admin");
        setSupportActionBar(toolbar);


        btn_admincreatevote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent create = new Intent(AdminActivity.this, CreatevoteActivity.class);
                startActivity(create);
            }
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String adminid = currentUser.getEmail().split("@")[0];

        recyclerView = findViewById(R.id.rv_adminvotelist); // 아이디 연결
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // Votelist 객체를 담을 어레이 리스트(어댑터 쪽으로)

        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동

        databaseReference = database.getReference("Votelist"); // DB 테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 함수
                arrayList.clear(); // 기존 배열 초기화
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) { // 반복문으로 데이터 리스트를 추출
                    Votedetail votedetail  = snapshot.getValue(Votedetail.class); // 만들어둔 Votedetail 객체에 데이터를 담는다.
                    if(votedetail.getCreated().equals(adminid)){
                        arrayList.add(votedetail); // 담은 데이터들을 배열에 넣고 리사이클뷰로 보낼 준비
                    }
                }
                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // DB를 가져오던 중 에러 발생시
                Log.e("VoterActivity", String.valueOf(databaseError.toException()));
            }
        });

        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));  // 투표목록 구분선

        adapter = new AdminVotelistAdapter(arrayList, this, this);
        recyclerView.setAdapter(adapter); // 리사이클러뷰에 어댑터 연결
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String adminid = currentUser.getEmail().split("@")[0];
        tv_adminid.setText(adminid);

        Datesetting(); // date setting
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseAuth.getInstance().signOut(); // 로그아웃(firebase 로그인 연동)
    }


    @Override
    public void onItemClick(int position) {
        Votedetail votedetail = arrayList.get(position);
        String start_date = votedetail.getStart();
        String end_date = votedetail.getEnd();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = null;
        Date s_date = null;
        Date e_date = null;
        try {
            s_date = dateFormat.parse(String.valueOf(start_date));
            e_date = dateFormat.parse(String.valueOf(end_date));
            today = dateFormat.parse(dateFormat.format(now));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(AdminActivity.this, AdminvoteActivity.class);
        intent.putExtra("vote", votedetail);

        int check_started = today.compareTo(s_date);
        int check_ended = today.compareTo(e_date);
        if(check_started <= 0) {
            intent.putExtra("started", true);
        } else {
            intent.putExtra("started", false);
        }
        if(check_ended > 0) {
            intent.putExtra("ended", false);
        } else {
            intent.putExtra("ended", true);
        }
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void Datesetting() {
        SimpleDateFormat yearformat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthformat = new SimpleDateFormat("MM");
        SimpleDateFormat dayformat = new SimpleDateFormat("dd");

        now = new Date();

        today_y = Integer.parseInt(yearformat.format(now));
        today_m = Integer.parseInt(monthformat.format(now));
        today_d = Integer.parseInt(dayformat.format(now));
    }

    private void FindViewID() {
        toolbar = findViewById(R.id.toolbar);

        tv_adminmain = findViewById(R.id.tv_adminmain);
        tv_adminid = findViewById(R.id.tv_adminid);
        tv_votelist = findViewById(R.id.tv_votelist);

        btn_admincreatevote = findViewById(R.id.btn_admincreatevote);
    }
    private void TextSizeSet() {
        tv_adminmain.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/12));
        tv_adminid.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        tv_votelist.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
        btn_admincreatevote.setTextSize((float) (((MainActivity)MainActivity.context_main).standardSize_X/20));
    }
}