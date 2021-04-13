package net.wurstclient.altmanager.util.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class WsonObject
{
    private final JsonObject json;

    public WsonObject(JsonObject json)
    {
        this.json = Objects.requireNonNull(json);
    }

    public boolean getBoolean(String key) throws JsonException
    {
        return JsonUtils.getAsBoolean(json.get(key));
    }

    public int getInt(String key) throws JsonException
    {
        return JsonUtils.getAsInt(json.get(key));
    }

    public long getLong(String key) throws JsonException
    {
        return JsonUtils.getAsLong(json.get(key));
    }

    public String getString(String key) throws JsonException
    {
        return JsonUtils.getAsString(json.get(key));
    }

    public WsonArray getArray(String key) throws JsonException
    {
        return JsonUtils.getAsArray(json.get(key));
    }

    public LinkedHashMap<String, String> getAllStrings()
    {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        for(Map.Entry<String, JsonElement> entry : json.entrySet())
        {
            JsonElement value = entry.getValue();
            if(!JsonUtils.isString(value))
                continue;

            map.put(entry.getKey(), value.getAsString());
        }

        return map;
    }

    public LinkedHashMap<String, Number> getAllNumbers()
    {
        LinkedHashMap<String, Number> map = new LinkedHashMap<>();

        for(Map.Entry<String, JsonElement> entry : json.entrySet())
        {
            JsonElement value = entry.getValue();
            if(!JsonUtils.isNumber(value))
                continue;

            map.put(entry.getKey(), value.getAsNumber());
        }

        return map;
    }

    public LinkedHashMap<String, JsonObject> getAllJsonObjects()
    {
        LinkedHashMap<String, JsonObject> map = new LinkedHashMap<>();

        for(Map.Entry<String, JsonElement> entry : json.entrySet())
        {
            JsonElement value = entry.getValue();
            if(!value.isJsonObject())
                continue;

            map.put(entry.getKey(), value.getAsJsonObject());
        }

        return map;
    }

    public JsonObject toJsonObject()
    {
        return json;
    }
}