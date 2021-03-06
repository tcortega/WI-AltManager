package net.wurstclient.altmanager.libs;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.wurstclient.altmanager.WiAltManager;
import net.wurstclient.altmanager.util.SkinUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;

public class PlayerSkinFetcher extends AbstractTexture
{
    private static final MinecraftClient mc = WiAltManager.MC;
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private final Runnable loadedCallback;
    @Nullable
    private File cacheFile;
    private String url;
    private boolean convertLegacy;
    @Nullable
    private CompletableFuture<?> loader;

    public PlayerSkinFetcher(@Nullable File cacheFile, String username, boolean convertLegacy, @Nullable Runnable callback) throws IOException
    {
        super();
        this.cacheFile = cacheFile;
        this.convertLegacy = convertLegacy;
        this.loadedCallback = callback;

        URL tempUrl = SkinUtils.getSkinUrl(username);
        if (tempUrl == null)
        {
            this.url = null;
            return;
        }
        this.url = tempUrl.toString();
    }

    public static AbstractTexture Fetch(Identifier id, String username) throws IOException
    {
        TextureManager textureManager = mc.getTextureManager();
//        AbstractTexture abstractTexture = textureManager.getTexture(id);
//        if (abstractTexture == null)
//        {
            AbstractTexture abstractTexture = new PlayerSkinFetcher((File) null, username, true, (Runnable) null);
            textureManager.registerTexture(id, abstractTexture);
//        }

        return abstractTexture;
    }

    private static NativeImage remapTexture(NativeImage image)
    {
        boolean bl = image.getHeight() == 32;
        if (bl)
        {
            NativeImage nativeImage = new NativeImage(64, 64, true);
            nativeImage.copyFrom(image);
            image.close();
            image = nativeImage;
            nativeImage.fillRect(0, 32, 64, 32, 0);
            nativeImage.copyRect(4, 16, 16, 32, 4, 4, true, false);
            nativeImage.copyRect(8, 16, 16, 32, 4, 4, true, false);
            nativeImage.copyRect(0, 20, 24, 32, 4, 12, true, false);
            nativeImage.copyRect(4, 20, 16, 32, 4, 12, true, false);
            nativeImage.copyRect(8, 20, 8, 32, 4, 12, true, false);
            nativeImage.copyRect(12, 20, 16, 32, 4, 12, true, false);
            nativeImage.copyRect(44, 16, -8, 32, 4, 4, true, false);
            nativeImage.copyRect(48, 16, -8, 32, 4, 4, true, false);
            nativeImage.copyRect(40, 20, 0, 32, 4, 12, true, false);
            nativeImage.copyRect(44, 20, -8, 32, 4, 12, true, false);
            nativeImage.copyRect(48, 20, -16, 32, 4, 12, true, false);
            nativeImage.copyRect(52, 20, -8, 32, 4, 12, true, false);
        }

        stripAlpha(image, 0, 0, 32, 16);
        if (bl)
        {
            stripColor(image, 32, 0, 64, 32);
        }

        stripAlpha(image, 0, 16, 64, 32);
        stripAlpha(image, 16, 48, 48, 64);
        return image;
    }

    private static void stripColor(NativeImage image, int x, int y, int width, int height)
    {
        int l;
        int m;
        for (l = x; l < width; ++l)
        {
            for (m = y; m < height; ++m)
            {
                int k = image.getPixelColor(l, m);
                if ((k >> 24 & 255) < 128)
                {
                    return;
                }
            }
        }

        for (l = x; l < width; ++l)
        {
            for (m = y; m < height; ++m)
            {
                image.setPixelColor(l, m, image.getPixelColor(l, m) & 16777215);
            }
        }

    }

    private static void stripAlpha(NativeImage image, int x, int y, int width, int height)
    {
        for (int i = x; i < width; ++i)
        {
            for (int j = y; j < height; ++j)
            {
                image.setPixelColor(i, j, image.getPixelColor(i, j) | -16777216);
            }
        }

    }

    private void onTextureLoaded(NativeImage image)
    {
        if (this.loadedCallback != null)
        {
            this.loadedCallback.run();
        }

        mc.execute(() -> {
            if (!RenderSystem.isOnRenderThread())
            {
                RenderSystem.recordRenderCall(() -> {
                    this.uploadTexture(image);
                });
            } else
            {
                this.uploadTexture(image);
            }

        });
    }

    private void uploadTexture(NativeImage image)
    {
        TextureUtil.allocate(this.getGlId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

    public void load(ResourceManager manager) throws IOException
    {

        if (this.url == null)
        {
            return;
        }

        if (this.loader == null)
        {
            NativeImage nativeImage2;
            if (this.cacheFile != null && this.cacheFile.isFile())
            {
                LOGGER.debug("Loading http texture from local cache ({})", this.cacheFile);
                FileInputStream fileInputStream = new FileInputStream(this.cacheFile);
                nativeImage2 = this.loadTexture(fileInputStream);
            } else
            {
                nativeImage2 = null;
            }

            if (nativeImage2 != null)
            {
                this.onTextureLoaded(nativeImage2);
            } else
            {
                this.loader = CompletableFuture.runAsync(() -> {
                    LOGGER.debug("Downloading http texture from {} to {}", this.url, this.cacheFile);

                    try
                    {

                        Object inputStream2 = null;
                        URLConnection conn = new URL(this.url).openConnection();
                        try
                        {
                            inputStream2 = conn.getInputStream();
                            if (this.cacheFile != null)
                            {
                                FileUtils.copyInputStreamToFile((InputStream) inputStream2, this.cacheFile);
                            }
                        } catch (IOException ioe)
                        {
                            LOGGER.warn("Failed to get input stream for http texture");
                        }

                        Object finalInputStream = inputStream2;
                        MinecraftClient.getInstance().execute(() -> {
                            NativeImage nativeImage = this.loadTexture((InputStream) finalInputStream);
                            if (nativeImage != null)
                            {
                                this.onTextureLoaded(nativeImage);
                            }

                        });
                        return;
                    } catch (Exception var6)
                    {
                        LOGGER.error("Couldn't download http texture", var6);
                        return;
                    }

                }, Util.getMainWorkerExecutor());
            }
        }
    }

    @Nullable
    private NativeImage loadTexture(InputStream stream)
    {
        NativeImage nativeImage = null;

        try
        {
            nativeImage = NativeImage.read(stream);
            if (this.convertLegacy)
            {
                nativeImage = remapTexture(nativeImage);
            }
        } catch (IOException var4)
        {
            LOGGER.warn("Error while loading the skin texture", var4);
        }

        return nativeImage;
    }
}
