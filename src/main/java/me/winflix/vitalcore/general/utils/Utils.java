package me.winflix.vitalcore.general.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.menu.Menu;
import me.winflix.vitalcore.general.models.PlayerMenuUtility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Utils {
    public static String ERROR_PREFIX = "";
    public static String SUCCESS_PREFIX = "";
    public static String INFO_PREFIX = "";

    public static String useColors(String value) {
        // Aplicar códigos hexadecimales primero
        Pattern colorPattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher colorMatcher = colorPattern.matcher(value);

        while (colorMatcher.find()) {
            String color = value.substring(colorMatcher.start(), colorMatcher.end());
            value = value.replace(color, ChatColor.of(color) + "");

            colorMatcher = colorPattern.matcher(value);
        }

        // Aplicar códigos de formato de Minecraft
        value = ChatColor.translateAlternateColorCodes('&', value);

        return value;
    }

    public static void openMenu(Player player, Class<? extends Menu> menu) {
        if (menu == null) {
            return;
        }

        try {
            Constructor<? extends Menu> constructor = menu.getDeclaredConstructor(PlayerMenuUtility.class);
            Menu instance = constructor.newInstance(VitalCore.getPlayerMenuUtility(player));
            instance.open();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Location locationfromString(String locationString) {
        // Remover "Location{" al principio y "}" al final
        locationString = locationString.replace("Location{", "").replace("}", "");

        // Separar los valores por las comas
        String[] parts = locationString.split(",");

        // Extraer los valores individuales
        String worldName = parts[0].split("=")[1];
        double x = Double.parseDouble(parts[1].split("=")[1]);
        double y = Double.parseDouble(parts[2].split("=")[1]);
        double z = Double.parseDouble(parts[3].split("=")[1]);
        float pitch = Float.parseFloat(parts[4].split("=")[1]);
        float yaw = Float.parseFloat(parts[5].split("=")[1]);

        // Obtener el mundo de Bukkit
        World world = Bukkit.getWorld(worldName);

        // Devolver la nueva instancia de Location
        if (world != null) {
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            throw new IllegalArgumentException("El mundo '" + worldName + "' no existe.");
        }
    }

    public static void infoMessage(Player p, String message) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(Placeholders.PLUGIN_NAME, VitalCore.getPlugin().getName());
        String messageFormat = Placeholders.replacePlaceholders(INFO_PREFIX + message, placeholders);
        p.sendMessage(useColors(messageFormat));
    }

    public static void errorMessage(Player p, String message) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(Placeholders.PLUGIN_NAME, VitalCore.getPlugin().getName());
        String messageFormat = Placeholders.replacePlaceholders(ERROR_PREFIX + message, placeholders);
        p.sendMessage(useColors(messageFormat));
    }

    public static void successMessage(Player p, String message) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(Placeholders.PLUGIN_NAME, VitalCore.getPlugin().getName());
        String messageFormat = Placeholders.replacePlaceholders(SUCCESS_PREFIX + message, placeholders);
        p.sendMessage(useColors(messageFormat));
    }

    public static String formatUUIDString(String uuidString) {
        if (uuidString.length() == 32) {
            return uuidString.substring(0, 8) + "-" +
                    uuidString.substring(8, 12) + "-" +
                    uuidString.substring(12, 16) + "-" +
                    uuidString.substring(16, 20) + "-" +
                    uuidString.substring(20);
        }
        return uuidString;
    }

    @SuppressWarnings("deprecation")
    public static void sendConfirmationClickableMessage(Player target, String message, ClickableMessage confirm,
            ClickableMessage reject) {
        ComponentBuilder component = new ComponentBuilder();

        String regex = "(?=&[0-9a-fA-Fk-oK-OrR])";
        String[] parts = message.split(regex);

        // Realizar los reemplazos de espacios una vez
        String confirmKeyword = confirm.getMessage().replaceAll(" ", "").toLowerCase();
        String rejectKeyword = reject.getMessage().replaceAll(" ", "").toLowerCase();

        for (String part : parts) {
            TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(useColors(part)));

            // Comparar sin distinción entre mayúsculas y minúsculas
            String partWithoutSpaces = part.replaceAll(" ", "").toLowerCase();

            if (partWithoutSpaces.equals(confirmKeyword)) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + confirm.getCommand()));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(useColors(confirm.getHoverMessage()))));
            }

            if (partWithoutSpaces.equals(rejectKeyword)) {
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + reject.getCommand()));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(useColors(reject.getHoverMessage()))));
            }

            component.append(textComponent);
        }

        target.spigot().sendMessage(component.create());
    }

    /**
     * Spawnea un ítem flotante en la posición dada.
     *
     * @param worldName nombre del mundo (por ejemplo "world")
     * @param x         coordenada X
     * @param y         coordenada Y
     * @param z         coordenada Z
     * @param stack     el ItemStack a mostrar
     */
    public static Item spawnFloatingItem(Location location, ItemStack stack) {
        return location.getWorld().spawn(location, Item.class, item -> {
            item.setItemStack(stack);
            // Desactivar gravedad para que no caiga
            item.setGravity(false);
            // Velocidad cero para que no se desplace
            item.setVelocity(new Vector(0, 0, 0));
            // Opcional: que no se pueda recoger
            item.setPickupDelay(Integer.MAX_VALUE);
            // Opcional: que no pueda romperse o recibir daño
            item.setInvulnerable(true);
        });
    }

    public static void removeFloatingItem(Item item) {
        if (item != null && !item.isDead()) {
            item.remove(); // elimina la entidad del mundo
        }
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

}
