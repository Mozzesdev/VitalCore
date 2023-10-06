package me.winflix.vitalcore.citizen.interfaces;

import me.winflix.vitalcore.citizen.models.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface EntityController {
    void create(Location at, NPC npc);

     void die();

    Entity getBukkitEntity();

    void remove();

    boolean spawn(Location at);
}
