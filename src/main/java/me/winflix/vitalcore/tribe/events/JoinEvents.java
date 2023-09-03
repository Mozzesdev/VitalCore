package me.winflix.vitalcore.tribe.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.database.collections.TribesCollection;
import me.winflix.vitalcore.general.database.collections.UsersCollection;
import me.winflix.vitalcore.tribe.models.Tribe;
import me.winflix.vitalcore.tribe.models.User;

public class JoinEvents implements Listener {

    VitalCore plugin;

    public JoinEvents(VitalCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) throws Exception {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        User existPlayer = UsersCollection.getUserWithTribe(uuid);

        if (existPlayer == null) {
            Tribe tribeDocument = TribesCollection.createTribe(player);
            UsersCollection.createUser(player, uuid, tribeDocument);
        }
    }
}
