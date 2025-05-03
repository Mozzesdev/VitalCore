package me.winflix.vitalcore.core.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.core.Core;
import me.winflix.vitalcore.general.commands.BaseCommand;

public class SetSpawn extends BaseCommand {
    
    @Override
    public String getName() {
        return "setspawn";
    }
    
    @Override
    public String getPermission() {
        return "vitalcore.setspawn";
    }
    
    @Override
    public String getSyntax() {
        return "/setspawn";
    }
    
    
    @Override
    public String getVariants() {
        return "";
    }
    
    @Override
    public String getDescription() {
        return "";
    }
    
    @Override
    public List<String> getArguments(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void perform(Player player, String[] args) {
        Core.worldManager.setSpawn(player);
    }

}