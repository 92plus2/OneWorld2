package com.work.project.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.work.project.R;

import java.util.ArrayList;

public class LanguageAdapter extends ArrayAdapter<com.work.project.Model.LanguageItem> {
    public LanguageAdapter(Context context, ArrayList<com.work.project.Model.LanguageItem> languageList) {
        super(context, 0, languageList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.language_spinner_row, parent, false
            );
        }
        ImageView imageViewFlag = convertView.findViewById(R.id.image_view_flag);
        TextView textViewName = convertView.findViewById(R.id.text_view_name);
        com.work.project.Model.LanguageItem currentItem = getItem(position);
        if (currentItem != null) {
            imageViewFlag.setImageResource(currentItem.getFlagImage());
            textViewName.setText(currentItem.getLanguageName());
        }
        return convertView;
    }
}