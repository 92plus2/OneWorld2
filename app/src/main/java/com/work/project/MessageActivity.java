package com.work.project;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.work.project.Adapter.MessageAdapter;
import com.work.project.Fragments.APIService;
import com.work.project.Model.Chat;
import com.work.project.Model.User;
import com.work.project.Notifications.Client;
import com.work.project.Notifications.Data;
import com.work.project.Notifications.MyFirebaseMessaging;
import com.work.project.Notifications.MyResponse;
import com.work.project.Notifications.Sender;
import com.work.project.Notifications.Token;
import com.work.project.Util.CountryUtil;
import com.work.project.Util.LanguageUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.res.Resources;

import com.work.project.R;
import com.work.project.Util.Translator;

import static com.work.project.R.drawable;

public class MessageActivity extends AppCompatActivity {
    public static Map<String, Uri> localImageFiles = new HashMap<>();  // если мы загрузили картинку на сервер, но она есть у нас в файле локально
    public static String globalUserChatId = null;  // id пользователя, с которым мы переписываемся.

    CircleImageView profile_image;
    TextView username;
    TextView username2;
    TextView language;
    TextView biography;
    TextView country;
    public ImageView langImg;
    public ImageView countryImg;
    CircleImageView profile_image2;
    String currentUserId;
    String otherUserId;
    DatabaseReference chats;
    ValueEventListener chatsListener;
    boolean hasChatsListener;
    User currentUser;
    DatabaseReference currentUserRef;
    DatabaseReference otherUserRef;

    ImageButton btn_send;
    Button show;
    ImageButton photo_but;
    EditText text_send;
    RelativeLayout bio;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    Map<Chat, Integer> chatIndices;
    StorageReference storageReference;
    RecyclerView recyclerView;

    public LinearLayout bigPhotoLayout;
    public ImageView bigPhotoView;

    private static APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

    boolean notify = false;

    public final static String TAG = "oneworld";
    private final static int MAX_MESSAGES = 300;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    private Object Context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        profile_image2 = findViewById(R.id.profile_image2);
        username = findViewById(R.id.username);
        username2 = findViewById(R.id.username2);
        biography = findViewById(R.id.user_biography);
        btn_send = findViewById(R.id.btn_send);
        show = findViewById(R.id.show_bio);
        bio = findViewById(R.id.info);
        photo_but = findViewById(R.id.btn_img);
        text_send = findViewById(R.id.text_send);
        langImg = findViewById(R.id.lang_img);
        countryImg = findViewById(R.id.country_img);
        language = findViewById(R.id.language);
        country = findViewById(R.id.country);

        bigPhotoLayout = findViewById(R.id.big_photo_layout);
        bigPhotoView = findViewById(R.id.big_photo_image_view);

        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("userid");
        FirebaseUser fCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = fCurrentUser.getUid();

