package io.github.mcengine.papermc.artificialintelligence.engine;

import io.github.mcengine.api.artificialintelligence.MCEngineArtificialIntelligenceApi;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;
import io.github.mcengine.api.mcengine.Metrics;
import io.github.mcengine.api.mcengine.util.MCEngineApiUtilExtension;
import io.github.mcengine.common.artificialintelligence.command.MCEngineArtificialIntelligenceCommonCommand;
import io.github.mcengine.common.artificialintelligence.tabcompleter.MCEngineArtificialIntelligenceCommonTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;

/**
 * Main PaperMC plugin class for MCEngineArtificialIntelligence.
 * Handles plugin lifecycle, token validation, API initialization, and update checking.
 */
public class MCEngineArtificialIntelligencePaperMC extends JavaPlugin {

    /**
     * Secret key used for token validation (may be configured).
     */
    private String secretKey;

    /**
     * Token used to verify license or authentication.
     */
    private String token;

    /**
     * Expiration date of the token (if applicable).
     */
    private Date expirationDate;

    /**
     * Called when the plugin is enabled.
     * Performs configuration loading, token validation, API initialization, and schedules token validation checks.
     */
    @Override
    public void onEnable() {
        new Metrics(this, 25556);
        saveDefaultConfig(); // Save config.yml if it doesn't exist

        boolean enabled = getConfig().getBoolean("enable", false);
        if (!enabled) {
            getLogger().warning("Plugin is disabled in config.yml (enable: false). Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MCEngineArtificialIntelligenceApiUtilToken.initialize(this);
        MCEngineArtificialIntelligenceApi api = new MCEngineArtificialIntelligenceApi(this);

        getCommand("ai").setExecutor(new MCEngineArtificialIntelligenceCommonCommand(api));
        getCommand("ai").setTabCompleter(new MCEngineArtificialIntelligenceCommonTabCompleter(this));

        // Load extensions
        MCEngineApiUtilExtension.loadExtensions(this, "addons", "AddOn");
        MCEngineApiUtilExtension.loadExtensions(this, "dlcs", "DLC");

        // Load built-in models
        String[] platforms = { "deepseek", "openai", "openrouter" };
        for (String platform : platforms) {
            String modelsKey = "ai." + platform + ".models";
            if (getConfig().isConfigurationSection(modelsKey)) {
                ConfigurationSection section = getConfig().getConfigurationSection(modelsKey);
                for (String key : section.getKeys(false)) {
                    String modelName = section.getString(key);
                    if (modelName != null && !modelName.equalsIgnoreCase("null")) {
                        api.registerModel(platform, modelName);
                    }
                }
            }
        }

        // Load custom server models
        if (getConfig().isConfigurationSection("ai.custom")) {
            for (String server : getConfig().getConfigurationSection("ai.custom").getKeys(false)) {
                String modelsKey = "ai.custom." + server + ".models";
                if (getConfig().isConfigurationSection(modelsKey)) {
                    ConfigurationSection section = getConfig().getConfigurationSection(modelsKey);
                    for (String key : section.getKeys(false)) {
                        String modelName = section.getString(key);
                        if (modelName != null && !modelName.equalsIgnoreCase("null")) {
                            api.registerModel("customurl", server + ":" + modelName);
                        }
                    }
                }
            }
        }

        api.checkUpdate("github", "MCEngine", "artificialintelligence", getConfig().getString("github.token", "null"));
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {}
}
