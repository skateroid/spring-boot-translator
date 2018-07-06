package com.khokhlinea.translate.controller;

import com.khokhlinea.translate.connection.MyConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("translate")
public class TranslateController {
    private MyConnection myConnection;

    public TranslateController() {
        myConnection = new MyConnection();
    }

    @GetMapping
    public String translate(@RequestParam(value = "text") String text,
                            @RequestParam(value = "from") String from,
                            @RequestParam(value = "to") String to) {
        return myConnection.getTranslatedString(text, from, to);
    }
}