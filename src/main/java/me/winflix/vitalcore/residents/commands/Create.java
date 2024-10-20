package me.winflix.vitalcore.residents.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.mojang.authlib.properties.Property;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.residents.Residents;
import me.winflix.vitalcore.residents.models.ResidentNPC;
import me.winflix.vitalcore.residents.trait.traits.LookClose;
import me.winflix.vitalcore.skins.utils.SkinGrabber;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Create extends SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getVariants() {
        return "cr";
    }

    @Override
    public String getDescription() {
        return "Create a NPC";
    }

    @Override
    public String getPermission() {
        return "vitalcore.npc.bypass";
    }

    @Override
    public String getSyntax() {
        return "npc create [name] --look";
    }

    public String[] getFlags() {
        String[] flags = { "--look", "--at", "--baby", "--skin" };
        return flags;
    }

    @Override
    public List<String> getSubCommandArguments(Player sender, String[] args) {
        String[] flags = getFlags();
        List<String> argsFlag = new ArrayList<>();
        List<String> flagsList = new ArrayList<>(Arrays.asList(flags));

        Arrays.asList(args).forEach(arg -> {
            flagsList.removeIf(flag -> flag.equalsIgnoreCase(arg));
        });

        flags = flagsList.toArray(String[]::new);

        if (args.length >= 3) {
            int lastIndex = args.length - 1;

            if (args[lastIndex - 1].equalsIgnoreCase("--at")) {
                Bukkit.getOnlinePlayers().forEach(p -> argsFlag.add(p.getName()));
                String coordsPattern = "[{0},{1},{2},{3}]";
                MessageFormat message = new MessageFormat(coordsPattern);
                Location loc = sender.getLocation();
                Object[] ob = { Integer.toString(loc.getBlockX()), Integer.toString(loc.getBlockY()),
                        Integer.toString(loc.getBlockZ()), loc.getWorld().getName() };
                argsFlag.add(message.format(ob));
                return argsFlag;
            }

            if (args[lastIndex - 1].equalsIgnoreCase("--skin")) {
                Bukkit.getOnlinePlayers().forEach(p -> argsFlag.add(p.getName()));
                return argsFlag;
            }

            argsFlag.addAll(Arrays.asList(flags));

            return argsFlag;
        }

        return null;
    }

    @Override
    public void perform(Player sender, String[] args) {
        String npcName = "Survivor";
        Location currentLocation = sender.getLocation();
        EntityType type = EntityType.PLAYER;
        Property propertySkin = null;

        if (args.length >= 2) {
            npcName = args[1];
        }

        ResidentNPC npc = new ResidentNPC(currentLocation, npcName, type);

        String successMessage = "&7[&aVitalCore&7] &7-> &aEl NPC &b" + npcName
                + " &afue creado con éxito en las coordenadas";
        List<String> flagsFound = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--look")) {
                npc.getOrAddTrait(LookClose.class);
                flagsFound.add("--look");
            } else if (args[i].equalsIgnoreCase("--at")) {
                if (i + 1 < args.length) {
                    String coordinates = args[i + 1];
                    if (coordinates.startsWith("[") && coordinates.endsWith("]")) {
                        if (isValidCoordinates(coordinates)) {
                            Location npcLocation = parseCoordinates(coordinates, sender);
                            if (npcLocation != null) {
                                npc.setLocation(npcLocation);
                                flagsFound.add("--at");
                            } else {
                                Utils.errorMessage(sender, "No se pudo establecer la ubicación del NPC.");
                                return;
                            }
                        } else {
                            Utils.errorMessage(sender, "Las coordenadas después de --at no son válidas.");
                            return;
                        }
                    } else {
                        Player targetSpawn = Bukkit.getPlayerExact(coordinates);
                        if (targetSpawn == null || !targetSpawn.isOnline()) {
                            Utils.errorMessage(sender, "El jugador después de --at no se encuentra online.");
                            return;
                        }
                        npc.setLocation(targetSpawn.getLocation());
                        flagsFound.add("--at");
                    }
                } else {
                    Utils.errorMessage(sender,
                            "Debes proporcionar coordenadas después de --at o el nombre de un jugador conectado.");
                    return;
                }
            } else if (args[i].equalsIgnoreCase("--skin")) {
                if (i + 1 < args.length) {
                    propertySkin = SkinGrabber.fetchSkinByName(args[i + 1]);
                    flagsFound.add("--skin");
                } else {
                    Utils.errorMessage(sender,
                            "Debes proporcionar el nombre de una skin despues del --skin.");
                    return;
                }
            }
        }

        npc.spawn();

        if (!flagsFound.contains("--skin")) {
            propertySkin = SkinGrabber.fetchSkinByName(npcName);
        }

        if (propertySkin != null) {
            npc.setSkin(propertySkin);
        }

        TextComponent coordinatesComponent = createCoordinatesComponent(npc.getLocation());

        TextComponent finalMessage = new TextComponent(Utils.useColors(successMessage));
        finalMessage.addExtra(coordinatesComponent);

        if (!flagsFound.isEmpty()) {
            finalMessage.addExtra(
                    Utils.useColors("&acon las siguientes propiedades: &b" + String.join("&a, &b", flagsFound)));
        }

        sender.spigot().sendMessage(finalMessage);

        Residents.getNpcs().put(npc.getUniqueId(), npc);
    }

    private String formatCoordinates(Location loc) {
        return Utils.useColors(" &b[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ", "+ loc.getWorld().getName() + "] ");
    }

    private TextComponent createCoordinatesComponent(Location loc) {
        String formattedCoordinates = formatCoordinates(loc);

        TextComponent coordinatesComponent = new TextComponent(TextComponent.fromLegacyText(formattedCoordinates));
        coordinatesComponent.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(Utils.useColors("&6Haz clic para ir al NPC"))));
        coordinatesComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));

        return coordinatesComponent;
    }

    private boolean isValidCoordinates(String coordinates) {
        return coordinates.matches(".*\\[(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?),(-?\\d+(\\.\\d+)?),([a-zA-Z_]+)]");
    }

    private Location parseCoordinates(String coordinates, Player sender) {
        String[] parts = coordinates.substring(1, coordinates.length() - 1).split(",");
        if (parts.length == 4) {
            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                String worldName = parts[3];
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    return new Location(world, x, y, z);
                } else {
                    Utils.errorMessage(sender, "El mundo especificado no existe.");
                }
            } catch (NumberFormatException e) {
                Utils.errorMessage(sender, "Las coordenadas no son números válidos.");
            }
        } else {
            Utils.errorMessage(sender, "El formato de las coordenadas es incorrecto.");
        }
        return null;
    }

}
