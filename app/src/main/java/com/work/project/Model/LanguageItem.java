package com.work.project.Model;

import android.content.res.Resources;

import java.util.Objects;

public class LanguageItem {
    private String languageName;
    private int languageId;
    private int flagImage;

    public LanguageItem(Resources res, int languageId, boolean isCountry) {
        this.languageId = languageId;
        if(!isCountry) {
            languageName = LanguageUtil.getLongLanguageString(res, languageId);
            flagImage = LanguageUtil.getLanguageDrawable(languageId);
        }
        else{
            languageName = CountryUtil.getLongCountryString(res, languageId);
            flagImage = CountryUtil.getCountryDrawable(languageId);
        }
    }

    public String getLanguageName() {
        return languageName;
    }

    public int getFlagImage() {
        return flagImage;
    }

    public int getLanguageId() {
        return languageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LanguageItem)) return false;
        LanguageItem that = (LanguageItem) o;
        return languageId == that.languageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageId);
    }
}