package com.khokhlinea.translate.controller;

import com.khokhlinea.translate.database.SQLiteDB;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@RestController
@RequestMapping("translate")
public class TranslateController {
    private Properties properties;
    private FileInputStream fis;
    private SQLiteDB db;

    public TranslateController() {
        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            properties = new Properties();
            properties.load(fis);
            db = new SQLiteDB();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping
    public String translate(@RequestParam(value = "text") String text,
                                   @RequestParam(value = "from") String from,
                                   @RequestParam(value = "to") String to) throws IOException {
        String apiKey = properties.getProperty("apiKey");
        StringBuilder urlString = new StringBuilder("https://translate.yandex.net/api/v1.5/tr.json/translate?key=").append(apiKey);
        URL urlObject = new URL(urlString.toString());

        List<String> text_array = new ArrayList<>(Arrays.asList(text.split(" ")));
        List<String> translated_array = new ArrayList<>();

        for (String word_for_translate : text_array) {
            HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            StringBuilder stringForOutputStream = new StringBuilder();
            stringForOutputStream.append("text=").append(URLEncoder.encode(word_for_translate, "UTF-8")).append("&lang=").append(from).append("-").append(to);
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream()); //создаю объект, который будет писать в поток
            dataOutputStream.writeBytes(stringForOutputStream.toString());

            InputStream response = connection.getInputStream();
            String responseString = new Scanner(response).nextLine();
            int start_index = responseString.indexOf("["); //нахожу начало переведенной строки и её конец
            int end_index = responseString.indexOf("]");
            String translated_word = responseString.substring(start_index + 2, end_index - 1); //сам перевод
            translated_array.add(translated_word);
            db.connect();
            db.addDataToTable(word_for_translate, from, to);
            closeConnections();
        }
//        db.addDataToTable(text, from, to);  //либо так, либо в цикле по слову в базу добавлять, уточнений в ТЗ нет по этому моменту
        StringBuilder translated_string = new StringBuilder();
        translated_array.stream().forEach(word -> translated_string.append(word).append(" "));

        return translated_string.toString().trim();
    }

    private void closeConnections(){
        try {
            db.disconnect();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}