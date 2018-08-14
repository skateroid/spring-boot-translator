package com.khokhlinea.translate;

import org.springframework.http.HttpStatus;

import java.util.List;

public class Text {
    private int code;
    private List<String> text;
    private String lang;

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
