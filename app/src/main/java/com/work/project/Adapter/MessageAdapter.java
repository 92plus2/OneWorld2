package com.work.project.Adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.work.project.MessageActivity;
import com.work.project.Model.Chat;
import com.work.project.R;
import com.work.project.Util.Translator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String avatarUrl;
    private String destLanguage;

    public MessageAdapter(Context mContext, List<Chat> mChat, String avatarUrl, String destLanguage){
        this.mChat = mChat;
        this.mContext = mContext;
        this.avatarUrl = avatarUrl;
        this.destLanguage = destLanguage;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        if(viewType == MSG_TYPE_LEFT)
            layoutId = R.layout.chat_item_left;
        else
            layoutId = R.layout.chat_item_right;

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull MessageViewHolder holder) {
        holder.onRecycled();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final Chat chat = mChat.get(position);
            if (holder.time != null) {
                if (holder.isLeftMessage()) {
                    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime date = LocalTime.parse(chat.getTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    String time = timeFormat.format(date.plusHours(Calendar.getInstance().get(Calendar.ZONE_OFFSET) - chat.getZoneOffset()));
                    holder.time.setText(time);
                } else {
                    holder.time.setText(chat.getTime());
                }
        }
        holder.messageText.setText(chat.getMessage());

        if (avatarUrl.equals("default")) {
            holder.profilePicture.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            Glide.with(mContext).load(avatarUrl).into(holder.profilePicture);
        }

        if (holder.isRightMessage()) {
            holder.txtSeen.setVisibility(View.VISIBLE);
            if (chat.isSeen()) {
                holder.txtSeen.setText(R.string.message_seen);
            } else {
                holder.txtSeen.setText(R.string.message_delivered);
            }
        }
        else {
            holder.txtSeen.setVisibility(View.GONE);
        }

        if (chat.getPhoto() != null) {
            holder.messagePhoto.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.GONE);
            int height = (int) convertDpToPixel(200, holder.time.getContext());
            setHeight(holder.messagePhoto, height);

            Glide.with(mContext).load(chat.getPhotoCached()).into(holder.messagePhoto);

            if (holder.isLeftMessage()) {
                holder.clickToTranslate.setVisibility(View.GONE);
            }

            holder.messagePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBigPhoto(holder.messageText.getContext(), chat.getPhotoCached());
                }
            });
        }
        else {
            holder.messagePhoto.setVisibility(View.GONE);
            setHeight(holder.messagePhoto, 0);
            holder.messageText.setVisibility(View.VISIBLE);
            if (holder.isLeftMessage()) {
                holder.clickToTranslate.setVisibility(View.VISIBLE);
                holder.clickToTranslate.setText(R.string.click_to_translate);
                holder.clickToTranslate.setTextSize(12);
            }
        }

        // распологаем time правильно
        if(holder.isRightMessage()) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) holder.time.getLayoutParams();
            View alignView = chat.getPhoto() == null? holder.messageText : holder.photoLayout;
            params.addRule(RelativeLayout.LEFT_OF, alignView.getId());
        }

        // добавляем callback для перевода
        if(holder.isLeftMessage()){
            View.OnClickListener translationListener = v -> {
                translate(holder);
                setClickListenerRecursively(holder.messageLayout, null);
            };
            setClickListenerRecursively(holder.messageLayout, translationListener);
        }
        else
            setClickListenerRecursively(holder.messageLayout, null);
    }

    private static void setClickListenerRecursively(View view, View.OnClickListener listener){
        if(view instanceof ImageView)
            return;
        if(view instanceof TextView) {
            view.setClickable(true);
            view.setFocusable(false);
        }
        view.setOnClickListener(listener);

        if(view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;
            for(int i = 0; i < viewGroup.getChildCount(); i++){
                View child = viewGroup.getChildAt(i);
                setClickListenerRecursively(child, listener);
            }
        }
    }


    private void setHeight(View view, int height){
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public void showBigPhoto(Context context, Uri uri){
        MessageActivity activity = (MessageActivity) context;
        activity.bigPhotoLayout.setVisibility(View.VISIBLE);
        activity.bigPhotoLayout.setAlpha(0.0f);
        // Плавно показываем наш layout
        activity.bigPhotoLayout.animate()
                .alpha(1.0f)
                .setListener(null);

        Glide.with(context).load(uri).into(activity.bigPhotoView);
    }

    public static class MessageViewHolder extends Translator.ValidatableHolder{
        public TextView messageText;
        public TextView time;

        public ImageView profilePicture;
        public TextView txtSeen;
        public ImageView messagePhoto;
        public RelativeLayout photoLayout;
        public RelativeLayout messageLayout;
        public TextView clickToTranslate;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.time);
            clickToTranslate = itemView.findViewById(R.id.clickPlusToTranslate);
            profilePicture = itemView.findViewById(R.id.profile_image);
            txtSeen = itemView.findViewById(R.id.txt_seen);
            messagePhoto = itemView.findViewById(R.id.message_photo);
            photoLayout = itemView.findViewById(R.id.photoLayout);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }

        public boolean isRightMessage(){
            return clickToTranslate == null;
        }

        public boolean isLeftMessage(){
            return !isRightMessage();
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = fuser.getUid();
        Chat chat = mChat.get(position);
        if (chat.getSender().equals(currentUserId))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

    private void translate(final MessageViewHolder holder) {
        String translationText = holder.messageText.getText().toString();
        String language = destLanguage.toLowerCase();
        Translator.translate(translationText, language,  new Translator.TranslateCallback() {
            @Override
            public void onTranslationSuccess(String translatedText) {
                holder.clickToTranslate.setSingleLine(false);
                holder.clickToTranslate.setTextSize(18);
                holder.clickToTranslate.setText("(" + translatedText + ")");
            }

            @Override
            public void onTranslationSameLanguage() {
                holder.clickToTranslate.setText(mContext.getString(R.string.same_lang));
            }

            @Override
            public int getValidationId() {
                return holder.getValidationId();
            }
        });
    }

}