package me.winflix.vitalcore.citizen.commands;

import com.mojang.authlib.properties.Property;
import me.winflix.vitalcore.citizen.enums.SpawnReason;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.trait.traits.MobTrait;
import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.skins.utils.SkinGrabber;
import net.minecraft.world.entity.MobType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

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
        return "npc create [name]";
    }

    @Override
    public List<String> getSubCommandArguments(Player player, String[] args) {
        return null;
    }

    @Override
    public void perform(Player player, String[] args) {

        String npcName = "Survivor";
        Location loc = player.getLocation();
        EntityType type = EntityType.PLAYER;

        if (args.length >= 2) {
            npcName = args[1];
        }

        NPC npc = new NPC(loc, npcName, type);

        if (args.length >= 2) {
            Property propertySkin = SkinGrabber.fetchSkinByName(npcName);

            if (propertySkin != null) {
                npc.setSkin(propertySkin);
            }
        }

        npc.getOrAddTrait(MobTrait.class).setType(type);

        npc.spawn(loc, SpawnReason.CREATE);

    }

}
