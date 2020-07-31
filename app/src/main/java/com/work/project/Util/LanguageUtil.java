package com.work.project.Util;

import android.content.res.Resources;

import com.work.project.R;

public class LanguageUtil {
    public static final int ENGLISH = 0, RUSSIAN = 1, GERMAN = 2, SPANISH = 3, FRENCH = 4, ITALIAN = 5, CHINIZE = 6;

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
            default:
                // неизвестный язык - считаем, что английский
                return R.drawable.uk;
        }
    }

    private LanguageUtil(){}
}
