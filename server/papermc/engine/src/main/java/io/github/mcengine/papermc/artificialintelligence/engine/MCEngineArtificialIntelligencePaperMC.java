package io.github.mcengine.papermc.artificialintelligence.engine;

import io.github.mcengine.api.core.MCEngineCoreApi;
import io.github.mcengine.api.artificialintelligence.util.MCEngineArtificialIntelligenceApiUtilToken;
import io.github.mcengine.api.core.Metrics;
import io.github.mcengine.common.artificialintelligence.MCEngineArtificialIntelligenceCommon;
import io.github.mcengine.common.artificialintelligence.command.MCEngineArtificialIntelligenceCommonCommand;
import io.github.mcengine.common.artificialintelligence.tabcompleter.MCEngineArtificialIntelligenceCommonTabCompleter;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main PaperMC plugin class for MCEngineArtificialIntelligence.
 */
public class MCEngineArtificialIntelligencePaperMC extends JavaPlugin {

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

        String license = getConfig().getString("licenses.license", "free"); 
        if (!license.equalsIgnoreCase("free")) { 
            getLogger().warning("Plugin is disabled in config.yml.");
            getLogger().warning("Invalid license.");
            getLogger().warning("Check license or use \"free\".");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize token manager and core AI API
        MCEngineArtificialIntelligenceApiUtilToken.initialize(this);
        MCEngineArtificialIntelligenceCommon api = new MCEngineArtificialIntelligenceCommon(this);

        // Register dispatcher for "/ai" command
        String namespace = "ai";
        api.registerNamespace(namespace);
        api.registerSubCommand(namespace, "default", new MCEngineArtificialIntelligenceCommonCommand(api, api.getDB()));
        api.registerSubTabCompleter(namespace, "default", new MCEngineArtificialIntelligenceCommonTabCompleter(this));

        // Get dispatcher and assign to command
        CommandExecutor dispatcher = api.getDispatcher(namespace);
        getCommand("ai").setExecutor(dispatcher);
        getCommand("ai").setTabCompleter((TabCompleter) dispatcher); // Safe cast: dispatcher implements both

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
            "io.github.mcengine.api.artificialintelligence.extension.agent.IMCEngineArtificialIntelligenceAgent",
            "agents",
            "Agent"
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

        // Check for plugin updates
        MCEngineCoreApi.checkUpdate(
            this,
            getLogger(),
            "github",
            "MCEngine-Engine",
            "artificialintelligence",
            getConfig().getString("github.token", "null")
        );

        // Log project information
        getLogger().info("Project Platform: " + getConfig().getString("project.platform", "github"));
        getLogger().info("Project Organization Name: " + getConfig().getString("project.org.name", "MCEngine-Engine"));
        getLogger().info("Project Organization URL: " + getConfig().getString("project.org.url", "https://github.com/MCEngine-Engine"));
        getLogger().info("Project Repository Name: " + getConfig().getString("project.repo.name", "artificialintelligence"));
        getLogger().info("Project Repository URL: " + getConfig().getString("project.repo.url", "https://github.com/MCEngine-Engine/artificialintelligence"));
        getLogger().info("Project Repository Issues URL: " + getConfig().getString("project.repo.issue.url", "https://github.com/MCEngine-Engine/artificialintelligence/issues"));
        getLogger().info("Project Website URL: " + getConfig().getString("project.website.url", "https://mcengine-website.github.io/artificialintelligence"));
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic (if any) can go here
    }
}
