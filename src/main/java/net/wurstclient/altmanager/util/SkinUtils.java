package net.wurstclient.altmanager.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SkinUtils
{
    private static String getUUID(String username) throws IOException
    {
        URL profileURL =
                URI.create("https://api.mojang.com/users/profiles/minecraft/")
                        .resolve(URLEncoder.encode(username, "UTF-8")).toURL();

        try (InputStream profileInputStream = profileURL.openStream())
        {
            // {"name":"<username>","id":"<UUID>"}

            JsonObject profileJson = new Gson().fromJson(
                    IOUtils.toString(profileInputStream, StandardCharsets.UTF_8),
                    JsonObject.class);

            if (profileJson == null)
                return "e58c93ee53304a6dbfa093fb8d86e90b";

            return profileJson.get("id").getAsString();
        }
    }

    /**
     * Decodes the base64 textures value from {@link #getSessionJson(String)}.
     * Once decoded, it looks like this:
     *
     * <code><pre>
     * {
     *   "timestamp" : &lt;current time&gt;,
     *   "profileId" : "&lt;UUID&gt;",
     *   "profileName" : "&lt;username&gt;",
     *   "textures":
     *   {
     *     "SKIN":
     *     {
     *       "url": "http://textures.minecraft.net/texture/&lt;texture ID&gt;"
     *     }
     *   }
     * }
     * </pre></code>
     */
    private static JsonObject getTexturesValue(String uuid) throws IOException
    {
        JsonObject sessionJson = getSessionJson(uuid);

        JsonArray propertiesJson =
                sessionJson.get("properties").getAsJsonArray();
        JsonObject firstProperty = propertiesJson.get(0).getAsJsonObject();
        String texturesBase64 = firstProperty.get("value").getAsString();

        byte[] texturesBytes = Base64.decodeBase64(texturesBase64.getBytes());
        JsonObject texturesJson =
                new Gson().fromJson(new String(texturesBytes), JsonObject.class);

        return texturesJson;
    }

    /**
     * Grabs the JSON code from the session server. It looks something like
     * this:
     *
     * <code><pre>
     * {
     *   "id": "&lt;UUID&gt;",
     *   "name": "&lt;username&gt;",
     *   "properties":
     *   [
     *     {
     *       "name": "textures",
     *       "value": "&lt;base64 encoded JSON&gt;"
     *     }
     *   ]
     * }
     * </pre></code>
     */
    private static JsonObject getSessionJson(String uuid) throws IOException
    {
        URL sessionURL = URI
                .create(
                        "https://sessionserver.mojang.com/session/minecraft/profile/")
                .resolve(uuid).toURL();

        try (InputStream sessionInputStream = sessionURL.openStream())
        {
            return new Gson().fromJson(
                    IOUtils.toString(sessionInputStream, StandardCharsets.UTF_8),
                    JsonObject.class);
        }
    }

    /**
     * Returns the skin download URL for the given username.
     */
    public static URL getSkinUrl(String username) throws IOException
    {
        String uuid = getUUID(username);
        JsonObject texturesValueJson = getTexturesValue(uuid);

        // Grab URL for skin
        JsonObject tJObj = texturesValueJson.get("textures").getAsJsonObject();
        JsonObject skinJObj = tJObj.get("SKIN").getAsJsonObject();
        String skin = skinJObj.get("url").getAsString();

        return URI.create(skin).toURL();
    }
}
