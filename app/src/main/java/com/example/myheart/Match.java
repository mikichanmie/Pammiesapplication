package com.example.myheart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class Match extends AppCompatActivity {
    private Button heart1, find_chat_room;
    ArrayList<Users> usersArrayListGiver;
    ArrayList<Users> usersArrayListTaker;
    FirebaseDatabase database;
    String consultFilter, statusUser;
    FirebaseAuth mAuth;
    String id = "", taker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        heart1 = findViewById(R.id.consult);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        getStatus();
        getConsult();

        heart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(statusUser.equals("ให้คำปรึกษา")){
                    for (Users item : usersArrayListTaker) {
                        if (item.id.equals(taker)) {
                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                            intent.putExtra("uid", item.getId());
                            intent.putExtra("name", item.getNickname());
                            intent.putExtra("status", item.getStatus());
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                }else {
                    int index = (int)(Math.random() * usersArrayListGiver.size());
                    usersArrayListGiver.get(index);
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("uid",usersArrayListGiver.get(index).getId());
                    intent.putExtra("name",usersArrayListGiver.get(index).getNickname());
                    intent.putExtra("status",usersArrayListGiver.get(index).getStatus());
                    intent.putExtra("token",usersArrayListGiver.get(index).getToken());
                    intent.putExtra("statusTake",statusUser);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
    private void getStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        id = currentUser.getUid();

        DatabaseReference reference = database.getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (id.equals(users.id)) {
                        statusUser = users.status;
                        break;
                    }
                }
                if(statusUser.equals("ให้คำปรึกษา")){
                    getRoomForTaker();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void getConsult() {
        DatabaseReference reference = database.getReference().child("users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayListTaker = new ArrayList<>();
                usersArrayListGiver = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (statusUser.equals("ให้คำปรึกษา")) {
                        if (users.status.equals("รับคำปรึกษา") && !users.status.isEmpty()) {
                            usersArrayListTaker.add(users);
                        }
                    } else {
                        if (users.status.equals("ให้คำปรึกษา") && !users.status.isEmpty()) {
                            usersArrayListGiver.add(users);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void getRoomForTaker() {
        ProgressDialog progressdialog = new ProgressDialog(Match.this);
        progressdialog.setMessage("Please Wait....");
        progressdialog.show();
            DatabaseReference reference = database.getReference().child("chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String dataKey = dataSnapshot.getKey();
                            if (dataKey.substring(0, 28).equals(id)) {
                                taker = dataKey.substring(28);
                                Log.e("XXX", "test" + dataKey.substring(0, 28) + "equals" + " " + id);
                                break;
                            }
                            Log.e("XXX", "test" + dataSnapshot);
                        }
                    progressdialog.dismiss();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    finish();
                }
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


