package me.winflix.vitalcore.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.winflix.vitalcore.general.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Sound;

import java.util.regex.Matcher;

public class ChatManager {

    private final List<String> bannedWords = new ArrayList<>();
    private final List<String> whitelistedUrls = new ArrayList<>();
    private final String mentionPrefix;
    private final String mentionEveryone;

    private boolean increasedSensitivity = false;
    private String advertisementCommand = "";

    public ChatManager() {
        bannedWords.add("palabrota1");
        bannedWords.add("palabrota2");

        this.mentionPrefix = "@";
        this.mentionEveryone = "@everyone";

        whitelistedUrls.add("tuservidor.com");
        whitelistedUrls.add("discord.gg/abc123");
    }

    public void setIncreasedSensitivity(boolean increasedSensitivity) {
        this.increasedSensitivity = increasedSensitivity;
    }

    public void setAdvertisementCommand(String command) {
        this.advertisementCommand = command;
    }

    public void setWhitelistedUrls(List<String> urls) {
        this.whitelistedUrls.clear();
        for (String url : urls) {
            this.whitelistedUrls.add(url.toLowerCase());
        }
    }

    private String formatAndModerateMessage(String message) {
        String cleanMessage = censorMessage(message);
        return Utils.useColors(cleanMessage);
    }

    private String censorMessage(String message) {
        String censored = message;
        for (String banned : bannedWords) {
            String regex = "(?i)\\b" + Pattern.quote(banned) + "\\b";
            String replacement = "*".repeat(banned.length());
            censored = censored.replaceAll(regex, replacement);
        }
        return censored;
    }

    private boolean containsAdvertisement(String message, Player sender) {
        Pattern urlPattern = Pattern.compile("((https?://)?(www\\.)?([\\w-]+)\\.[\\w]{2,})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(message);

        while (matcher.find()) {
            String foundUrl = matcher.group(1).toLowerCase();
            boolean allowed = whitelistedUrls.stream().anyMatch(foundUrl::contains);
            if (!allowed)
                return true;
        }

        if (increasedSensitivity) {
            String[] adKeywords = { "mc-server", "buycraft", "store", "ip:", "server:" };
            for (String keyword : adKeywords) {
                if (message.toLowerCase().contains(keyword))
                    return true;
            }
        }
        return false;
    }

    private void notifyStaff(Player offender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("antiadvertising.notify") && !player.equals(offender)) {
                Utils.infoMessage(player, "&cEl jugador &e" + offender.getName() + " &cintentó hacer publicidad.");
            }
        }
    }

    private void executeAdvertisementCommand(Player offender) {
        if (!advertisementCommand.isEmpty()) {
            String command = advertisementCommand.replace("{player}", offender.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public void sendPublicMessage(Player sender, String message) {
        if (containsAdvertisement(message, sender)) {
            Utils.errorMessage(sender, "&cNo puedes hacer publicidad a otros servidores.");
            notifyStaff(sender);
            executeAdvertisementCommand(sender);
            return;
        }

        String formattedMessage = formatAndModerateMessage(message);
        notifyMentions(sender, formattedMessage);
        String finalMessage = Utils.useColors("&7" + sender.getName() + ": ") + formattedMessage;
        Bukkit.getServer().broadcastMessage(finalMessage);
    }

    public void sendPrivateMessage(Player sender, Player receiver, String message) {
        if (containsAdvertisement(message, sender)) {
            Utils.errorMessage(sender, "&cNo puedes enviar mensajes con publicidad.");
            notifyStaff(sender);
            executeAdvertisementCommand(sender);
            return;
        }

        String formattedMessage = formatAndModerateMessage(message);
        notifyMentions(sender, formattedMessage);

        receiver.sendMessage(Utils.useColors("&a[Privado de " + sender.getName() + "]: ") + formattedMessage);
        sender.sendMessage(Utils.useColors("&a[Privado a " + receiver.getName() + "]: ") + formattedMessage);
    }

    private void notifyMentions(Player sender, String message) {
        if (message.contains(mentionEveryone)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.equals(sender))
                    notifyPlayerMentioned(player);
            }
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(sender))
                continue;

            String pattern = Pattern.quote(mentionPrefix) + player.getName();
            Pattern mentionPattern = Pattern.compile("(?i)" + pattern);
            Matcher matcher = mentionPattern.matcher(message);
            if (matcher.find()) {
                notifyPlayerMentioned(player);
            }
        }
    }

    private void notifyPlayerMentioned(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
        player.sendTitle(Utils.useColors("&e¡Te han mencionado!"), "", 10, 40, 10);
    }

    public void setBannedWords(List<String> bannedWords) {
        this.bannedWords.clear();
        this.bannedWords.addAll(bannedWords);
    }
}