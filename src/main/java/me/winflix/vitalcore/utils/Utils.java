package me.winflix.vitalcore.utils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.models.TribeMember;
import me.winflix.vitalcore.models.TribeModel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Utils {

    public static String useColors(String value) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher colorMatcher = pattern.matcher(value);

        while (colorMatcher.find()) {
            String color = value.substring(colorMatcher.start(), colorMatcher.end());
            value = value.replace(color, ChatColor.of(color) + "");

            colorMatcher = pattern.matcher(value);
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public static void logMessage(Player p, String message) {
        p.sendMessage(useColors(VitalCore.prefix + message));
    }

    public static void sendClickableCommand(Player player, ClickableMessage... message) {
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

    public static void castMessageToAllMembersTribe(TribeModel tribe, String message) {
        List<TribeMember> membersTribe = tribe.getMembers();
        for (TribeMember member : membersTribe) {
            Player memberPlayer = Bukkit.getPlayer(UUID.fromString(member.getId()));
            if (memberPlayer.isOnline()) {
                memberPlayer.sendMessage(Utils.useColors(message));
            }
        }
    }
}
