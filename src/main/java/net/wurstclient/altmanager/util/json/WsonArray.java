package net.wurstclient.altmanager.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class WsonArray
{
    private final JsonArray json;

    public WsonArray(JsonArray json)
    {
        this.json = Objects.requireNonNull(json);
    }

    public ArrayList<String> getAllStrings()
    {
        return StreamSupport.stream(json.spliterator(), false)
                .filter(JsonUtils::isString).map(JsonElement::getAsString)
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
    }

    public ArrayList<WsonObject> getAllObjects()
    {
        return StreamSupport.stream(json.spliterator(), false)
                .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                .map(json -> new WsonObject(json))
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
    }

    public JsonArray toJsonArray()
    {
        return json;
    }
}
