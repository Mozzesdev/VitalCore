package me.winflix.vitalcore.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Listener para bloquear tab completion de comandos sin permisos
 */
public class CommandTabCompleteListener implements Listener {

    // Lista de comandos que requieren permisos específicos (hardcoded por ahora)
    private static final List<String> RESTRICTED_COMMANDS = Arrays.asList(
        // Comandos administrativos básicos
        "op", "deop", "ban", "pardon", "kick", "stop", "reload",
        "whitelist", "gamemode", "give", "tp", "teleport",
        
        // Comandos de WorldEdit (si está instalado)
        "worldedit", "we", "//set", "//replace", "//copy", "//paste",
        "//undo", "//redo", "//wand", "//pos1", "//pos2",
        
        // Comandos de EssentialsX (si está instalado)  
        "essentials", "ess", "god", "fly", "speed", "heal", "feed",
        "invsee", "enderchest", "vanish", "socialspy", "sudo",
        
        // Comandos de LuckPerms
        "luckperms", "lp", "perm", "permissions",
        
        // Comandos de otros plugins comunes
        "worldguard", "wg", "region", "rg",
        "multiverse", "mv", "mvtp", "mvinv",
        "vault", "economy", "eco", "money",
        
        // Comandos de servidores
        "bukkit", "spigot", "paper", "plugins", "pl", "version", "ver",
        "timings", "spark", "lag", "tps", "gc", "restart"
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        // Solo procesar si es un jugador
        if (!(event.getSender() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getSender();
        String buffer = event.getBuffer().toLowerCase().trim();
        
        // Si el buffer está vacío o no empieza con /, no hacer nada
        if (buffer.isEmpty() || !buffer.startsWith("/")) {
            return;
        }

        // Extraer el comando (sin el /)
        String command = buffer.substring(1);
        String[] parts = command.split(" ");
        String baseCommand = parts[0];

        // Verificar si el comando base está en la lista restringida
        if (isRestrictedCommand(baseCommand)) {
            // Verificar permisos usando el sistema nativo de Bukkit (compatible con LuckPerms)
            if (!hasCommandPermission(player, baseCommand)) {
                // Limpiar completamente las sugerencias
                event.setCompletions(new ArrayList<>());
                return;
            }
        }

        // Si llegamos aquí, el jugador tiene permisos o el comando no está restringido
        // Filtrar sugerencias individuales que también podrían estar restringidas
        List<String> filteredCompletions = new ArrayList<>();
        
        for (String completion : event.getCompletions()) {
            // Si la sugerencia es un comando (empieza con /), verificar permisos
            if (completion.startsWith("/")) {
                String suggestedCommand = completion.substring(1).split(" ")[0];
                if (isRestrictedCommand(suggestedCommand)) {
                    if (hasCommandPermission(player, suggestedCommand)) {
                        filteredCompletions.add(completion);
                    }
                    // Si no tiene permisos, no agregar la sugerencia
                } else {
                    // Comando no restringido, agregarlo
                    filteredCompletions.add(completion);
                }
            } else {
                // No es un comando, agregarlo (argumentos, nombres de jugadores, etc.)
                filteredCompletions.add(completion);
            }
        }
        
        event.setCompletions(filteredCompletions);
    }

    /**
     * Verifica si un comando está en la lista de comandos restringidos
     * @param command El comando a verificar (sin /)
     * @return true si está restringido, false en caso contrario
     */
    private boolean isRestrictedCommand(String command) {
        return RESTRICTED_COMMANDS.contains(command.toLowerCase());
    }

    /**
     * Verifica si un jugador tiene permisos para ejecutar un comando
     * Este método es compatible con LuckPerms y otros sistemas de permisos
     * @param player El jugador
     * @param command El comando (sin /)
     * @return true si tiene permisos, false en caso contrario
     */
    private boolean hasCommandPermission(Player player, String command) {
        // Verificación básica: si es OP, tiene todos los permisos
        if (player.isOp()) {
            return true;
        }

        // Lista de permisos a verificar para el comando
        List<String> permissionsToCheck = getPermissionsForCommand(command);
        
        // Verificar si el jugador tiene alguno de los permisos necesarios
        for (String permission : permissionsToCheck) {
            if (player.hasPermission(permission)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Obtiene la lista de permisos posibles para un comando dado
     * @param command El comando (sin /)
     * @return Lista de permisos a verificar
     */
    private List<String> getPermissionsForCommand(String command) {
        List<String> permissions = new ArrayList<>();
        
        // Permisos estándar que se verifican para cada comando
        permissions.add("minecraft.command." + command);
        permissions.add("bukkit.command." + command);
        permissions.add("essentials." + command);
        permissions.add("worldedit." + command);
        permissions.add("worldguard." + command);
        permissions.add("luckperms." + command);
        permissions.add("*"); // Permiso de administrador total
        
        // Permisos específicos para comandos comunes
        switch (command.toLowerCase()) {
            case "op":
            case "deop":
                permissions.add("minecraft.command.op");
                permissions.add("bukkit.command.op");
                break;
            case "ban":
            case "pardon":
            case "kick":
                permissions.add("minecraft.command.ban");
                permissions.add("bukkit.command.ban");
                break;
            case "gamemode":
            case "gm":
                permissions.add("minecraft.command.gamemode");
                permissions.add("essentials.gamemode");
                break;
            case "tp":
            case "teleport":
                permissions.add("minecraft.command.teleport");
                permissions.add("essentials.tp");
                break;
            case "give":
                permissions.add("minecraft.command.give");
                permissions.add("essentials.give");
                break;
            case "plugins":
            case "pl":
                permissions.add("bukkit.command.plugins");
                break;
            case "reload":
                permissions.add("bukkit.command.reload");
                permissions.add("minecraft.command.reload");
                break;
        }
        
        return permissions;
    }
}
