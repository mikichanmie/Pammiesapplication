package com.example.myheart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Expert extends AppCompatActivity {
    private Button heart1;
    ArrayList<Users> usersArrayListExpert;
    ArrayList<Users> usersArrayListTaker;
    FirebaseDatabase database;
    String consultFilter, statusUser;
    FirebaseAuth mAuth;
    String id = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        heart1 = findViewById(R.id.consult);
        mAuth = FirebaseAuth.getInstance();
        onClick();

    }

    private void onClick() {
        heart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStatus();
                getConsult();
            }
        });
    }

    private void getStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        id = currentUser.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (id.equals(users.id)) {
                        statusUser = users.status;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void getConsult() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayListTaker = new ArrayList<>();
                usersArrayListExpert = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (statusUser.equals("ผู้เชี่ยวชาญ")) {
                        if (users.status.equals("รับคำปรึกษา") && !users.status.isEmpty()) {
                            usersArrayListTaker.add(users);
                        }
                    } else {
                        if (users.status.equals("ผู้เชี่ยวชาญ") && !users.status.isEmpty()) {
                            usersArrayListExpert.add(users);
                        }
                    }
                }
                if(statusUser.equals("ผู้เชี่ยวชาญ")){
//                    int index = (int)(Math.random() * usersArrayListTaker.size());
//                    usersArrayListTaker.get(index);
                    //TODO
                    //หาห้องที่ผู้รับเปิดไว้
                    getRoomForTaker();
                }else {
                    int index = (int)(Math.random() * usersArrayListExpert.size());
                    usersArrayListExpert.get(index);
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("uid",usersArrayListExpert.get(index).getId());
                    intent.putExtra("name",usersArrayListExpert.get(index).getNickname());
                    intent.putExtra("status",usersArrayListExpert.get(index).getStatus());
                    intent.putExtra("token",usersArrayListExpert.get(index).getToken());
                    intent.putExtra("statusTake",statusUser);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void getRoomForTaker() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String giver = "";
                String expert = "";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String dataKey = dataSnapshot.getKey();
                    giver = dataKey.substring(0,28);
                    expert = dataKey.substring(28);
                    Log.e("XXX", "test"+dataSnapshot);
                    break;
                }
                if(giver.equals(id)){
                    for(Users item : usersArrayListTaker){

                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra("uid",item.getId());
                        intent.putExtra("name",item.getNickname());
                        intent.putExtra("status",item.getStatus());
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        id = currentUser.getUid();
    }
}


