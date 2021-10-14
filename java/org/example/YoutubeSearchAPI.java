package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class YoutubeSearchAPI {
    public static String API_KEY = "AIzaSyAcFhsut0fozB4aPmvZoWTUHc5E4ZfRGOM";

    public static String SearchWithKeyword(String keyword) {

        try {
            URL apiURL = new URL("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&order=viewCount&q="
                    + keyword + "&key=" + API_KEY);
            URLConnection yc = apiURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode jsonNode = mapper.readValue(apiURL, JsonNode.class);

            JsonNode part1 = jsonNode.at("/items");

            List<String> values = mapper.readValue(part1.toPrettyString(),List.class );

            return shorten(values.toString());


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return "";
    }

    static String shorten(String str) {
        String result = str.substring(str.indexOf("videoId=") + 8);
        result = result.substring(0, result.indexOf("}"));
        return result;

    }
}
