package com.khokhlinea.translate.connection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khokhlinea.translate.database.SQLiteDB;
import com.khokhlinea.translate.exeptions.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class MyConnection {
    private Properties properties;
    private FileInputStream fis;
    private Logger logger;

    public MyConnection() {
        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            properties = new Properties();
            properties.load(fis);
            logger = Logger.getLogger(MyConnection.class.getName());
        } catch (IOException e) {
            logger.warning("Can't read config file");
            closeConnections();
        }
    }

    private String connect(String text, String from, String to) {
        String key = properties.getProperty("apiKey");
        List<String> textArray = new ArrayList<>(Arrays.asList(text.split(" ")));
        List<String> translatedList = new ArrayList<>();

        for (String wordForTranslate : textArray) {
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme("https").host("translate.yandex.net").path("/api/v1.5/tr.json/translate")
                    .queryParam("key", key).queryParam("text", wordForTranslate)
                    .queryParam("lang", from + "-" + to).build();
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> result = null;
            try {
                result = restTemplate.getForEntity(uriComponents.toString(), String.class);
            } catch (RestClientException e) {
                e.printStackTrace();
                throw new BadRequestException("Bad request!!");
            }
            if (result.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode root = mapper.readTree(result.getBody());
                    JsonNode resp_text = root.path("text");
                    for (Iterator<JsonNode> it = resp_text.iterator(); it.hasNext(); ) {
                        translatedList.add(it.next().asText());
                    }
                    SQLiteDB.getInstance().connect();
                    SQLiteDB.getInstance().addDataToTable(wordForTranslate, from, to);
                    closeConnections();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (result.getStatusCode() == HttpStatus.BAD_REQUEST) {
                System.out.println("Bad request!");
                throw new BadRequestException("Bad request!!");
            }
        }
        StringBuilder translatedString = new StringBuilder();
        translatedList.forEach(word -> translatedString.append(word).append(" "));
        return translatedString.toString().trim();
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
