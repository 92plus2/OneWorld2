package com.work.project.Util;

import android.content.res.Resources;

import com.work.project.R;

public class LanguageUtil {
    public static final int ENGLISH = 0, RUSSIAN = 1, GERMAN = 2, SPANISH = 3, FRENCH = 4, ITALIAN = 5, CHINIZE = 6, JAPANESE = 7, PORTUGUESE = 8, TURKISH = 9, KOREAN = 10;

    public static String getShortLanguageString(int languageId){  // также используется в Google Translate!
        switch(languageId){
            case RUSSIAN:
                return "RU";
            case ENGLISH:
                return "EN";
            case GERMAN:
                return "DE";
            case SPANISH:
                return "ES";
            case FRENCH:
                return "FR";
            case ITALIAN:
                return "IT";
            case CHINIZE:
                return "ZH";
            case JAPANESE:
                return "JA";
            case PORTUGUESE:
                return "PT";
            case KOREAN:
                return "KO";
            case TURKISH:
                return "TR";
            default:
                // неизвестный язык - считаем, что английский
                return "EN";
        }
    }

    public static String getLongLanguageString(Resources res, int languageId){
        switch(languageId){
            case RUSSIAN:
                return res.getString(R.string.russian);
            case ENGLISH:
                return res.getString(R.string.english);
            case GERMAN:
                return res.getString(R.string.german);
            case SPANISH:
                return res.getString(R.string.spanish);
            case FRENCH:
                return res.getString(R.string.french);
            case ITALIAN:
                return res.getString(R.string.italian);
            case CHINIZE:
                return res.getString(R.string.chinese);
            case JAPANESE:
                return res.getString(R.string.japanese);
            case PORTUGUESE:
                return res.getString(R.string.portuguese);
            case KOREAN:
                return res.getString(R.string.korean);
            case TURKISH:
                return res.getString(R.string.turkish);
            default:
                return res.getString(R.string.unknown_language);
        }
    }

    public static int getLanguageDrawable(int languageId){
        switch(languageId){
            case RUSSIAN:
                return R.drawable.russia;
            case ENGLISH:
                return R.drawable.uk;
            case GERMAN:
                return R.drawable.germany;
            case SPANISH:
                return R.drawable.spain;
            case FRENCH:
                return R.drawable.france;
            case ITALIAN:
                return R.drawable.italy;
            case CHINIZE:
                return R.drawable.china;
            case JAPANESE:
                return R.drawable.japan;
            case PORTUGUESE:
                return R.drawable.portugal;
            case KOREAN:
                return R.drawable.south_korea;
            case TURKISH:
                return R.drawable.turkey;
            default:
                // неизвестный язык - считаем, что английский
                return R.drawable.uk;
        }
    }

    private LanguageUtil(){}
}
