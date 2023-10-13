package me.winflix.vitalcore.residents.commands;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.mojang.authlib.properties.Property;

import me.winflix.vitalcore.general.commands.SubCommand;
import me.winflix.vitalcore.residents.models.ResidentNPC;
import me.winflix.vitalcore.skins.utils.SkinGrabber;

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

        ResidentNPC npc = new ResidentNPC(loc, npcName, type);

        if (args.length >= 2) {
            Property propertySkin = SkinGrabber.fetchSkinByName(npcName);

            if (propertySkin != null) {
                npc.setSkin(propertySkin);
            }
        }

        npc.spawn(loc);

    }

}
