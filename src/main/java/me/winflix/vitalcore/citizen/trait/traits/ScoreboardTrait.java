package me.winflix.vitalcore.citizen.trait.traits;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.collect.Sets;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.citizen.Citizen;
import me.winflix.vitalcore.citizen.enums.DespawnReason;
import me.winflix.vitalcore.citizen.interfaces.Trait;
import me.winflix.vitalcore.citizen.models.NPC;
import me.winflix.vitalcore.citizen.utils.Util;
import me.winflix.vitalcore.citizen.utils.LocationLookup.PerPlayerMetadata;
import me.winflix.vitalcore.core.nms.NMS;

@TraitName("scoreboardtrait")
public class ScoreboardTrait extends Trait {
    private boolean changed;
    private ChatColor color;
    private String lastName;
    private final PerPlayerMetadata<Boolean> metadata;
    private ChatColor previousGlowingColor;
    private Set<String> tags = new HashSet<String>();

    public ScoreboardTrait() {
        super("scoreboardtrait");
        metadata = Citizen.getLocationLookup().<Boolean> registerMetadata("scoreboard", (meta, event) -> {
            for (NPC npc : Citizen.getNpcs().values()) {
                ScoreboardTrait trait = npc.getTraitNullable(ScoreboardTrait.class);
                if (trait == null)
                    continue;
                Team team = trait.getTeam();
                if (team == null || meta.has(event.getPlayer().getUniqueId(), team.getName()))
                    continue;
                NMS.sendTeamPacket(event.getPlayer(), team, 0);
                meta.set(event.getPlayer().getUniqueId(), team.getName(), true);
            }
        });
    }

