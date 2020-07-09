package com.work.project.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.work.project.Fragments.ProfileFragment;
import com.work.project.MessageActivity;
import com.work.project.Model.Chat;
import com.work.project.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.entity.mime.Header;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String avatarUrl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String avatarUrl){
        this.mChat = mChat;
        this.mContext = mContext;
        this.avatarUrl = avatarUrl;
    }
    public static class Http {
        private static final String BASE_URL = "https://translation.googleapis.com/language/translate/v2?";
        private static final String KEY = "AIzaSyDKDoumFm_ZpPmSIHwNfMJBPsIOeinMAH8";


        private static AsyncHttpClient client = new AsyncHttpClient();


        public static void post(String transText,String sourceLang, String destLang, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(transText, sourceLang, destLang), responseHandler);
        }

        private static String makeKeyChunk(String key) {
            return "key=" + KEY;
        }

        private static String makeTransChunk(String transText) {
            String encodedText = URLEncoder.encode(transText);
            return "&amp;q=" + encodedText;
        }

        private static String langSource(String langSource) {
            return "&amp;source=" + langSource;
        }

        private static String langDest(String langDest) {
            return "&amp;target=" + langDest;

        }

        private static String getAbsoluteUrl(String transText, String sourceLang, String destLang) {
            String apiUrl = BASE_URL + makeKeyChunk(KEY) + makeTransChunk(transText) + langSource(sourceLang) + langDest(destLang);
            return apiUrl;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageViewHolder(view);
        } else {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            final Button but = view.findViewById(R.id.plus);
            final TextView txt = view.findViewById(R.id.show_message);
            final TextView txt2 = view.findViewById(R.id.clickPlusToTranslate);
            if(txt2.getText().toString().equals("click + to translate"))
                but.setText("+");
            else
                but.setText("-");
            but.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    if (but.getText().equals("+")) {
                        final String s = txt.getText().toString();
                        final String[] message2 = {s};
                        final String[] s1 = {"RU"};
                        String s2 = ProfileFragment.language;


                        String translationString = s;
                        Http.post(translationString, "ru", "es", new JsonHttpResponseHandler() {
                            //@Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                                try {
                                    JSONObject serverResp = new JSONObject(response.toString());
                                    JSONObject jsonObject = serverResp.getJSONObject("data");
                                    JSONArray transObject = jsonObject.getJSONArray("translations");
                                    JSONObject transObject2 = transObject.getJSONObject(0);
                                    txt2.setSingleLine(false);
                                    txt2.setTextSize(18);
                                    //txt2.setTextColor(R.color.colorblack);
                                    but.setText("-");
                                    txt2.setText(transObject2.getString("translatedText"));
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }

                        });
                    } else {
                        txt2.setTextSize(12);
                        //txt2.setTextColor(R.color.colorPrimaryDark);
                        txt2.setSingleLine();
                        txt2.setText("click + to translate");
                        but.setText("+");
                    }
                }
            });
            return new MessageViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final Chat chat = mChat.get(position);
        if (holder.time != null) {
            holder.time.setText(chat.getTime());
            holder.time.setTextSize(11);
        }
        holder.showMessage.setText(chat.getMessage());

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
            holder.showMessage.setVisibility(View.GONE);
            int height = (int) convertDpToPixel(200, holder.time.getContext());
            setHeight(holder.messagePhoto, height);
            Glide.with(mContext).load(chat.getPhoto()).into(holder.messagePhoto);
            if (holder.isLeftMessage()) {
                holder.clickPlusToTranslate.setVisibility(View.GONE);
                holder.plus.setVisibility(View.GONE);
            }

            holder.messagePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBigPhoto(holder.showMessage.getContext(), chat.getPhoto());
                }
            });
        }
        else {
            holder.messagePhoto.setVisibility(View.GONE);
            setHeight(holder.messagePhoto, 0);
            holder.showMessage.setVisibility(View.VISIBLE);
            if (holder.isLeftMessage()) {
                holder.clickPlusToTranslate.setVisibility(View.VISIBLE);
                holder.plus.setVisibility(View.VISIBLE);
            }
        }

        // распологаем time правильно
        if(holder.isRightMessage()) {
            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) holder.time.getLayoutParams();
            View alignView = chat.getPhoto() == null? holder.showMessage : holder.photoLayout;
            params.addRule(RelativeLayout.LEFT_OF, alignView.getId());
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

        public TextView showMessage;
        public TextView time;

        public ImageView profilePicture;
        public TextView txtSeen;
        public ImageView messagePhoto;
        public RelativeLayout photoLayout;

        public TextView clickPlusToTranslate;
        public Button plus;

        public MessageViewHolder(View itemView) {
            super(itemView);
            showMessage = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.time);
            clickPlusToTranslate = itemView.findViewById(R.id.clickPlusToTranslate);
            plus = itemView.findViewById(R.id.plus);
            profilePicture = itemView.findViewById(R.id.profile_image);
            txtSeen = itemView.findViewById(R.id.txt_seen);
            messagePhoto = itemView.findViewById(R.id.message_photo);
            photoLayout = itemView.findViewById(R.id.photoLayout);
        }

        public boolean isRightMessage(){
            return clickPlusToTranslate == null;
        }

        public boolean isLeftMessage(){
            return !isRightMessage();
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        Chat chat = mChat.get(position);
        if (chat.getSender().equals(fuser.getUid()))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }
}