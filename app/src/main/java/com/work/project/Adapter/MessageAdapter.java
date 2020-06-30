package com.work.project.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.work.project.Model.Chat;
import com.work.project.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl){
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            final Button but = view.findViewById(R.id.plus);
            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (but.getText().equals("+")) {
                        final TextView txt = view.findViewById(R.id.show_message);
                        final String s = txt.getText().toString();
                        final String[] message2 = {s};
                        TranslatorOptions options =
                                new TranslatorOptions.Builder()
                                        .setSourceLanguage(TranslateLanguage.RUSSIAN)
                                        .setTargetLanguage(TranslateLanguage.ENGLISH)
                                        .build();
                        final Translator englishGermanTranslator =
                                Translation.getClient(options);
                        DownloadConditions conditions = new DownloadConditions.Builder()
                                .build();
                        englishGermanTranslator.downloadModelIfNeeded(conditions)
                                .addOnSuccessListener(
                                        new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void v) {
                                                // Model downloaded successfully. Okay to start translating.
                                                // (Set a flag, unhide the translation UI, etc.)
                                                // Toast.makeText(MessageActivity.this, "error1", Toast.LENGTH_SHORT).show();
                                                englishGermanTranslator.translate(s)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<String>() {
                                                                    @Override
                                                                    public void onSuccess(@NonNull String translatedText) {
                                                                        ;
                                                                        message2[0] = translatedText;
                                                                        if (!s.equals("")) {
                                                                            txt.setText(s + "\n" + "(" + message2[0] + ")");
                                                                            but.setText("-");
                                                                        }
                                                                    }
                                                                })
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        // Error.
                                                                        //Toast.makeText(MessageActivity.this, "ERROR2", Toast.LENGTH_SHORT).show();
                                                                        // ...
                                                                    }
                                                                });
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Model couldn’t be downloaded or other internal error.
                                                // ...
                                            }
                                        });
                    }
                }
            });
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {


        Chat chat = mChat.get(position);
        holder.time.setText(chat.getTime());
        holder.time.setTextSize(11);
        holder.show_message.setText(chat.getMessage());

        if (imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }

        if (position == mChat.size()-1){
            if (chat.isSeen()){
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public TextView time;

        public ImageView profile_image;
        public TextView txt_seen;

        public ViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.show_message2);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}