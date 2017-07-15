package foo.changes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ChangeSource {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Object.class, new ChangeSerializer())
            .create();
}
