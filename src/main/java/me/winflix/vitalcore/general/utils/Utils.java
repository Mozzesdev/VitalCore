package me.winflix.vitalcore.general.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
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

    public static void sendClickableAction(Player player, ClickableMessage... message) {
        List<ClickableMessage> messageList = Arrays.asList(message);
        ComponentBuilder component = new ComponentBuilder();
        messageList.forEach(m -> {
            if (m != null) {
                TextComponent current = new TextComponent(TextComponent
                        .fromLegacyText(useColors(m.getMessage())));
                if (m.getHoverMessage() != null) {
                    current.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(useColors(m.getHoverMessage()))));
                }
                if (m.getCommand() != null) {
                    current.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + m.getCommand()));
                } else {
                    current.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ""));
                }
                component.append(current);
            }
        });
        player.spigot().sendMessage(component.create());
    }

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

}
