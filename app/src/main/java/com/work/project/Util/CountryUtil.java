package com.work.project.Util;

import android.content.res.Resources;

import com.work.project.R;

public class CountryUtil {
    public static final int UK = 0, RUSSIA = 1, GERMANY = 2, SPAIN = 3, FRANCE = 4, ITALY = 5, CHINA = 6, USA = 7;

    public static String getLongCountryString(Resources res, int countryId){
        switch(countryId){
            case RUSSIA:
                return res.getString(R.string.russia);
            case UK:
                return res.getString(R.string.uk);
            case GERMANY:
                return res.getString(R.string.germany);
            case SPAIN:
                return res.getString(R.string.spain);
            case FRANCE:
                return res.getString(R.string.france);
            case ITALY:
                return res.getString(R.string.italy);
            case CHINA:
                return res.getString(R.string.china);
            case USA:
                return res.getString(R.string.usa);
            default:
                return res.getString(R.string.unknown_country);
        }
    }

    public static int getCountryDrawable(int countryId){
        switch(countryId){
            case RUSSIA:
                return R.drawable.russia;
            case UK:
                return R.drawable.uk;
            case GERMANY:
                return R.drawable.germany;
            case SPAIN:
                return R.drawable.spain;
            case FRANCE:
                return R.drawable.france;
            case ITALY:
                return R.drawable.italy;
            case CHINA:
                return R.drawable.china;
            case USA:
                return R.drawable.usa;
            default:
                // будем считать, что чел из сша, если не знаем страну
                return R.drawable.usa;
        }
    }

    private CountryUtil(){}
}