        photo_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bio.getVisibility() == View.GONE) {
                    bio.setVisibility(View.VISIBLE);
                    show.setBackgroundResource(drawable.ic_baseline_arrow_drop_up_24);
                }
                else {
                    bio.setVisibility(View.GONE);
                    show.setBackgroundResource(drawable.ic_baseline_arrow_drop_down_24);
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                final String msg = text_send.getText().toString();

                if (!msg.equals("")){
                    sendMessage(msg, null);
                } else {
                    Toast.makeText(MessageActivity.this, R.string.you_cannot_send_an_empty_message, Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        currentUserRef = User.getReferenceById(currentUserId);
        otherUserRef = User.getReferenceById(otherUserId);

        otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User otherUser = dataSnapshot.getValue(User.class);
                username.setText(otherUser.getUsername());
                if (otherUser.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(otherUser.getImageURL()).into(profile_image);
                }
                username2.setText(otherUser.getUsername() + ", " + otherUser.getAge());
                if (otherUser.getImageURL().equals("default")){
                    profile_image2.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(otherUser.getImageURL()).into(profile_image2);
                }
                Resources res = getResources();
                String languageName = LanguageUtil.getLongLanguageString(res, otherUser.getNewLanguageID());
                String languageText = formatSentence(R.array.username_knows_language, otherUser, languageName);
                language.setText(languageText);
                langImg.setImageResource(LanguageUtil.getLanguageDrawable(otherUser.getNewLanguageID()));

                String countryName = CountryUtil.getLongCountryString(res, otherUser.getNewCountryID());
                String countryText = formatSentence(R.array.username_is_from_country, otherUser, countryName);
                country.setText(countryText);
                countryImg.setImageResource(CountryUtil.getCountryDrawable(otherUser.getNewCountryID()));
                biography.setText(R.string.no_biography);
                if(otherUser.getBiography() != null && !otherUser.getBiography().isEmpty()) {
                    biography.setText(otherUser.getBiography());


                }


                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);
                        startReadingMessages(otherUser.getImageURL(), LanguageUtil.getShortLanguageString(currentUser.getNewLanguageID()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        // add user to chat fragment
        final DatabaseReference ourChatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentUserId)
                .child(otherUserId);

        ourChatRef.child("id").setValue(otherUserId);

        final DatabaseReference receiverChatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(otherUserId)
                .child(currentUserId);
        receiverChatRef.child("id").setValue(currentUserId);
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    private String formatSentence(int arrayId, User user, Object info){
        Resources res = this.getResources();
        String format = res.getStringArray(arrayId)[user.getGenderID()];
        return String.format(format, user.getUsername(), info);
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.uploading_image));
        pd.show();

        if (imageUri != null){
            final  StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) throw Objects.requireNonNull(task.getException());

                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();
                        pd.dismiss();

                        localImageFiles.put(mUri, imageUri);
                        sendMessage("Photo", mUri);
                    } else {
                        Toast.makeText(MessageActivity.this, R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                   // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(this, R.string.upload_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    private void sendMessage(String message, String photoUrl){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", currentUserId);
        hashMap.put("receiver", otherUserId);
        hashMap.put("message", message);
        hashMap.put("seen", false);
        Date currentDate = new Date();
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String time = timeFormat.format(currentDate);
        hashMap.put("time", time);
        hashMap.put("exactTime", System.currentTimeMillis());
        hashMap.put("photo", photoUrl);
        chats.push().setValue(hashMap);

        if (notify) {
            sendNotification(currentUser.getId(), currentUser.getUsername(), otherUserId, Data.NEW_MESSAGE, message);
        }
    }

    public static void sendNotification(String senderId, String senderName, String receiverId, int notificationType){
        sendNotification(senderId, senderName, receiverId, notificationType, null);
    }

    private static void sendNotification(final String senderId, final String senderName, final String receiverId, final int notificationType, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.child(receiverId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Token token = dataSnapshot.getValue(Token.class);
                Data data = new Data(senderId, receiverId, senderName, message, notificationType);

                String tokenString;
                if(token == null || (tokenString = token.getToken()) == null)
                    return;

                Sender sender = new Sender(data, tokenString);

                apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        /*if (response.code() == 200){
                            if (response.body().success != 1){
                                Toast.makeText(MessageActivity.this, "Failed to deliver notification",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }*/
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }


    private void startReadingMessages(final String imageurl, final String destLanguage){
        mChat = new ArrayList<>();
        chatIndices = new HashMap<>();
        messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl, destLanguage);
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
        chats.limitToLast(MAX_MESSAGES).addValueEventListener(chatsListener);
        hasChatsListener = true;
    }

    private volatile boolean isLoadingMessages = false;

    // метод для чтения сообщений в фоновом процессе
    private void loadNewMessages(DataSnapshot messages){
        isLoadingMessages = true;

        final List<Chat> newMessages = new ArrayList<>();
        final List<Chat> seenMessages = new ArrayList<>();

        for (DataSnapshot snapshot : messages.getChildren()) {
            Chat chat = snapshot.getValue(Chat.class);

            if (!chatIndices.containsKey(chat)) {
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
                int index = chatIndices.get(chat);
                Chat ourChat = mChat.get(index);
                if(ourChat.isSeen() != chat.isSeen())
                    seenMessages.add(ourChat);
            }
        }

        runOnUiThread(() -> {
            // добавляем в список новые сообщения
            for (Chat newMessage : newMessages) {
                mChat.add(newMessage);
                chatIndices.put(newMessage, mChat.size() - 1);
                messageAdapter.notifyItemInserted(mChat.size() - 1);
            }

            // если надо, помечаем сообщения как прочитанное
            for(Chat message : seenMessages){
                //Log.d(TAG, "seen message: " + message.getMessage());
                message.setSeen(true);
                messageAdapter.notifyItemChanged(chatIndices.get(message));
            }

            // скроллим в конец
            recyclerView.scrollToPosition(mChat.size() - 1);
            isLoadingMessages = false;
        });
    }

    private void status(String status){
        DatabaseReference curUserRef = User.getCurrentUserReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        curUserRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        if(chats != null && !hasChatsListener) {
            chats.limitToLast(MAX_MESSAGES).addValueEventListener(chatsListener);
            hasChatsListener = true;
        }
        globalUserChatId = otherUserId;
        MyFirebaseMessaging.removeMessageNotificationsFromUser(this, otherUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        if(chats != null)
            chats.removeEventListener(chatsListener);
        hasChatsListener = false;
        globalUserChatId = null;
    }

    public void hideBigPhoto(View view){
        bigPhotoLayout.animate()
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        bigPhotoLayout.setVisibility(View.GONE);
                    }
                });
    }
}
