package me.winflix.vitalcore.tribes.events;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.winflix.vitalcore.general.database.collections.tribe.UsersDAO;
import me.winflix.vitalcore.tribes.models.User;

public class JoinEvents implements Listener {

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        User existPlayer = UsersDAO.getUser(uuid);

        if (existPlayer == null) {
            UsersDAO.createUser(player);
        }
    }
}
