package me.winflix.vitalcore.addons;

import java.io.IOException;
import java.util.ArrayList;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.addons.commands.ModelList;
import me.winflix.vitalcore.addons.commands.Spawn;
import me.winflix.vitalcore.addons.commands.Reload;
import me.winflix.vitalcore.addons.commands.Remove;
import me.winflix.vitalcore.addons.events.ResourcePackListener;
import me.winflix.vitalcore.addons.managers.ModelEngineManager;
import me.winflix.vitalcore.addons.managers.ResourcePackManager;
import me.winflix.vitalcore.addons.server.HttpServerManager;

public class Addons extends Manager {
    private final ArrayList<SubCommand> addCommands = new ArrayList<>();
    private ModelEngineManager modelEngineManager;
    private ResourcePackManager resourcePackManager;
    private HttpServerManager httpServerManager;

    public Addons(VitalCore plugin) {
        super(plugin);
    }

    @Override
    public Addons initialize() {
        modelEngineManager = new ModelEngineManager(plugin.getDataFolder());
        try {
            resourcePackManager = new ResourcePackManager(modelEngineManager);
            resourcePackManager.generateResourcePack(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupCommands();
        setupEvents();
        httpServerManager = new HttpServerManager(plugin,
                this.resourcePackManager);
        httpServerManager.startServer();
        return this;
    }

    public void setupCommands() {
        registerCommands();
        CommandManager addCM = new CommandManager(plugin, addCommands);
        plugin.getCommand("addons").setExecutor(addCM);
    }

    public void setupEvents() {
        plugin.getServer().getPluginManager().registerEvents(new ResourcePackListener(resourcePackManager), plugin);
    }

    public void registerCommands() {
        addCommands.add(new Spawn());
        addCommands.add(new Remove());
        addCommands.add(new Reload());
        addCommands.add(new ModelList(modelEngineManager));
    }

    public ModelEngineManager getModelEngineManager() {
        return modelEngineManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    @Override
    public void onDisable() {
        if (httpServerManager != null)
            httpServerManager.stopServer();
        if (modelEngineManager != null)
            modelEngineManager.shutdown();
    }

}
