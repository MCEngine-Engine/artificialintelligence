package io.github.mcengine.spigotmc.artificialintelligence.engine;

import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;
import io.github.mcengine.api.core.Metrics;
import io.github.mcengine.common.artificialintelligence.command.MCEngineArtificialIntelligenceCommonCommand;
import io.github.mcengine.common.artificialintelligence.tabcompleter.MCEngineArtificialIntelligenceCommonTabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main SpigotMC plugin class for MCEngineArtificialIntelligence.
 */
public class MCEngineArtificialIntelligenceSpigotMC extends JavaPlugin {

    /**
     * Called when the plugin is enabled.
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

        // Initialize token manager and core API
        MCEngineArtificialIntelligenceApiUtilToken.initialize(this);
        MCEngineArtificialIntelligenceCommon api = new MCEngineArtificialIntelligenceCommon(this);

        // Register namespace and command
        String namespace = "ai";
        api.registerNamespace(namespace);
        api.registerSubCommand(namespace, "default", new MCEngineArtificialIntelligenceCommonCommand(api, api.getDB()));
        api.registerSubTabCompleter(namespace, "default", new MCEngineArtificialIntelligenceCommonTabCompleter(this));
        getCommand("ai").setExecutor(api.getDispatcher(namespace));
        getCommand("ai").setTabCompleter(api.getDispatcher(namespace));

        // Load extensions
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.artificialintelligence.extension.library.IMCEngineArtificialIntelligenceLibrary",
            "libraries",
            "Library"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.artificialintelligence.extension.api.IMCEngineArtificialIntelligenceAPI",
            "apis",
            "API"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.artificialintelligence.extension.addon.IMCEngineArtificialIntelligenceAddOn",
            "addons",
            "AddOn"
        );
        MCEngineCoreApi.loadExtensions(
            this,
            "io.github.mcengine.api.artificialintelligence.extension.dlc.IMCEngineArtificialIntelligenceDLC",
            "dlcs",
            "DLC"
        );

        // Load built-in models from config
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

        // Check for updates
        MCEngineCoreApi.checkUpdate(this, getLogger(), "github", "MCEngine-Engine", "artificialintelligence", getConfig().getString("github.token", "null"));
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {}
}
