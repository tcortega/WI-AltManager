package net.wurstclient.altmanager.libs;

import com.google.gson.JsonObject;
import net.wurstclient.altmanager.util.json.JsonException;
import net.wurstclient.altmanager.util.json.JsonUtils;
import net.wurstclient.altmanager.util.json.WsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;

public final class AltsFile
{
    private final Path path;
    private final Encryption encryption;
    private boolean disableSaving;

    public AltsFile(Path path, Path encFolder)
    {
        this.path = path;
        encryption = new Encryption(encFolder);
    }

    public static ArrayList<Alt> parseJson(WsonObject wson)
    {
        ArrayList<Alt> alts = new ArrayList<>();

        for (Map.Entry<String, JsonObject> e : wson.getAllJsonObjects().entrySet())
        {
            String email = e.getKey();
            JsonObject jsonAlt = e.getValue();

            alts.add(loadAlt(email, jsonAlt));
        }

        return alts;
    }

    private static Alt loadAlt(String email, JsonObject jsonAlt)
    {
        String password = JsonUtils.getAsString(jsonAlt.get("password"), "");
        String name = JsonUtils.getAsString(jsonAlt.get("name"), "");
        boolean starred = JsonUtils.getAsBoolean(jsonAlt.get("starred"), false);

        return new Alt(email, password, name, starred);
    }

    public static JsonObject createJson(AltManager alts)
    {
        JsonObject json = new JsonObject();

        for (Alt alt : alts.getList())
        {
            JsonObject jsonAlt = new JsonObject();

            jsonAlt.addProperty("password", alt.getPassword());
            jsonAlt.addProperty("name", alt.getName());
            jsonAlt.addProperty("starred", alt.isStarred());

            json.add(alt.getEmail(), jsonAlt);
        }

        return json;
    }

    public void load(AltManager altManager)
    {
        try
        {
            WsonObject wson = encryption.parseFileToObject(path);
            loadAlts(wson, altManager);

        } catch (NoSuchFileException e)
        {
            // The file doesn't exist yet. No problem, we'll create it later.

        } catch (IOException | JsonException e)
        {
            System.out.println("Couldn't load " + path.getFileName());
            e.printStackTrace();

            renameCorrupted();
        }

        save(altManager);
    }

    private void renameCorrupted()
    {
        try
        {
            Path newPath =
                    path.resolveSibling("!CORRUPTED_" + path.getFileName());
            Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Renamed to " + newPath.getFileName());

        } catch (IOException e2)
        {
            System.out.println(
                    "Couldn't rename corrupted file " + path.getFileName());
            e2.printStackTrace();
        }
    }

    private void loadAlts(WsonObject wson, AltManager altManager)
    {
        ArrayList<Alt> alts = parseJson(wson);

        try
        {
            disableSaving = true;
            altManager.addAll(alts);

        } finally
        {
            disableSaving = false;
        }
    }

    public void save(AltManager alts)
    {
        if (disableSaving)
            return;

        JsonObject json = createJson(alts);

        try
        {
            encryption.toEncryptedJson(json, path);

        } catch (IOException | JsonException e)
        {
            System.out.println("Couldn't save " + path.getFileName());
            e.printStackTrace();
        }
    }
}
