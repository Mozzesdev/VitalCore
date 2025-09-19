package me.winflix.vitalcore.addons;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.plugin.RegisteredServiceProvider;

import me.winflix.rsmanager.api.ResourcePackAPI;
import me.winflix.rsmanager.api.exceptions.NamespaceAlreadyRegisteredException;
import me.winflix.rsmanager.api.exceptions.ResourcePackApiException;
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.commands.CommandManager;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.interfaces.Manager;
import me.winflix.vitalcore.addons.commands.ModelList;
import me.winflix.vitalcore.addons.commands.Spawn;
import me.winflix.vitalcore.addons.commands.Reload;
import me.winflix.vitalcore.addons.commands.Remove;
import me.winflix.vitalcore.addons.model.runtime.ModelEngineManager;

public class Addons extends Manager {
    private final ArrayList<SubCommand> addCommands = new ArrayList<>();
    private ModelEngineManager modelEngineManager;
    private ResourcePackAPI rsManagerAPI = null;
    private boolean apiAvailable = false;
    public static final String ADDONS_NAMESPACE = "vc_addons";

    public Addons(VitalCore plugin) {
        super(plugin);
    }

    @Override
    public Addons initialize() {
        RegisteredServiceProvider<ResourcePackAPI> provider = plugin.getServer().getServicesManager()
                .getRegistration(ResourcePackAPI.class);
        if (provider != null) {
            this.rsManagerAPI = provider.getProvider();
            this.apiAvailable = true;

            try {
                this.rsManagerAPI.registerNamespace(plugin, ADDONS_NAMESPACE);
            } catch (NamespaceAlreadyRegisteredException e) {
                VitalCore.Log.warning("[Addons] El namespace '" + ADDONS_NAMESPACE
                        + "' ya estaba registrado (esto puede ser normal).");
            } catch (IllegalArgumentException | ResourcePackApiException e) {
                VitalCore.Log.log(Level.SEVERE,
                        "[Addons] Error al registrar el namespace '" + ADDONS_NAMESPACE + "': " + e.getMessage(), e);
                this.apiAvailable = false;
            }

        } else {
            VitalCore.Log.severe(
                    "[Addons] No se pudo encontrar la API de CentralResourcePack. Funcionalidades de Resource Pack desactivadas.");
            VitalCore.Log.severe("[Addons] Asegúrate de que CentralResourcePack esté instalado y habilitado.");
            this.apiAvailable = false;
        }

        modelEngineManager = new ModelEngineManager(plugin,
                this.rsManagerAPI, ADDONS_NAMESPACE);

        setupCommands();
        setupEvents();

        return this;
    }

    public void setupCommands() {
        registerCommands();
        CommandManager addCM = new CommandManager(plugin, addCommands);
        plugin.getCommand("addons").setExecutor(addCM);
    }

    public void setupEvents() {
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

    public ResourcePackAPI getRSManagerApi() {
        return rsManagerAPI;
    }

    // Flag para saber si la API está disponible
    public boolean isApiAvailable() {
        return apiAvailable;
    }

    @Override
    public void onDisable() {
        if (modelEngineManager != null)
            modelEngineManager.shutdown();

        this.rsManagerAPI = null;
        this.apiAvailable = false;
    }

}
