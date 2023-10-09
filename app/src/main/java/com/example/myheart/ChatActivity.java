package com.example.myheart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class ChatActivity extends AppCompatActivity {
    String  reciverUid,reciverName,SenderUID = "", myStatus, token;
    Button stopchat;
    TextView reciverNName;
    FirebaseDatabase databaseChat;
    FirebaseAuth firebaseAuth;
    ImageButton sendbtn, notificationbtn ;
    EditText textmsg;
    String senderRoom,reciverRoom;
    RecyclerView messageAdpter;
    ArrayList<msgModelclass> messagesArrayList;
    messagesAdpter mmessagesAdpter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();

        databaseChat = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        reciverName = getIntent().getStringExtra("status");
        reciverUid = getIntent().getStringExtra("uid");
        myStatus = getIntent().getStringExtra("statusTake");
        token = getIntent().getStringExtra("token");

        messagesArrayList = new ArrayList<>();

        stopchat = findViewById(R.id.stopchat);
        sendbtn = findViewById(R.id.sendmsg);
        textmsg = findViewById(R.id.chatmsg);
        reciverNName = findViewById(R.id.recivername);
        messageAdpter = findViewById(R.id.msgadpter);
        notificationbtn = findViewById(R.id.notification_btn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageAdpter.setLayoutManager(linearLayoutManager);
        mmessagesAdpter = new messagesAdpter(ChatActivity.this,messagesArrayList);
        messageAdpter.setAdapter(mmessagesAdpter);

        reciverNName.setText(""+reciverName);

        SenderUID =  firebaseAuth.getUid();
        senderRoom = SenderUID+reciverUid;
        reciverRoom = reciverUid+SenderUID;

        DatabaseReference  chatreference = databaseChat.getReference().child("chats").child(senderRoom).child("messages");
        new Thread(() -> {
            chatreference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    messagesArrayList.clear();
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        msgModelclass messages = dataSnapshot.getValue(msgModelclass.class);
                        messagesArrayList.add(messages);
                    }
                    mmessagesAdpter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }).start();

        notificationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FCMSend.pushNotification(
                        ChatActivity.this,
                        token,
                        "ผู้รับต้องการคำปรึกษา",
                        "กรุณาเข้าแอพเพื่อให้คำปรึกษา"
                );
            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = textmsg.getText().toString();
                if (message.isEmpty()){
                    Toast.makeText(ChatActivity.this, "Enter The Message First", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> words = Arrays.asList("ควาย","โง่","สัส","เหี้ย","สัด","ควย","ครวย","พ่อมึงตาย","แม่มึงตาย","พ่อง","สวะ","ไม่มีใครรักออ","ไร้สมอง","ปัญญาอ่อน","ปยอ","ปญอ","สถุน","ขาดความอบอุ่น","มึง","กู","กุ","มรึง","เชี้ย","เฮี้ย","เหรี้ย","โงร่"); // suppose these words are offensive
                for (String word : words) {
                    Pattern rx = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);
                    message = rx.matcher(message).replaceAll(new String(new char[word.length()]).replace('\0', '*'));
                }

                textmsg.setText("");
                Date date = new Date();
                msgModelclass messagess = new msgModelclass(message,SenderUID,date.getTime());

                databaseChat=FirebaseDatabase.getInstance();
                databaseChat.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                databaseChat.getReference().child("chats")
                                        .child(reciverRoom)
                                        .child("messages")
                                        .push().setValue(messagess).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        });
            }
        });

        stopchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setMessage("คุณตกลงที่จะจบแชทนี้หรือไม่                  อย่าลืมบอกลากันนะ :)")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                Match.checkPage = true;
                                DatabaseReference  chatreference = databaseChat.getReference().child("chats");
                                DatabaseReference dataToDeleteRefSender = chatreference.child(senderRoom);
                                DatabaseReference dataToDeleteRefReciver = chatreference.child(reciverRoom);

                                // Use the removeValue() method to delete the data
                                dataToDeleteRefSender.removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Data deleted successfully
                                                Toast.makeText(ChatActivity.this, "ลบเรียบร้อย", Toast.LENGTH_SHORT).show();
                                                // Use the removeValue() method to delete the data
                                                dataToDeleteRefReciver.removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Data deleted successfully
                                                                Toast.makeText(ChatActivity.this, "ลบเรียบร้อย", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                                                                startActivity(intent);
                                                                finish();

                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Failed to delete data
                                                                Toast.makeText(ChatActivity.this, "ไม่ได้ลบ", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Failed to delete data
                                                Toast.makeText(ChatActivity.this, "ไม่ได้ลบ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

}