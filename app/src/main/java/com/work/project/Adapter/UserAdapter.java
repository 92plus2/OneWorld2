package com.work.project.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.work.project.MessageActivity;
import com.work.project.Model.Chat;
import com.work.project.Model.User;
import com.work.project.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context mContext;
    private RecyclerView recyclerView;
    private List<User> mUsers;
    private Map<User, Long> lastMessageTimes;
    private Map<DatabaseReference, ValueEventListener> listeners;

    FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes");

    public static int CHATS = 0, SEARCH_USERS = 1, FRIEND_REQUESTS = 2;
    private int pageType;

    public UserAdapter(Context mContext, List<User> mUsers, int pageType){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.lastMessageTimes = new HashMap<>();
        this.listeners = new HashMap<>();
        this.pageType = pageType;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(isChat())
            view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        else
            view = LayoutInflater.from(mContext).inflate(R.layout.user_item2, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (isChat()){
            observeLastMessage(user, holder);
        } else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat()){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
        if(isChat())
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
                }
            });
        else{
            holder.ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(pageType == SEARCH_USERS) {
                        reference.child("YourLikes").child(fuser.getUid()).child(user.getId()).setValue(0);
                        reference.child("YouWereLikedBy").child(user.getId()).child(fuser.getUid()).setValue(0);
                    }
                    else{
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.putExtra("userid", user.getId());
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    private boolean isChat(){
        return pageType == CHATS;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton ok;
        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;
        @SuppressLint("ResourceAsColor")
        public UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            ok = itemView.findViewById(R.id.ok);
        }
    }

    private void observeLastMessage(final User user, final UserViewHolder holder){
        //Log.d("oneworld", "user name to observe: " + user.getUsername());
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final DatabaseReference chatReference = User.getChatBetween(firebaseUser.getUid(), user.getId());
        final ValueEventListener chatListener = new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = null;
                long lastMessageTime = 0;
                String lasttime = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat != null) {
                        theLastMessage = chat.getMessage();
                        lastMessageTime = chat.getExactTime();
                        lasttime = chat.getTime();
                    }
                }

                if(theLastMessage != null) {
                    holder.last_msg.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
                    if(theLastMessage.length() > 30){
                        theLastMessage = theLastMessage.substring(0, 30) + "...";
                    }
                    holder.last_msg.setText(theLastMessage + " (" + lasttime + ")");
                }
                else {
                    holder.last_msg.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
                    holder.last_msg.setText("No Messages");
                }

                // Мы хотим, чтобы пользователи были отсортированы
                int oldPosition = mUsers.indexOf(user);
                mUsers.remove(user);
                int position;
                for(position = mUsers.size() - 1; position >= 0; position--){
                    User otherUser = mUsers.get(position);
                    if(lastMessageTimes.containsKey(otherUser)
                            && lastMessageTimes.get(otherUser) >= lastMessageTime)
                        break;
                }
                position++;
                if(position == mUsers.size() && position != 0)
                    position--;
                mUsers.add(position, user);
                if(recyclerView.getItemAnimator() == null && allUsersHaveTimes())
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                notifyItemMoved(oldPosition, position);
                lastMessageTimes.put(user, lastMessageTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        listeners.put(chatReference, chatListener);
        chatReference.limitToLast(1).addValueEventListener(chatListener);
    }

    private boolean allUsersHaveTimes(){
        for(User user : mUsers){
            if(!lastMessageTimes.containsKey(user))
                return false;
        }
        return true;
    }


    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.setItemAnimator(null);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        for(Map.Entry<DatabaseReference, ValueEventListener> kv : listeners.entrySet()){
            kv.getKey().removeEventListener(kv.getValue());
        }
    }
}
