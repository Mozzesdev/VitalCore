package me.winflix.vitalcore.addons.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.managers.ResourcePackManager;

public class ResourcePackListener implements Listener {

    private final ResourcePackManager resourcePackManager;

    public ResourcePackListener(ResourcePackManager manager) {
        this.resourcePackManager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Puedes añadir un pequeño delay si es necesario
        // Bukkit.getScheduler().runTaskLater(pluginInstance, () -> {
        resourcePackManager.sendResourcePackToPlayer(event.getPlayer());
        // }, 20L); // Ejemplo de 1 segundo de delay (20 ticks)
    }

    // Opcional: Manejar el estado de la descarga
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        VitalCore.Log.info("[ResourcePack] Estado para " + player.getName() + ": " + status);
        switch(status) {
            case SUCCESSFULLY_LOADED:
                // Éxito
                player.sendMessage("¡Paquete de recursos cargado!");
                break;
            case DECLINED:
                // El jugador rechazó
                player.sendMessage("Rechazaste el paquete. Algunas cosas podrían no verse bien.");
                // ¿KICK? Depende de tu servidor: player.kickPlayer("Paquete de recursos requerido.");
                break;
            case FAILED_DOWNLOAD:
                // Error de descarga (URL mala, error de red, etc.)
                player.sendMessage("Error al descargar el paquete de recursos.");
                break;
            case ACCEPTED:
                // El jugador aceptó, la descarga está en progreso
                break;
            case DISCARDED:
                // El jugador descartó el paquete
                player.sendMessage("Descartaste el paquete de recursos.");
                break;
            case FAILED_RELOAD:
                // Error al recargar el paquete
                player.sendMessage("Error al recargar el paquete de recursos.");
                break;
            case INVALID_URL:
                // URL inválida
                player.sendMessage("El paquete de recursos tiene una URL inválida.");
                break;
            case DOWNLOADED:
                // Paquete descargado pero no cargado
                player.sendMessage("El paquete de recursos fue descargado pero no cargado.");
                break;
        }
    }
}