package com.work.project.Model;

import android.content.res.Resources;

import com.work.project.R;

public class LanguageUtil {
    public static final int RU = 0, EN = 1, DE = 2, ES = 3, FR = 4, IT = 5, ZH = 6;

    public static String getShortLanguageString(int languageId){  // также используется в Google Translate!
        switch(languageId){
            case RU:
                return "RU";
            case EN:
                return "EN";
            case DE:
                return "DE";
            case ES:
                return "ES";
            case FR:
                return "FR";
            case IT:
                return "IT";
            case ZH:
                return "ZH";
            default:
                return "Unknown language!";
        }
    }

    public static String getLongLanguageString(Resources res, int languageId){
        switch(languageId){
            case RU:
                return res.getString(R.string.russian);
            case EN:
                return res.getString(R.string.english);
            case DE:
                return res.getString(R.string.german);
            case ES:
                return res.getString(R.string.spanish);
            case FR:
                return res.getString(R.string.french);
            case IT:
                return res.getString(R.string.italian);
            case ZH:
                return res.getString(R.string.chinese);
            default:
                return "Unknown language!";
        }
    }

    public static int getLanguageDrawable(int languageId){
        switch(languageId){
            case RU:
                return R.drawable.russian;
            case EN:
                return R.drawable.english;
            case DE:
                return R.drawable.german;
            case ES:
                return R.drawable.spanish;
            case FR:
                return R.drawable.french;
            case IT:
                return R.drawable.italian;
            case ZH:
                return R.drawable.chinese;
            default:
                return 0;
        }
    }
}
