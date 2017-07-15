package foo.repository;

import com.google.gson.*;
import foo.changes.Change;

import java.lang.reflect.Type;

public class ChangeSerializer implements JsonSerializer<Change>, JsonDeserializer<Change> {

    private static final String TYPE = "type";

    @Override
    public JsonElement serialize(Change src, Type typeOfSrc,
                                 JsonSerializationContext context) {

        JsonElement retValue = context.serialize(src);
        if (retValue.isJsonObject()) {
            retValue.getAsJsonObject().addProperty(TYPE, src.getClass().getSimpleName());
        }
        return retValue;
    }

    @Override
    public Change deserialize(JsonElement json, Type typeOfT,
                              JsonDeserializationContext context) throws JsonParseException {

        Class actualClass;
        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            String typeName = jsonObject.get(TYPE).getAsString();

            try {
                actualClass = Class.forName(Change.class.getPackage().getName() + "." + typeName);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new JsonParseException(e.getMessage());
            }
        }
        else {
            actualClass = typeOfT.getClass();
        }
        return context.deserialize(json, actualClass);
    }
}

