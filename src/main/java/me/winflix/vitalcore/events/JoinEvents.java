package me.winflix.vitalcore.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.database.collections.TribeCollection;
import me.winflix.vitalcore.database.collections.UserCollection;
import me.winflix.vitalcore.files.PlayerStatesFile;
import me.winflix.vitalcore.models.PlayerModel;
import me.winflix.vitalcore.models.TribeModel;

public class JoinEvents implements Listener {

    VitalCore plugin;

    public JoinEvents(VitalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) throws Exception {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerModel existPlayer = UserCollection.getPlayer(uuid);

        if (existPlayer == null) {
            TribeModel tribeDocument = TribeCollection.createTribe(player);
            PlayerModel playerDocument = UserCollection.createPlayer(player, uuid, tribeDocument);
            PlayerStatesFile playerFile = new PlayerStatesFile(plugin,
                    uuid.toString() + ".yml",
                    "players-states", playerDocument);
            playerFile.create();
        }
    }
}
