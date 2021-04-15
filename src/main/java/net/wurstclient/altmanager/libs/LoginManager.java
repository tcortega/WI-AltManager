package net.wurstclient.altmanager.libs;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import net.minecraft.client.util.Session;
import net.wurstclient.altmanager.WiAltManager;

import java.net.Proxy;

public final class LoginManager
{
    public static String login(String email, String password)
    {
        YggdrasilUserAuthentication auth =
                (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(
                        Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);

        auth.setUsername(email);
        auth.setPassword(password);

        try
        {
            auth.logIn();
            WiAltManager.IMC
                    .setSession(new Session(auth.getSelectedProfile().getName(),
                            auth.getSelectedProfile().getId().toString(),
                            auth.getAuthenticatedToken(), "mojang"));
            return "";

        } catch (AuthenticationUnavailableException e)
        {
            return "\u00a74\u00a7lCannot contact authentication server!";

        } catch (AuthenticationException e)
        {
            e.printStackTrace();

            if (e.getMessage().contains("Invalid username or password.")
                    || e.getMessage().toLowerCase().contains("account migrated"))
                return "\u00a74\u00a7lWrong password! (or shadowbanned)";
            else
                return "\u00a74\u00a7lCannot contact authentication server!";

        } catch (NullPointerException e)
        {
            e.printStackTrace();
            return "\u00a74\u00a7lWrong password! (or shadowbanned)";
        }
    }

    public static void changeCrackedName(String newName)
    {
        WiAltManager.IMC.setSession(new Session(newName, "", "", "mojang"));
    }
}
