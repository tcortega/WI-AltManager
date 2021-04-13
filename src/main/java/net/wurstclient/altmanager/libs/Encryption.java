package net.wurstclient.altmanager.libs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.wurstclient.altmanager.util.json.JsonException;
import net.wurstclient.altmanager.util.json.JsonUtils;
import net.wurstclient.altmanager.util.json.WsonArray;
import net.wurstclient.altmanager.util.json.WsonObject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public final class Encryption
{
    private static final String CHARSET = "UTF-8";

    private final Cipher encryptCipher;
    private final Cipher decryptCipher;

    public Encryption(Path encFolder)
    {
        KeyPair rsaKeyPair =
                getRsaKeyPair(encFolder.resolve("wi_rsa_public.txt"),
                        encFolder.resolve("wi_rsa_private.txt"));

        SecretKey aesKey =
                getAesKey(encFolder.resolve("wi_aes.txt"), rsaKeyPair);

        try
        {
            encryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey,
                    new IvParameterSpec(aesKey.getEncoded()));

            decryptCipher = Cipher.getInstance("AES/CFB8/NoPadding");
            decryptCipher.init(Cipher.DECRYPT_MODE, aesKey,
                    new IvParameterSpec(aesKey.getEncoded()));

        }catch(GeneralSecurityException e)
        {
            throw new CrashException(
                    CrashReport.create(e, "Creating AES ciphers"));
        }
    }

    public byte[] decrypt(byte[] bytes)
    {
        try
        {
            return decryptCipher.doFinal(Base64.getDecoder().decode(bytes));

        }catch(IllegalArgumentException | GeneralSecurityException e)
        {
            throw new CrashException(CrashReport.create(e, "Decrypting bytes"));
        }
    }

    public String loadEncryptedFile(Path path) throws IOException
    {
        try
        {
            return new String(decrypt(Files.readAllBytes(path)), CHARSET);

        }catch(CrashException e)
        {
            throw new IOException(e);
        }
    }

    public JsonElement parseFile(Path path) throws IOException, JsonException
    {
        try(BufferedReader reader = Files.newBufferedReader(path))
        {
            return JsonUtils.JSON_PARSER.parse(loadEncryptedFile(path));

        }catch(JsonParseException e)
        {
            throw new JsonException(e);
        }
    }

    public WsonArray parseFileToArray(Path path)
            throws IOException, JsonException
    {
        JsonElement json = parseFile(path);

        if(!json.isJsonArray())
            throw new JsonException();

        return new WsonArray(json.getAsJsonArray());
    }

    public WsonObject parseFileToObject(Path path)
            throws IOException, JsonException
    {
        JsonElement json = parseFile(path);

        if(!json.isJsonObject())
            throw new JsonException();

        return new WsonObject(json.getAsJsonObject());
    }

    public byte[] encrypt(byte[] bytes)
    {
        try
        {
            return Base64.getEncoder().encode(encryptCipher.doFinal(bytes));

        }catch(GeneralSecurityException e)
        {
            throw new CrashException(CrashReport.create(e, "Encrypting bytes"));
        }
    }

    public void saveEncryptedFile(Path path, String content) throws IOException
    {
        try
        {
            Files.write(path, encrypt(content.getBytes(CHARSET)));

        }catch(CrashException e)
        {
            throw new IOException(e);
        }
    }

    public void toEncryptedJson(JsonObject json, Path path)
            throws IOException, JsonException
    {
        try
        {
            saveEncryptedFile(path, JsonUtils.PRETTY_GSON.toJson(json));

        }catch(JsonParseException e)
        {
            throw new JsonException(e);
        }
    }

    private KeyPair getRsaKeyPair(Path publicFile, Path privateFile)
    {
        if(Files.notExists(publicFile) || Files.notExists(privateFile))
            return createRsaKeys(publicFile, privateFile);

        try
        {
            return loadRsaKeys(publicFile, privateFile);

        }catch(GeneralSecurityException | ReflectiveOperationException
                | IOException e)
        {
            System.err.println("Couldn't load RSA keypair!");
            e.printStackTrace();

            return createRsaKeys(publicFile, privateFile);
        }
    }

    private SecretKey getAesKey(Path path, KeyPair pair)
    {
        if(Files.notExists(path))
            return createAesKey(path, pair);

        try
        {
            return loadAesKey(path, pair);

        }catch(GeneralSecurityException | IOException e)
        {
            System.err.println("Couldn't load AES key!");
            e.printStackTrace();

            return createAesKey(path, pair);
        }
    }

    private KeyPair createRsaKeys(Path publicFile, Path privateFile)
    {
        try
        {
            System.out.println("Generating RSA keypair.");

            // generate keypair
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();

            KeyFactory factory = KeyFactory.getInstance("RSA");

            // save public key
            try(ObjectOutputStream out =
                        new ObjectOutputStream(Files.newOutputStream(publicFile)))
            {
                RSAPublicKeySpec keySpec = factory.getKeySpec(pair.getPublic(),
                        RSAPublicKeySpec.class);

                out.writeObject(keySpec.getModulus());
                out.writeObject(keySpec.getPublicExponent());
            }

            // save private key
            try(ObjectOutputStream out =
                        new ObjectOutputStream(Files.newOutputStream(privateFile)))
            {
                RSAPrivateKeySpec keySpec = factory
                        .getKeySpec(pair.getPrivate(), RSAPrivateKeySpec.class);

                out.writeObject(keySpec.getModulus());
                out.writeObject(keySpec.getPrivateExponent());
            }

            return pair;

        }catch(GeneralSecurityException | IOException e)
        {
            throw new CrashException(
                    CrashReport.create(e, "Creating RSA keypair"));
        }
    }

    private SecretKey createAesKey(Path path, KeyPair pair)
    {
        try
        {
            System.out.println("Generating AES key.");

            // generate key
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            SecretKey key = keygen.generateKey();

            // save key
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, pair.getPublic());
            Files.write(path, rsaCipher.doFinal(key.getEncoded()));

            return key;

        }catch(GeneralSecurityException | IOException e)
        {
            throw new CrashException(CrashReport.create(e, "Creating AES key"));
        }
    }

    private KeyPair loadRsaKeys(Path publicFile, Path privateFile)
            throws GeneralSecurityException, ReflectiveOperationException,
            IOException
    {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        // load public key
        PublicKey publicKey;
        try(ObjectInputStream in =
                    new ObjectInputStream(Files.newInputStream(publicFile)))
        {
            publicKey = factory.generatePublic(new RSAPublicKeySpec(
                    (BigInteger)in.readObject(), (BigInteger)in.readObject()));
        }

        // load private key
        PrivateKey privateKey;
        try(ObjectInputStream in =
                    new ObjectInputStream(Files.newInputStream(privateFile)))
        {
            privateKey = factory.generatePrivate(new RSAPrivateKeySpec(
                    (BigInteger)in.readObject(), (BigInteger)in.readObject()));
        }

        return new KeyPair(publicKey, privateKey);
    }

    private SecretKey loadAesKey(Path path, KeyPair pair)
            throws GeneralSecurityException, IOException
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());

        return new SecretKeySpec(cipher.doFinal(Files.readAllBytes(path)),
                "AES");
    }
}
