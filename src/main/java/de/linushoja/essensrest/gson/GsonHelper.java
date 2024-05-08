package de.linushoja.essensrest.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

public class GsonHelper {
    private static Gson gson;

    public static Gson getGson(){
        if(gson != null) return gson;

        gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        return gson;
    }
}
