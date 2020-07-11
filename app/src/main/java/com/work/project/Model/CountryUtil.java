package com.work.project.Model;

import android.content.res.Resources;

import com.work.project.R;

public class CountryUtil {
    public static final int ENGLAND = 0, RUSSIA = 1, GERMANY = 2, SPAIN = 3, FRANCE = 4, ITALY = 5, CHINA = 6, USA = 7;

    public static String getLongCountryString(Resources res, int countryId){
        switch(countryId){
            case RUSSIA:
                return res.getString(R.string.russia);
            case ENGLAND:
                return res.getString(R.string.england);
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
                throw new IllegalArgumentException("Wrong countryId: " + countryId);
        }
    }

    public static int getCountryDrawable(int countryId){
        switch(countryId){
            case RUSSIA:
                return R.drawable.russia;
            case ENGLAND:
                return R.drawable.england;
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
                throw new IllegalArgumentException("Wrong countryId: " + countryId);
        }
    }

    private CountryUtil(){}
}
