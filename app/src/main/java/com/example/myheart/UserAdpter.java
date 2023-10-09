package com.example.myheart;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {
    Context Expert;
    ArrayList<Users> usersArrayList;

    public UserAdpter(Expert expert, ArrayList<Users> usersArrayList) {
        this.Expert = expert;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public UserAdpter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(Expert).inflate(R.layout.user_item,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdpter.viewholder holder, int position) {

        Users users = usersArrayList.get(position);
        holder.userstatus.setText(users.status);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Expert, ChatActivity.class);
                intent.putExtra("uid",users.getId());
                Expert.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        TextView userstatus;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}
