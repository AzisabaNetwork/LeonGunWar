package net.azisaba.lgw.core.api.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint of PlaceholderAPI integration
 */
public class PlaceHolderAPI {
    public static final String PL_ID = "PlaceholderAPI";
    private static final PlaceHolderAPI API = new PlaceHolderAPI();
    private static final Logger logger = LoggerFactory.getLogger(PlaceHolderAPI.class);

    public static PlaceHolderAPI getApi() {
        return API;
    }

    /**
     * Is PlaceholderAPI Loaded
     * @return is loaded
     */
    public boolean isLoaded() {
        return Bukkit.getPluginManager().isPluginEnabled(PL_ID);
    }

    public <T extends PlaceholderExpansion> void register(T placeholderExpansion) {
        if(!isLoaded()) {
            logger.warn("Ignore placeholder registration of {}", placeholderExpansion.getClass().getName());
        } else {
            placeholderExpansion.register();
        }
    }
}
