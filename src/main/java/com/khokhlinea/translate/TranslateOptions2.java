package com.khokhlinea.translate;

import java.io.Serializable;

public class TranslateOptions2 implements Serializable {
    private String key;
    private String text;
    private String lang;

    public TranslateOptions2() {
    }

    public TranslateOptions2(String key, String text, String lang) {

        this.key = key;
        this.text = text;
        this.lang = lang;
    }

    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }

    public String getLang() {
        return lang;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
