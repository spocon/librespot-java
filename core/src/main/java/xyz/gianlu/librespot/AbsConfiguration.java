package xyz.gianlu.librespot;

import com.spotify.connectstate.model.Connect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.gianlu.librespot.cache.CacheManager;
import xyz.gianlu.librespot.core.AuthConfiguration;
import xyz.gianlu.librespot.core.TimeProvider;
import xyz.gianlu.librespot.core.ZeroconfServer;
import xyz.gianlu.librespot.player.Player;

/**
 * @author Gianlu
 */
public abstract class AbsConfiguration implements TimeProvider.Configuration, Player.Configuration, CacheManager.Configuration, AuthConfiguration, ZeroconfServer.Configuration {

    @Nullable
    public abstract String deviceName();

    @Nullable
    public abstract Connect.DeviceType deviceType();

    @NotNull
    public abstract String preferredLocale();
}
