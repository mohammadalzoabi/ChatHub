package com.moalzoabi.chathub;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<Users> mUsers, userFilterList;
    private boolean isChat;

    DatabaseReference reference;

    String lastMessageChat;

    public ChatsAdapter(Context ct, List<Users> mUsers, boolean isChat){
        this.context = ct;
        this.mUsers = mUsers;
        this.isChat = isChat;
        userFilterList = new ArrayList<>();
        userFilterList.addAll(mUsers);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user, parent, false);
        return new ChatsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChatsAdapter.ViewHolder holder, int position) {

        Users users = mUsers.get(position);
        holder.username.setText(users.getUsername());

        if(users.getImageURL().equals("default")){
            holder.imageView.setImageResource(R.drawable.logo);
        } else {
            Glide.with(context)
                    .load(users.getImageURL())
                    .dontAnimate()
                    .fitCenter()
                    .placeholder(holder.imageView.getDrawable())
                    .into(holder.imageView);
        }

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && !chat.getIsSeen() && chat.getSender().equals(users.getId())) {
                        holder.notOpened.setVisibility(View.VISIBLE);
                    } else {
                        holder.notOpened.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

        lastMessage(users.getId(), holder.lastMessageTextView);

        /*if(isChat){
            if(users.getStatus().equals("Online")){
                holder.statusTextView.setVisibility(View.VISIBLE);
                holder.statusTextView.setText("Online");
                holder.statusTextView.setBackgroundResource(R.drawable.online_status_shape);
            } else {
                holder.statusTextView.setVisibility(View.VISIBLE);
                holder.statusTextView.setText("Offline");
                holder.statusTextView.setBackgroundResource(R.drawable.online_status_shape_offline);
            }
        } else {
            holder.lastMessageTextView.setVisibility(View.GONE);
            holder.statusTextView.setVisibility(View.GONE);
        }*/



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", users.getId());
                context.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private final Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Users> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(userFilterList);
            }else{
                String filter = constraint.toString().toLowerCase().trim();
                for(Users users : userFilterList){
                    if(users.getUsername().toLowerCase().contains(filter)){
                        filteredList.add(users);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mUsers.clear();
            mUsers.addAll((Collection<? extends Users>) results.values);
            notifyDataSetChanged();

        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView imageView;
        public LottieAnimationView notOpened;
        public TextView lastMessageTextView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.usernameListTextView);
            imageView = itemView.findViewById(R.id.profileImageListImageView);
            lastMessageTextView = itemView.findViewById(R.id.lastMessageTextView);
            notOpened = itemView.findViewById(R.id.notOpened);
        }
    }


    private void lastMessage(String userId, final TextView lastMessageText){
        lastMessageChat = "";
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    Chat chat = snapshot1.getValue(Chat.class);
                    if(chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())){
                        lastMessageChat = chat.getMessage();
                    }
                }

                switch (lastMessageChat){
                    case "sample":
                        lastMessageText.setText("sample");
                        break;

                    default:
                        lastMessageText.setText(lastMessageChat);
                        break;
                }
                lastMessageChat = "";
            }
            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }
}

