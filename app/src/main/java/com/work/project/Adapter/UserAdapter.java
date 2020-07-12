package com.work.project.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import com.work.project.Model.CountryUtil;
import com.work.project.Model.LanguageUtil;
import com.work.project.Model.User;
import com.work.project.Notifications.Data;
import com.work.project.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context mContext;
    private RecyclerView recyclerView;
    private List<User> mUsers;
    private Map<User, Long> lastMessageTimes;
    private Map<RecyclerView.ViewHolder, Pair<DatabaseReference, ValueEventListener>> listeners;

    FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
    private String fuserName;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes");

    public static int CHATS = 0, SEARCH_USERS = 1, FRIEND_REQUESTS = 2;
    private int pageType;

    public UserAdapter(Context mContext, List<User> mUsers, int pageType){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.lastMessageTimes = new HashMap<>();
        this.listeners = new HashMap<>();
        this.pageType = pageType;

        // получаем имя пользователя
        User.getCurrentUserReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                fuserName = user.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(isChat())
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_user_item, parent, false);
        else
            view = LayoutInflater.from(mContext).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }

        if (isChat()) {
            observeLastMessage(user, holder);
            if (user.getStatus().equals("online")){
                holder.imgOnline.setVisibility(View.VISIBLE);
                holder.imgOffline.setVisibility(View.GONE);
            } else {
                holder.imgOnline.setVisibility(View.GONE);
                holder.imgOffline.setVisibility(View.VISIBLE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
                }
            });
        } else {
            Resources res = mContext.getResources();

            String languageText;
            String languageName = LanguageUtil.getLongLanguageString(res, user.getLanguageID());
            switch (user.getGenderID()){
                case User.MALE:
                    languageText = res.getString(R.string.he_knows_language, languageName);
                    break;
                case User.FEMALE:
                    languageText = res.getString(R.string.she_knows_language, languageName);
                    break;
                default:
                    languageText = res.getString(R.string.username_knows_language, user.getUsername(), languageName);
                    break;
            }
            holder.language.setText(languageText);
            holder.langImg.setImageResource(LanguageUtil.getLanguageDrawable(user.getLanguageID()));

            String countryName = CountryUtil.getLongCountryString(res, user.getCountryID());
            String countryText;
            switch (user.getGenderID()){
                case User.MALE:
                    countryText = res.getString(R.string.he_is_from_country, countryName);
                    break;
                case User.FEMALE:
                    countryText = res.getString(R.string.she_is_from_country, countryName);
                    break;
                default:
                    countryText = res.getString(R.string.username_is_from_country, user.getUsername(), countryName);
                    break;
            }
            holder.country.setText(countryText);
            holder.countryImg.setImageResource(CountryUtil.getCountryDrawable(user.getCountryID()));

            holder.biographyScrollView.setOnTouchListener((view, event) -> {
                // Чтобы можно было скроллить биографию, т. к. RecyclerView перехватывает touchEvent
                if(holder.biography.getHeight() > holder.biographyScrollView.getHeight()) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
                else
                    return false;
            });

            if(user.getBiography() != null && !user.getBiography().isEmpty()) {
                holder.biography.setText(user.getBiography());

                // загружаем язык нашего пользователя и переводим
                DatabaseReference curUserRef = User.getCurrentUserReference().child("languageId");
                curUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int languageId = 0;
                        if(dataSnapshot.exists())
                             languageId = (Integer) dataSnapshot.getValue();

                        String language = LanguageUtil.getShortLanguageString(languageId);

                        MessageAdapter.translate(user.getBiography(), language, mContext, translatedText -> {
                            holder.biography.setText(user.getBiography() + "\n(" + translatedText + ")");
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            holder.likeButton.setOnClickListener(view -> {
                if(pageType == SEARCH_USERS) {  // мы лайкаем человека
                    reference.child("YourLikes").child(fuser.getUid()).child(user.getId()).setValue(0);
                    reference.child("YouWereLikedBy").child(user.getId()).child(fuser.getUid()).setValue(0);
                    // посылаем ему уведомление, что его лайкнули
                    MessageActivity.sendNotification(fuser.getUid(), fuserName, user.getId(), Data.NEW_FRIEND_REQUEST);
                }
                else{  // мы принимаем заявку в друзья
                    // удаляем все лайки
                    reference.child("YourLikes").child(fuser.getUid()).child(user.getId()).removeValue();
                    reference.child("YourLikes").child(user.getId()).child(fuser.getUid()).removeValue();
                    reference.child("YouWereLikedBy").child(user.getId()).child(fuser.getUid()).removeValue();
                    reference.child("YouWereLikedBy").child(fuser.getUid()).child(user.getId()).removeValue();
                    // посылаем уведомление пользователю
                    MessageActivity.sendNotification(fuser.getUid(), fuserName, user.getId(), Data.FRIEND_REQUEST_ACCEPTED);
                    // открываем диалог
                    Intent intent = new Intent(mContext, MessageActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
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
        // chat_user_item and user_card
        public TextView username;
        public ImageView profileImage;
        // chat_user_item
        private TextView lastMsg;
        private ImageView imgOnline;
        private ImageView imgOffline;
        // user_card
        private final ImageButton likeButton;
        public TextView biography;
        public TextView language;
        public TextView country;
        public ImageView langImg;
        public ImageView countryImg;
        public ScrollView biographyScrollView;

        @SuppressLint("ResourceAsColor")
        public UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            imgOnline = itemView.findViewById(R.id.img_online);
            imgOffline = itemView.findViewById(R.id.img_offline);
            biography = itemView.findViewById(R.id.user_biography);
            lastMsg = itemView.findViewById(R.id.last_msg);
            likeButton = itemView.findViewById(R.id.like_button);
            language = itemView.findViewById(R.id.language);
            country = itemView.findViewById(R.id.country);
            langImg = itemView.findViewById(R.id.lang_img);
            countryImg = itemView.findViewById(R.id.country_img);
            biographyScrollView = itemView.findViewById(R.id.biography_scrollview);
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
                    holder.lastMsg.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
                    if(theLastMessage.length() > 30){
                        theLastMessage = theLastMessage.substring(0, 30) + "...";
                    }
                    holder.lastMsg.setText(mContext.getResources().getString(R.string.message_and_time_format, theLastMessage, lasttime));
                }
                else {
                    holder.lastMsg.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
                    holder.lastMsg.setText(R.string.no_messages);
                }

                // Мы хотим, чтобы пользователи были отсортированы
                int oldPosition = mUsers.indexOf(user);
                if(oldPosition == -1){
                    chatReference.removeEventListener(this);
                    return;
                }
                mUsers.remove(user);
                int position;
                for(position = mUsers.size() - 1; position >= 0; position--){
                    User otherUser = mUsers.get(position);
                    if(lastMessageTimes.containsKey(otherUser)
                            && lastMessageTimes.get(otherUser) >= lastMessageTime)
                        break;
                }
                position++;
                mUsers.add(position, user);
                if(recyclerView.getItemAnimator() == null && allUsersHaveTimes())
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                notifyItemMoved(oldPosition, position);
                lastMessageTimes.put(user, lastMessageTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        listeners.put(holder, new Pair<>(chatReference, chatListener));
        chatReference.limitToLast(1).addValueEventListener(chatListener);
    }


    @Override
    public void onViewRecycled(@NonNull UserViewHolder holder) {
        super.onViewRecycled(holder);
        if(listeners.containsKey(holder)){
            Pair<DatabaseReference, ValueEventListener> pair = listeners.remove(holder);
            pair.first.removeEventListener(pair.second);
        }
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
        for(Pair<DatabaseReference, ValueEventListener> pair : listeners.values()){
            pair.first.removeEventListener(pair.second);
        }
    }
}
