package com.moalzoabi.chathub;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private Context context;
    private List<Chat> mChat;
    private String imgURL;
    private String timeStamp;

    FirebaseUser firebaseUser;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public MessageAdapter(Context ct, List<Chat> mChat, String imgURL){
        this.context = ct;
        this.mChat = mChat;
        this.imgURL = imgURL;
    }

    @NonNull
    @NotNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_RIGHT){
            view = LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());

        timeStamp = chat.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("HH:mm", calendar).toString();

        holder.date.setText(dateTime);

        if(imgURL.equals("default")){
            holder.profileImage.setImageResource(R.drawable.logo);
        } else {
            Glide.with(context)
                    .load(imgURL)
                    .dontAnimate()
                    .fitCenter()
                    .placeholder(holder.profileImage.getDrawable())
                    .into(holder.profileImage);
        }

        if(position == mChat.size()-1){
            if(chat.getIsSeen()){
                holder.isSeenTextView.setText("Seen");
            } else {
                holder.isSeenTextView.setText("Delivered");
            }
        } else{
            holder.isSeenTextView.setVisibility(View.GONE);
        }



        holder.messageRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure you want to delete this message?");
                builder.setIcon(R.drawable.ic_delete);
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage("Deleting");
                        progressDialog.show();

                        deleteMessage(position);

                        progressDialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView show_message;
        public ImageView profileImage;
        public TextView isSeenTextView;
        public TextView date;
        RelativeLayout messageRelativeLayout;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profileImage = itemView.findViewById(R.id.profile_image);
            isSeenTextView = itemView.findViewById(R.id.seenStatusTextView);
            date = itemView.findViewById(R.id.timeStampTextView);
            messageRelativeLayout = itemView.findViewById(R.id.messageRelativeLayout);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return  MSG_TYPE_LEFT;
        }
    }


    private void deleteMessage(int position) {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String messageTimeStamp = mChat.get(position).getTime();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = reference.orderByChild("time").equalTo(messageTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("sender").getValue().equals(myId)) {
                        dataSnapshot.getRef().removeValue();
                        Toast.makeText(context, "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "You Can Only Delete Your Messages", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

    }
}

