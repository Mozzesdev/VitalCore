package me.winflix.vitalcore.residents.utils.controllers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import me.winflix.vitalcore.residents.interfaces.NPC;

public interface EntityController {
    void create(Location at, NPC npc);

     void die();

    Entity getBukkitEntity();

    void remove();

    boolean spawn(Location at);
}
