package org.example;

///Users/anikethtarikonda/Desktop/better_chad_bot/src/main/KeyTermsToUrl


import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KeyTermsToUrlMap {
    public static final File log = new File("/Users/anikethtarikonda/Desktop/better_chad_bot/src/main/KeyTermsToUrl");

    public static void writeToFile(Map<String, String> values) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(log, true));

            for (Map.Entry<String, String> data : values.entrySet()) {
                String formattedOutput = data.getKey() + "," + data.getValue() + "\n";
                out.append(formattedOutput);
            }


            out.close();
        } catch (IOException e) {
            System.out.println("An error occurred. Data could not be logged");
            e.printStackTrace();
        }
    }

    public static Map<String, String> getDataFromFile() {
        Map<String, String> result = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(log))) {
            for(String line; (line = br.readLine()) != null; ) {
                String[] values = line.split(",");
                result.put(values[0], values[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
