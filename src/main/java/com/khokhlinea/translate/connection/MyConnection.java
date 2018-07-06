package com.khokhlinea.translate.connection;

import com.khokhlinea.translate.database.SQLiteDB;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
        String apiKey = properties.getProperty("apiKey");
        StringBuilder urlString = new StringBuilder("https://translate.yandex.net/api/v1.5/tr.json/translate?key=").append(apiKey);
        URL urlObject = null;
        try {
            urlObject = new URL(urlString.toString());
        } catch (MalformedURLException e) {
            logger.warning("Can't create URL Object");
            closeConnections();
        }

        List<String> text_array = new ArrayList<>(Arrays.asList(text.split(" ")));
        List<String> translated_array = new ArrayList<>();

        for (String word_for_translate : text_array) {
            InputStream response = null;
            try {
                HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                StringBuilder stringForOutputStream = new StringBuilder();
                stringForOutputStream.append("text=").append(URLEncoder.encode(word_for_translate, "UTF-8")).append("&lang=").append(from).append("-").append(to);
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(stringForOutputStream.toString());
                response = connection.getInputStream();
            } catch (IOException e) {
                logger.warning("IO exception");
                closeConnections();
            }
            String responseString = new Scanner(response).nextLine();
            int start_index = responseString.indexOf("["); //нахожу начало переведенной строки и её конец
            int end_index = responseString.indexOf("]");
            String translated_word = responseString.substring(start_index + 2, end_index - 1); //сам перевод
            translated_array.add(translated_word);

            SQLiteDB.getInstance().connect();
            SQLiteDB.getInstance().addDataToTable(word_for_translate, from, to);
            closeConnections();
        }
        //SQLiteDB.getInstance().addDataToTable(text, from, to);  //либо так, либо в цикле по слову в базу добавлять, уточнений в ТЗ нет по этому моменту
        StringBuilder translated_string = new StringBuilder();
        translated_array.stream().forEach(word -> translated_string.append(word).append(" "));

        return translated_string.toString().trim();
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

    public Properties getProperties() {
        return properties;
    }
}