    private void clearClientTeams(Team team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (metadata.remove(player.getUniqueId(), team.getName())) {
                NMS.sendTeamPacket(player, team, 1);
            }
        }
    }

    public void createTeam(String entityName) {
        String teamName = Util.getTeamName(npc.getUniqueId());
        npc.getMetaData().set(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, teamName);
        Scoreboard scoreboard = Util.getDummyScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        if (!team.hasEntry(entityName)) {
            clearClientTeams(team);
        }
        team.addEntry(entityName);
    }

    public ChatColor getColor() {
        return color;
    }

    private Team getTeam() {
        String teamName = npc.getMetaData().get(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, "");
        if (teamName.isEmpty())
            return null;
        return Util.getDummyScoreboard().getTeam(teamName);
    }

    @Override
    public void onDespawn(DespawnReason reason) {
        previousGlowingColor = null;
        String name = lastName;
        String teamName = npc.getMetaData().get(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME, "");
        if (teamName.isEmpty())
            return;
        Team team = Util.getDummyScoreboard().getTeam(teamName);
        npc.getMetaData().remove(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME);
        if (team == null || name == null || !team.hasEntry(name))
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(VitalCore.getPlugin(), () -> {
            if (npc.isSpawned())
                return;
            try {
                team.getSize();
            } catch (IllegalStateException ex) {
                return;
            }
            if (team.getSize() == 1) {
                clearClientTeams(team);
                team.unregister();
            } else {
                team.removeEntry(name);
            }
        }, reason == DespawnReason.DEATH && npc.getEntity() instanceof LivingEntity ? 20 : 2);
    }

    @Override
    public void onRemove() {
        onDespawn();
    }

    @Override
    public void onSpawn() {
        changed = true;
        if (SUPPORT_TAGS) {
            try {
                npc.getEntity().getScoreboardTags().clear();
                npc.getEntity().getScoreboardTags().addAll(tags);
            } catch (NoSuchMethodError e) {
                SUPPORT_TAGS = false;
            }
        }

    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    public void update() {
        String forceVisible = npc.getMetaData().<Object> get(NPC.Metadata.NAMEPLATE_VISIBLE, true).toString();
        boolean nameVisibility = !npc.requiresNameHologram()
                && (forceVisible.equals("true") || forceVisible.equals("hover"));
        Team team = getTeam();
        if (team == null)
            return;

        if (!USE_SCOREBOARD_TEAMS) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                metadata.remove(player.getUniqueId(), team.getName());
                NMS.sendTeamPacket(player, team, 1);
            }
            team.unregister();
            npc.getMetaData().remove(NPC.Metadata.SCOREBOARD_FAKE_TEAM_NAME);
            return;
        }

        if (npc.isSpawned()) {
            lastName = npc.getEntity() instanceof Player && npc.getEntity().getName() != null
                    ? npc.getEntity().getName()
                    : npc.getUniqueId().toString();
        }

        if (SUPPORT_TAGS) {
            try {
                if (!npc.getEntity().getScoreboardTags().equals(tags)) {
                    tags = Sets.newHashSet(npc.getEntity().getScoreboardTags());
                }
            } catch (NoSuchMethodError e) {
                SUPPORT_TAGS = false;
            }
        }

        if (SUPPORT_TEAM_SETOPTION) {
            try {
                OptionStatus visibility = nameVisibility ? OptionStatus.ALWAYS : OptionStatus.NEVER;
                if (visibility != team.getOption(Option.NAME_TAG_VISIBILITY)) {
                    changed = true;
                }
                team.setOption(Option.NAME_TAG_VISIBILITY, visibility);
            } catch (NoSuchMethodError e) {
                SUPPORT_TEAM_SETOPTION = false;
            } catch (NoClassDefFoundError e) {
                SUPPORT_TEAM_SETOPTION = false;
            }
        } else {
            NMS.setTeamNameTagVisible(team, nameVisibility);
        }

        if (SUPPORT_COLLIDABLE_SETOPTION) {
            try {
                OptionStatus collide = npc.getMetaData().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected())
                        ? OptionStatus.ALWAYS
                        : OptionStatus.NEVER;
                if (collide != team.getOption(Option.COLLISION_RULE)) {
                    changed = true;
                }
                team.setOption(Option.COLLISION_RULE, collide);
            } catch (NoSuchMethodError e) {
                SUPPORT_COLLIDABLE_SETOPTION = false;
            } catch (NoClassDefFoundError e) {
                SUPPORT_COLLIDABLE_SETOPTION = false;
            }
        }

        if (color != null) {
            if (SUPPORT_GLOWING_COLOR && Util.getMinecraftRevision().contains("1_12_R1")) {
                SUPPORT_GLOWING_COLOR = false;
            }
            if (SUPPORT_GLOWING_COLOR) {
                try {
                    if (team.getColor() == null || previousGlowingColor == null
                            || (previousGlowingColor != null && color != previousGlowingColor)) {
                        team.setColor(color);
                        previousGlowingColor = color;
                        changed = true;
                    }
                } catch (NoSuchMethodError err) {
                    SUPPORT_GLOWING_COLOR = false;
                }
            } else {
                if (team.getPrefix() == null || team.getPrefix().length() == 0 || previousGlowingColor == null
                        || (previousGlowingColor != null
                                && !team.getPrefix().equals(previousGlowingColor.toString()))) {
                    team.setPrefix(color.toString());
                    previousGlowingColor = color;
                    changed = true;
                }
            }
        }

        if (!changed)
            return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasMetadata("NPC"))
                continue;
            if (metadata.has(player.getUniqueId(), team.getName())) {
                NMS.sendTeamPacket(player, team, 2);
            } else {
                NMS.sendTeamPacket(player, team, 0);
                metadata.set(player.getUniqueId(), team.getName(), true);
            }
        }
    }

    private static boolean SUPPORT_COLLIDABLE_SETOPTION = true;
    private static boolean SUPPORT_GLOWING_COLOR = true;
    private static boolean SUPPORT_TAGS = true;
    private static boolean SUPPORT_TEAM_SETOPTION = true;
    private static boolean USE_SCOREBOARD_TEAMS = true;
}