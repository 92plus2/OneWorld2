package com.work.project.Model;

import android.content.res.Resources;

import com.work.project.R;

public class CountryUtil {
    public static final int RU = 0, EN = 1, DE = 2, ES = 3, FR = 4, IT = 5, ZH = 6;

    public static String getShortLanguageString(int countryId){  // также используется в Google Translate!
        switch(countryId){
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
                return "Unknown country!";
        }
    }

    public static String getLongCountryString(Resources res, int languageId){
        switch(languageId){
            case RU:
                return res.getString(R.string.russia);
            case EN:
                return res.getString(R.string.england);
            case DE:
                return res.getString(R.string.germany);
            case ES:
                return res.getString(R.string.spain);
            case FR:
                return res.getString(R.string.france);
            case IT:
                return res.getString(R.string.italy);
            case ZH:
                return res.getString(R.string.chine);
            default:
                return "Unknown country!";
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
