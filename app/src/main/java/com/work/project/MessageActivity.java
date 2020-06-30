package com.work.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.work.project.Adapter.MessageAdapter;
import com.work.project.Fragments.APIService;
import com.work.project.Model.Chat;
import com.work.project.Model.User;
import com.work.project.Notifications.Client;
import com.work.project.Notifications.Data;
import com.work.project.Notifications.MyResponse;
import com.work.project.Notifications.Sender;
import com.work.project.Notifications.Token;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    CircleImageView profile_image;
    TextView username;

    FirebaseUser currentUser;
    String currentUserId;
    String otherUserId;
    DatabaseReference chats;
    ValueEventListener chatsListener;
    DatabaseReference otherUserRef;

    ImageButton btn_send;
    ImageButton photo_but;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    Map<Chat, Integer> chatMap;

    RecyclerView recyclerView;


    APIService apiService;

    boolean notify = false;

    private final static String TAG = "oneworld";
    private static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");



        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        photo_but = findViewById(R.id.btn_img);
        text_send = findViewById(R.id.text_send);

        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("userid");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();

        photo_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                final String msg = text_send.getText().toString();

                if (!msg.equals("")){
                    Date currentDate = new Date();
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String time = timeFormat.format(currentDate);
                    sendMessage(msg, time);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });


        otherUserRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);

        otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                startReadingMessages(user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    private void sendMessage(String message, String time){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", currentUserId);
        hashMap.put("receiver", otherUserId);
        hashMap.put("message", message);
        hashMap.put("seen", false);
        hashMap.put("time", time);
        hashMap.put("exactTime", System.currentTimeMillis());
        hashMap.put("photo", null);
        chats.push().setValue(hashMap);


        // add user to chat fragment
        final DatabaseReference ourChatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentUserId)
                .child(otherUserId);

        ourChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    ourChatRef.child("id").setValue(otherUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        final DatabaseReference receiverChatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(otherUserId)
                .child(currentUserId);
        receiverChatRef.child("id").setValue(currentUserId);

        if (notify) {
            sendNotification(otherUserId, username.getText().toString(), message);
        }
    }

    private void sendNotification(String receiver, final String sender, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(currentUserId, R.mipmap.ic_launcher, sender+": "+message, "New Message",
                            otherUserId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void startReadingMessages(final String imageurl){
        mChat = new ArrayList<>();
        chatMap = new HashMap<>();
        messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
        recyclerView.setAdapter(messageAdapter);

        chats = User.getChatBetween(currentUserId, otherUserId);
        chatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                // загружаем новые сообщения в фоновом процессе
                if(!isLoadingMessages) {
                    new Thread() {
                        @Override
                        public void run() {
                            loadNewMessages(dataSnapshot);
                        }
                    }.start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        chats.limitToLast(10).addValueEventListener(chatsListener);
    }

    private volatile boolean isLoadingMessages = false;

    // метод для чтения сообщений в фоновом процессе
    private void loadNewMessages(DataSnapshot messages){
        isLoadingMessages = true;

        final List<Chat> newMessages = new ArrayList<>();
        final List<Chat> seenMessages = new ArrayList<>();

        for (DataSnapshot snapshot : messages.getChildren()) {
            Chat chat = snapshot.getValue(Chat.class);

            if (!chatMap.containsKey(chat)) {
                // добавляем новое сообщение
                newMessages.add(chat);
                // прочитаем его, если оно послано нам
                if(chat.getReceiver().equals(currentUserId)) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("seen", true);
                    snapshot.getRef().updateChildren(hashMap);
                }
            }
            else {
                // это старое сообщение. Проверим, прочитали ли его
                int index = chatMap.get(chat);
                Chat ourChat = mChat.get(index);
                if(ourChat.isSeen() != chat.isSeen())
                    seenMessages.add(ourChat);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // добавляем в список новые сообщения
                for (Chat newMessage : newMessages) {
                    mChat.add(newMessage);
                    chatMap.put(newMessage, mChat.size() - 1);
                    messageAdapter.notifyItemInserted(mChat.size() - 1);
                }

                // если надо, помечаем сообщения как прочитанное
                for(Chat message : seenMessages){
                    Log.d(TAG, "seen message: " + message.getMessage());
                    message.setSeen(true);
                    messageAdapter.notifyItemChanged(chatMap.get(message));
                }

                // скроллим в конец
                recyclerView.scrollToPosition(mChat.size() - 1);
                isLoadingMessages = false;
            }
        });
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status){
        DatabaseReference curUserRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        curUserRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(currentUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        currentUser("none");
        chats.removeEventListener(chatsListener);
    }
}
