package com.work.project.Util;

import android.content.res.Resources;

import com.work.project.R;

public class CountryUtil {
    public static final int UK = 0, RUSSIA = 1, GERMANY = 2, SPAIN = 3, FRANCE = 4, ITALY = 5, CHINA = 6, USA = 7, CANADA = 8, BRAZIL = 9, INDIA = 10,
            JAPAN = 11, ISRAEL = 12, AUSTRALIA = 13, FINLAND = 14, SWEDEN = 15, POLAND = 16, ARGENTINA = 17, AUSTRIA = 18, BELGIUM = 19, CHAD = 20,
            CHILE = 21, COLOMBIA = 22, CZECH_REPUBLIC = 23, DENMARK = 24, GEORGIA = 25, GREECE = 26, HONG_KONG = 27, INDONESIA = 28, IRELAND = 29,
            JAMAICA = 30, MALAYSIA = 31, MEXICO = 32, NETHERLANDS = 33, NORWAY = 34, PERU = 35, PHILIPPINES = 36, PORTUGAL = 37, SINGAPORE = 38,
            SOUTH_AFRICA = 39, SOUTH_KOREA = 40, SWITZERLAND = 41, TAIWAN = 42, THAILAND = 43, TURKEY = 44, UKRAINE = 45, UNITED_ARAB_EMIRATES = 46, VIETNAM = 47;

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
            case CANADA:
                return res.getString(R.string.canada);
            case BRAZIL:
                return res.getString(R.string.brazil);
            case INDIA:
                return res.getString(R.string.india);
            case JAPAN:
                return res.getString(R.string.japan);
            case ISRAEL:
                return res.getString(R.string.israel);
            case AUSTRALIA:
                return res.getString(R.string.australia);
            case FINLAND:
                return res.getString(R.string.finland);
            case SWEDEN:
                return res.getString(R.string.sweden);
            case POLAND:
                return res.getString(R.string.poland);
            case ARGENTINA:
                return res.getString(R.string.argentina);
            case AUSTRIA:
                return res.getString(R.string.austria);
            case BELGIUM:
                return res.getString(R.string.belgium);
            case CHAD:
                return res.getString(R.string.chad);
            case CHILE:
                return res.getString(R.string.chile);
            case COLOMBIA:
                return res.getString(R.string.colombia);
            case CZECH_REPUBLIC:
                return res.getString(R.string.czech_republic);
            case DENMARK:
                return res.getString(R.string.denmark);
            case GEORGIA:
                return res.getString(R.string.georgia);
            case GREECE:
                return res.getString(R.string.greece);
            case HONG_KONG:
                return res.getString(R.string.hong_kong);
            case INDONESIA:
                return res.getString(R.string.indonesia);
            case IRELAND:
                return res.getString(R.string.ireland);
            case JAMAICA:
                return res.getString(R.string.jamaica);
            case MALAYSIA:
                return res.getString(R.string.malaysia);
            case MEXICO:
                return res.getString(R.string.mexico);
            case NETHERLANDS:
                return res.getString(R.string.netherlands);
            case NORWAY:
                return res.getString(R.string.norway);
            case PERU:
                return res.getString(R.string.peru);
            case PHILIPPINES:
                return res.getString(R.string.philippines);
            case PORTUGAL:
                return res.getString(R.string.portugal);
            case SINGAPORE:
                return res.getString(R.string.singapore);
            case SOUTH_AFRICA:
                return res.getString(R.string.south_africa);
            case SOUTH_KOREA:
                return res.getString(R.string.south_korea);
            case SWITZERLAND:
                return res.getString(R.string.switzerland);
            case TAIWAN:
                return res.getString(R.string.taiwan);
            case THAILAND:
                return res.getString(R.string.thailand);
            case TURKEY:
                return res.getString(R.string.turkey);
            case UKRAINE:
                return res.getString(R.string.ukraine);
            case UNITED_ARAB_EMIRATES:
                return res.getString(R.string.united_arab_emirates);
            case VIETNAM:
                return res.getString(R.string.vietnam);
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
            case CANADA:
                return R.drawable.canada;
            case BRAZIL:
                return R.drawable.brazil;
            case INDIA:
                return R.drawable.india;
            case JAPAN:
                return R.drawable.japan;
            case ISRAEL:
                return R.drawable.israel;
            case AUSTRALIA:
                return R.drawable.australia;
            case FINLAND:
                return R.drawable.finland;
            case SWEDEN:
                return R.drawable.sweden;
            case POLAND:
                return R.drawable.poland;
            case ARGENTINA:
                return R.drawable.argentina;
            case AUSTRIA:
                return R.drawable.austria;
            case BELGIUM:
                return R.drawable.belgium;
            case CHAD:
                return R.drawable.chad;
            case CHILE:
                return R.drawable.chile;
            case COLOMBIA:
                return R.drawable.colombia;
            case CZECH_REPUBLIC:
                return R.drawable.czech_republic;
            case DENMARK:
                return R.drawable.denmark;
            case GEORGIA:
                return R.drawable.georgia;
            case GREECE:
                return R.drawable.greece;
            case HONG_KONG:
                return R.drawable.hong_kong;
            case INDONESIA:
                return R.drawable.indonesia;
            case IRELAND:
                return R.drawable.ireland;
            case JAMAICA:
                return R.drawable.jamaica;
            case MALAYSIA:
                return R.drawable.malaysia;
            case MEXICO:
                return R.drawable.mexico;
            case NETHERLANDS:
                return R.drawable.netherlands;
            case NORWAY:
                return R.drawable.norway;
            case PERU:
                return R.drawable.peru;
            case PHILIPPINES:
                return R.drawable.philippines;
            case PORTUGAL:
                return R.drawable.portugal;
            case SINGAPORE:
                return R.drawable.singapore;
            case SOUTH_AFRICA:
                return R.drawable.south_africa;
            case SOUTH_KOREA:
                return R.drawable.south_korea;
            case SWITZERLAND:
                return R.drawable.switzerland;
            case TAIWAN:
                return R.drawable.taiwan;
            case THAILAND:
                return R.drawable.thailand;
            case TURKEY:
                return R.drawable.turkey;
            case UKRAINE:
                return R.drawable.ukraine;
            case UNITED_ARAB_EMIRATES:
                return R.drawable.united_arab_emirates;
            case VIETNAM:
                return R.drawable.vietnam;
            default:
                // будем считать, что чел из сша, если не знаем страну
                return R.drawable.usa;
        }
    }

    private CountryUtil(){}
}
