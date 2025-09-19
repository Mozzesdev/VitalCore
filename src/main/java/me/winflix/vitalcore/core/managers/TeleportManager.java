package me.winflix.vitalcore.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.core.managers.BackManager;
import me.winflix.vitalcore.core.models.TeleportRequest;
import me.winflix.vitalcore.general.utils.ClickableMessage;
import me.winflix.vitalcore.general.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private static final Map<UUID, List<TeleportRequest>> pendingRequests = new HashMap<>();
    private static final Map<UUID, Boolean> tpaToggleStatus = new HashMap<>(); // true = acepta TPA, false = rechaza
    private static final int REQUEST_TIMEOUT_SECONDS = 10;

    // Método para obtener nombres de jugadores con solicitudes pendientes
    public static List<String> getPendingRequestSenders(Player target) {
        List<TeleportRequest> requests = pendingRequests.get(target.getUniqueId());
        if (requests == null || requests.isEmpty()) {
            return new ArrayList<>();
        }
        
        return requests.stream()
                .map(req -> req.getSender().getName())
                .filter(name -> name != null)
                .toList();
    }

    // Método para obtener información detallada de solicitudes pendientes
    public static void showPendingRequests(Player target) {
        List<TeleportRequest> requests = pendingRequests.get(target.getUniqueId());
        
        if (requests == null || requests.isEmpty()) {
            Utils.infoMessage(target, "No tienes solicitudes de teletransporte pendientes.");
            return;
        }
        
        Utils.infoMessage(target, "&6&lSolicitudes pendientes &7(" + requests.size() + "):");
        for (TeleportRequest request : requests) {
            long timeLeft = REQUEST_TIMEOUT_SECONDS - ((System.currentTimeMillis() - request.getTimestamp()) / 1000);
            String timeLeftStr = timeLeft > 0 ? timeLeft + "s" : "Expirando...";
            Utils.infoMessage(target, "&8• &a" + request.getSender().getName() + " &7(" + timeLeftStr + ")");
        }
    }

    // Métodos para sistema de toggle TPA
    public static boolean isTpaEnabled(Player player) {
        return tpaToggleStatus.getOrDefault(player.getUniqueId(), true); // Por defecto está activado
    }
    
    public static void toggleTpa(Player player) {
        UUID playerId = player.getUniqueId();
        boolean currentStatus = tpaToggleStatus.getOrDefault(playerId, true);
        boolean newStatus = !currentStatus;
        tpaToggleStatus.put(playerId, newStatus);
        
        String message = newStatus ? 
            "Has activado las solicitudes de teletransporte." : 
            "Has desactivado las solicitudes de teletransporte.";
        Utils.infoMessage(player, message);
    }

    public static void sendTeleportRequest(Player sender, Player target) {
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            Utils.errorMessage(sender, "No puedes enviarte una solicitud de teletransporte a ti mismo.");
            return;
        }

        // Verificar si el target tiene las solicitudes TPA desactivadas
        if (!isTpaEnabled(target)) {
            Utils.errorMessage(sender, target.getName() + " tiene las solicitudes de teletransporte desactivadas.");
            return;
        }

        List<TeleportRequest> requests = pendingRequests.getOrDefault(target.getUniqueId(), new ArrayList<>());

        if (requests.stream().anyMatch(req -> req.getSender().getUniqueId().equals(sender.getUniqueId()))) {
            Utils.errorMessage(sender, "Ya tienes una solicitud pendiente para " + target.getName() + ".");
            return;
        }

        TeleportRequest request = new TeleportRequest(sender, target);
        requests.add(request);
        pendingRequests.put(target.getUniqueId(), requests);

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(
                VitalCore.getPlugin(),
                () -> {
                    List<TeleportRequest> currentRequests = pendingRequests.get(target.getUniqueId());
                    if (currentRequests != null && currentRequests.remove(request)) {
                        Utils.errorMessage(sender,
                                "Tu solicitud de teletransporte a " + target.getName() + " ha expirado.");
                        Utils.errorMessage(target,
                                "La solicitud de teletransporte de " + sender.getName() + " ha expirado.");
                        if (currentRequests.isEmpty()) {
                            pendingRequests.remove(target.getUniqueId());
                        }
                    }
                },
                REQUEST_TIMEOUT_SECONDS * 20L);
        request.setTimeoutTask(timeoutTask);

        // Notificar a ambos jugadores
        ClickableMessage confirmMsg = new ClickableMessage("&aAceptar", "tpaccept " + sender.getDisplayName(),
                "&7Haz clic para aceptar.");
        ClickableMessage rejectMsg = new ClickableMessage("&cRechazar", "tpadeny " + sender.getDisplayName(),
                "&7Haz clic para rechazar.");

        String targetMessage = "&8[&7VitalCore&8] -> &6${sender:name} &7quiere teletransportarse hacia ti! &7(${clk:accept} &b|| ${clk:reject}&7)";
        targetMessage = targetMessage.replace("${sender:name}", sender.getDisplayName());
        targetMessage = targetMessage.replace("${clk:accept}", confirmMsg.getMessage());
        targetMessage = targetMessage.replace("${clk:reject}", rejectMsg.getMessage());

        Utils.sendConfirmationClickableMessage(target, targetMessage, confirmMsg, rejectMsg);
        Utils.infoMessage(sender,
                "Solicitud de teletransporte enviada a " + target.getName() + ". Esperando respuesta...");
    }

    public static void acceptRequest(Player target, String senderName) {
        List<TeleportRequest> requests = pendingRequests.get(target.getUniqueId());

        if (requests == null || requests.isEmpty()) {
            Utils.errorMessage(target, "No tienes solicitudes de teletransporte pendientes.");
            return;
        }

        TeleportRequest request = requests.stream()
                .filter(req -> req.getSender().getName().equalsIgnoreCase(senderName))
                .findFirst().orElse(null);

        if (request == null) {
            Utils.errorMessage(target, "No tienes solicitudes de teletransporte pendientes de " + senderName + ".");
            return;
        }

        request.getTimeoutTask().cancel();
        requests.remove(request);
        if (requests.isEmpty()) {
            pendingRequests.remove(target.getUniqueId());
        }

        Player sender = request.getSender();
        if (sender == null || !sender.isOnline()) {
            Utils.errorMessage(target, "El jugador que te solicitó el teletransporte ya no está disponible.");
            return;
        }

        final int cooldownSeconds = 5;
        final int[] counter = { cooldownSeconds };
        
        // Determinar quién se teletransporta según el tipo de solicitud
        Player playerToTeleport = request.isTpaHere() ? target : sender;
        Player destination = request.isTpaHere() ? sender : target;
        
        Utils.infoMessage(playerToTeleport, "Teleportación en " + cooldownSeconds + " segundos...");

        BukkitTask tornadoTask = Bukkit.getScheduler().runTaskTimer(VitalCore.getPlugin(), () -> {
            spawnTornadoEffect(playerToTeleport);
        }, 0L, 2L);

        final BukkitTask[] cooldownTaskHolder = new BukkitTask[1];
        cooldownTaskHolder[0] = Bukkit.getScheduler().runTaskTimer(VitalCore.getPlugin(), () -> {
            if (counter[0] <= 0) {
                // Registrar ubicación previa antes del teletransporte
                BackManager.setPreviousLocation(playerToTeleport);
                
                playerToTeleport.teleport(destination.getLocation());

                if (request.isTpaHere()) {
                    Utils.successMessage(target, "Te has teletransportado hacia " + sender.getName() + ".");
                    Utils.successMessage(sender, "Has aceptado que " + target.getName() + " se teletransporte hacia ti.");
                } else {
                    Utils.successMessage(sender, "Has sido teletransportado a " + target.getName() + ".");
                    Utils.successMessage(target, "Has aceptado la solicitud de teletransporte de " + sender.getName() + ".");
                }

                playerToTeleport.playSound(playerToTeleport.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                Bukkit.getScheduler().cancelTask(cooldownTaskHolder[0].getTaskId());
                tornadoTask.cancel();
            } else {
                playerToTeleport.playSound(playerToTeleport.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

                if (counter[0] <= 3) {
                    Utils.infoMessage(playerToTeleport, "Irás en &a" + counter[0] + "&7...");
                }
                counter[0]--;
            }
        }, 0L, 20L);
    }

    public static void denyRequest(Player target, String senderName) {
        List<TeleportRequest> requests = pendingRequests.get(target.getUniqueId());

        if (requests == null || requests.isEmpty()) {
            Utils.errorMessage(target, "No tienes solicitudes de teletransporte pendientes.");
            return;
        }

        TeleportRequest request = requests.stream()
                .filter(req -> req.getSender().getName().equalsIgnoreCase(senderName))
                .findFirst().orElse(null);

        if (request == null) {
            Utils.errorMessage(target, "No tienes solicitudes de teletransporte pendientes de " + senderName + ".");
            return;
        }

        request.getTimeoutTask().cancel();
        requests.remove(request);
        if (requests.isEmpty()) {
            pendingRequests.remove(target.getUniqueId());
        }

        Player sender = request.getSender();
        Utils.infoMessage(target, "Has rechazado la solicitud de teletransporte de " + sender.getName() + ".");
        if (sender != null && sender.isOnline()) {
            Utils.errorMessage(sender, "Tu solicitud de teletransporte a " + target.getName() + " ha sido rechazada.");
        }
    }

    public static void spawnTornadoEffect(Player player) {
        Location base = player.getLocation().add(0, 1, 0);
        double time = (System.currentTimeMillis() % 10000) / 1000.0;
        int particles = 20; // cantidad de puntos circulares
        double maxHeight = 1.0; // altura total del tornado
        double baseRadius = 0.5; // radio inicial
        double radiusIncrement = 0.4; // incremento del radio conforme aumenta la altura

        for (int i = 0; i < particles; i++) {
            // Progresión vertical del tornado
            double heightFraction = (double) i / particles;
            double yOffset = heightFraction * maxHeight;
            // El radio aumenta conforme se eleva el tornado
            double radius = baseRadius + (heightFraction * radiusIncrement);
            // Calcular el ángulo para distribuir uniformemente y añadir rotación en el
            // tiempo
            double angle = 2 * Math.PI * i / particles + time;
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);
            Location particleLoc = base.clone().add(xOffset, yOffset, zOffset);
                        // Usar Particle.REDSTONE con DustOptions para simular polvo (color gris)
            player.getWorld().spawnParticle(Particle.DUST, particleLoc, 1,
                    new Particle.DustOptions(Color.GRAY, 1f));
        }
    }

    // Método para tpahere - solicitar que otro jugador venga hacia ti
    public static void sendTeleportHereRequest(Player sender, Player target) {
        if (target.getUniqueId().equals(sender.getUniqueId())) {
            Utils.errorMessage(sender, "No puedes enviarte una solicitud de teletransporte a ti mismo.");
            return;
        }

        // Verificar si el target tiene las solicitudes TPA desactivadas
        if (!isTpaEnabled(target)) {
            Utils.errorMessage(sender, target.getName() + " tiene las solicitudes de teletransporte desactivadas.");
            return;
        }

        List<TeleportRequest> requests = pendingRequests.getOrDefault(target.getUniqueId(), new ArrayList<>());

        if (requests.stream().anyMatch(req -> req.getSender().getUniqueId().equals(sender.getUniqueId()))) {
            Utils.errorMessage(sender, "Ya tienes una solicitud pendiente para " + target.getName() + ".");
            return;
        }

        // Crear solicitud especial para tpahere (el target viene al sender)
        TeleportRequest request = new TeleportRequest(sender, target, true); // true indica tpahere
        requests.add(request);
        pendingRequests.put(target.getUniqueId(), requests);

        BukkitTask timeoutTask = Bukkit.getScheduler().runTaskLater(
                VitalCore.getPlugin(),
                () -> {
                    List<TeleportRequest> currentRequests = pendingRequests.get(target.getUniqueId());
                    if (currentRequests != null && currentRequests.remove(request)) {
                        Utils.errorMessage(sender,
                                "Tu solicitud de teletransporte hacia ti de " + target.getName() + " ha expirado.");
                        Utils.errorMessage(target,
                                "La solicitud de teletransporte hacia " + sender.getName() + " ha expirado.");
                        if (currentRequests.isEmpty()) {
                            pendingRequests.remove(target.getUniqueId());
                        }
                    }
                },
                REQUEST_TIMEOUT_SECONDS * 20L);
        request.setTimeoutTask(timeoutTask);

        // Notificar a ambos jugadores
        ClickableMessage confirmMsg = new ClickableMessage("&aAceptar", "tpaccept " + sender.getDisplayName(),
                "&7Haz clic para aceptar.");
        ClickableMessage rejectMsg = new ClickableMessage("&cRechazar", "tpadeny " + sender.getDisplayName(),
                "&7Haz clic para rechazar.");

        String targetMessage = "&8[&7VitalCore&8] -> &6${sender:name} &7quiere que te teletransportes hacia él! &7(${clk:accept} &b|| ${clk:reject}&7)";
        targetMessage = targetMessage.replace("${sender:name}", sender.getDisplayName());
        targetMessage = targetMessage.replace("${clk:accept}", confirmMsg.getMessage());
        targetMessage = targetMessage.replace("${clk:reject}", rejectMsg.getMessage());

        Utils.sendConfirmationClickableMessage(target, targetMessage, confirmMsg, rejectMsg);
        Utils.infoMessage(sender,
                "Solicitud de teletransporte hacia ti enviada a " + target.getName() + ". Esperando respuesta...");
    }

    // Métodos para el toggle de TPA
    public static boolean getTeleportToggleStatus(Player player) {
        return tpaToggleStatus.getOrDefault(player.getUniqueId(), true); // Por defecto habilitado
    }

    public static void setTeleportToggleStatus(Player player, boolean status) {
        tpaToggleStatus.put(player.getUniqueId(), status);
    }
}
