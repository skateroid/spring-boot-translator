package com.khokhlinea.translate.connection;

import com.khokhlinea.translate.Text;

import com.khokhlinea.translate.database.SQLiteDB;
import com.khokhlinea.translate.exeptions.BadRequestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class TestPostForObject {
    private Properties properties;
    private FileInputStream fis;
    private Logger logger;

    public TestPostForObject() {
        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            properties = new Properties();
            properties.load(fis);
            logger = Logger.getLogger(com.khokhlinea.translate.connection.MyConnection.class.getName());
        } catch (IOException e) {
            logger.warning("Can't read config file");
            closeConnections();
        }
    }

    private String connect(String text, String from, String to) {
        String key = properties.getProperty("apiKey");
        List<String> textArray = new ArrayList<>(Arrays.asList(text.split(" ")));
        List<String> translatedList = new ArrayList<>();
        StringBuilder translatedString = new StringBuilder();

        for (String wordForTranslate : textArray) {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("key", key);
            queryParams.put("text", text);
            queryParams.put("lang", from + "-" + to);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme("https").host("translate.yandex.net").path("/api/v1.5/tr.json/translate")
                    .queryParam("key", key).queryParam("text", wordForTranslate)
                    .queryParam("lang", from + "-" + to).build();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Text> response = restTemplate.exchange(uriComponents.toString(), HttpMethod.POST, null, Text.class);

//            Text text1 = restTemplate.exchange(uriComponents.toString(), HttpMethod.POST, null, Text.class);
//            ResponseEntity<String> result = null;
//            try {
//                result = restTemplate.getForEntity(uriComponents.toString(), String.class);
//            } catch (RestClientException e) {
//                e.printStackTrace();
//                throw new BadRequestException("Bad request!!");
//            }
            HttpStatus status = response.getStatusCode();
            if (status == HttpStatus.OK) {
                Text word = response.getBody();
//                translatedList.addAll(word.getText());
                word.getText().forEach(word2 -> translatedString.append(word2).append(" "));
//                translatedList.add(word.getText());
                SQLiteDB.getInstance().connect();
                SQLiteDB.getInstance().addDataToTable(wordForTranslate, from, to);
                closeConnections();

            } else if (status == HttpStatus.BAD_REQUEST) {
                System.out.println("Bad request!");
                throw new BadRequestException("Bad request!!");
            }
        }
//        translatedString = new StringBuilder();
//        translatedList.forEach(word -> translatedString.append(word).append(" "));
        return translatedString.toString().trim();
    }

    public String getTranslatedString(String text, String from, String to) {
        return connect(text, from, to);
    }

    private void closeConnections() {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLiteDB.getInstance().disconnect();
        }
    }
}