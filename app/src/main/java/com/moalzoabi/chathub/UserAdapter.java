package com.moalzoabi.chathub;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {
    private Context context;
    private List<Users> mUsers, userFilterList;
    private boolean isChat;

    public UserAdapter(Context ct, List<Users> mUsers, boolean isChat){
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
        View view = LayoutInflater.from(context).inflate(R.layout.users, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UserAdapter.ViewHolder holder, int position) {
        Users users = mUsers.get(position);
        holder.username.setText(users.getUsername());
        holder.bioTextView.setText(users.getBio());

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

        /*if(isChat){
            if(users.getStatus().equals("Online")){
                holder.statusTextViewOn.setVisibility(View.VISIBLE);
                holder.statusTextViewOff.setVisibility(View.GONE);
            } else {
                holder.statusTextViewOn.setVisibility(View.GONE);
                holder.statusTextViewOff.setVisibility(View.VISIBLE);
            }
        } else {
            holder.statusTextViewOn.setVisibility(View.GONE);
            holder.statusTextViewOff.setVisibility(View.GONE);
        }*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userid", users.getId());
                context.startActivity(intent);
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

    private Filter userFilter = new Filter() {
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
        public TextView statusTextViewOn;
        public TextView statusTextViewOff;
        public TextView bioTextView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.usernameListTextView);
            imageView = itemView.findViewById(R.id.profileImageListImageView);
            statusTextViewOn = itemView.findViewById(R.id.statusTextViewOn);
            statusTextViewOff = itemView.findViewById(R.id.statusTextViewOff);
            bioTextView = itemView.findViewById(R.id.bioTextView);
        }
    }

}

