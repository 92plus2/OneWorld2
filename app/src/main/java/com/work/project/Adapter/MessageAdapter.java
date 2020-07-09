package com.work.project.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.work.project.MessageActivity;
import com.work.project.Model.Chat;
import com.work.project.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;

import cz.msebera.android.httpclient.Header;

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

    public static class Http {
        private static final String BASE_URL = "https://translation.googleapis.com/language/translate/v2?";
        private static final String KEY = "AIzaSyDKDoumFm_ZpPmSIHwNfMJBPsIOeinMAH8";


        private static AsyncHttpClient client = new AsyncHttpClient();


        public static void post(String transText, String destLang, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(transText, destLang), responseHandler);
        }

        private static String makeKeyChunk(String key) {
            return "key=" + KEY;
        }

        private static String makeTransChunk(String transText) {
            String encodedText = URLEncoder.encode(transText);
            return "&q=" + encodedText;
        }

        private static String langDest(String langDest) {
            return "&target=" + langDest;
        }

        private static String getAbsoluteUrl(String transText, String destLang) {
            String apiUrl = BASE_URL + makeKeyChunk(KEY) + makeTransChunk(transText) + langDest(destLang);
            //Log.d(MessageActivity.TAG, "url: " + apiUrl);
            return apiUrl;
        }
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final Chat chat = mChat.get(position);
        if (holder.time != null) {
            holder.time.setText(chat.getTime());
            holder.time.setTextSize(11);
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
                holder.txtSeen.setText("Seen");
            } else {
                holder.txtSeen.setText("Delivered");
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
            Glide.with(mContext).load(chat.getPhoto()).into(holder.messagePhoto);
            if (holder.isLeftMessage()) {
                holder.clickToTranslate.setVisibility(View.GONE);
            }

            holder.messagePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBigPhoto(holder.messageText.getContext(), chat.getPhoto());
                }
            });
        }
        else {
            holder.messagePhoto.setVisibility(View.GONE);
            setHeight(holder.messagePhoto, 0);
            holder.messageText.setVisibility(View.VISIBLE);
            if (holder.isLeftMessage()) {
                holder.clickToTranslate.setVisibility(View.VISIBLE);
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
            View.OnClickListener translationListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(MessageActivity.TAG, "on message click!");
                    translate(holder);
                    setClickListenerRecursively(holder.messageLayout, null);
                }
            };
            setClickListenerRecursively(holder.messageLayout, translationListener);
        }
        else
            setClickListenerRecursively(holder.messageLayout, null);
    }

    private static void setClickListenerRecursively(View view, View.OnClickListener listener){
        view.setOnClickListener(listener);
        if(view instanceof TextView) {
            view.setClickable(true);
            view.setFocusable(true);
        }

        if(view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;
            for(int i = 0; i < viewGroup.getChildCount(); i++){
                View child = viewGroup.getChildAt(i);
                setClickListenerRecursively(child, listener);
            }
        }
    }

    private void translate(final MessageViewHolder holder) {
        String language = destLanguage.toLowerCase();
        String translationString = holder.messageText.getText().toString();
        Http.post(translationString, language, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    JSONObject jsonObject = serverResp.getJSONObject("data");
                    JSONArray transObject = jsonObject.getJSONArray("translations");
                    JSONObject transObject2 = transObject.getJSONObject(0);
                    String translatedText = transObject2.getString("translatedText");
                    holder.clickToTranslate.setSingleLine(false);
                    holder.clickToTranslate.setTextSize(18);
                    holder.clickToTranslate.setText("(" + translatedText + ")");
                } catch (JSONException e) {
                    Log.d(MessageActivity.TAG, "error on translate: ", e);
                }
            }
        });
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

    public void showBigPhoto(Context context, String url){
        MessageActivity activity = (MessageActivity) context;
        activity.bigPhotoLayout.setVisibility(View.VISIBLE);
        activity.bigPhotoLayout.setAlpha(0.0f);
        // Плавно показываем наш layout
        activity.bigPhotoLayout.animate()
                .alpha(1.0f)
                .setListener(null);

        Glide.with(context).load(url).into(activity.bigPhotoView);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{

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
}