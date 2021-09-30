package com.example.help.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class jsonUtil {

    public static String getJSON(Context context, String fileName) {
         String json = null;
        try {
            // Opening data.json file
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            inputStream.read(buffer);
            inputStream.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return json;
        }
        return json;
    }

}
