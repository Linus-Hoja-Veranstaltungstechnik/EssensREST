package de.linushoja.essensrest.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        out.value(value == null ? null : value.toString());
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        LocalDateTime ldt = null;

        if (in.hasNext()) {
            ldt = LocalDateTime.parse(in.nextString());
        }
        return ldt;
    }
}
