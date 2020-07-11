package com.work.project.Model;

import java.util.Objects;

public class LanguageItem {
    private String languageName;
    private int languageId;
    private int flagImage;

    public LanguageItem(int languageId) {
        this.languageId = languageId;
        languageName = LanguageUtil.getShortLanguageString(languageId);
        flagImage = LanguageUtil.getLanguageDrawable(languageId);
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