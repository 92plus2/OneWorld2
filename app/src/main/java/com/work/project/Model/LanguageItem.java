package com.work.project.Model;
public class LanguageItem {
    private String mLanguageName;
    private int mFlagImage;
    public LanguageItem(String languageName, int flagImage) {
        mLanguageName = languageName;
        mFlagImage = flagImage;
    }
    public String getLanguageName() {
        return mLanguageName;
    }
    public int getFlagImage() {
        return mFlagImage;
    }
}