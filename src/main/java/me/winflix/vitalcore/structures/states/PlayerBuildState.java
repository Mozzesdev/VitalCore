package me.winflix.vitalcore.structures.states;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.winflix.vitalcore.structures.interfaces.BuildStatus;
import me.winflix.vitalcore.structures.models.Structure;

public class PlayerBuildState {

    // Usamos un mapa donde el UUID del jugador mapea a otro mapa que asocia cada estructura con su estado
    private Map<UUID, Map<Structure, BuildStatus>> playerStructures = new HashMap<>();

    // Verifica si el jugador está construyendo algo
    public boolean isPlayerBuilding(Player player) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        return structures != null && structures.containsValue(BuildStatus.IN_PROGRESS);
    }

    // Comienza a construir una estructura
    public void startBuilding(Player player, Structure structure) {
        playerStructures
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(structure, BuildStatus.IN_PROGRESS);
    }

    // Marca una estructura como completada
    public void finishBuilding(Player player, Structure structure) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        if (structures != null && structures.containsKey(structure)) {
            structures.put(structure, BuildStatus.COMPLETED);
        }
    }

    // Marca una estructura como fallida (por ejemplo, si no puede construirse)
    public void failBuilding(Player player, Structure structure) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        if (structures != null && structures.containsKey(structure)) {
            structures.put(structure, BuildStatus.FAILED);
        }
    }

    // Cancela la construcción de una estructura
    public void cancelBuilding(Player player, Structure structure) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        if (structures != null && structures.containsKey(structure)) {
            structures.put(structure, BuildStatus.CANCELLED);
        }
    }

    // Obtiene el estado de construcción de una estructura
    public BuildStatus getBuildStatus(Player player, Structure structure) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        return structures != null ? structures.getOrDefault(structure, BuildStatus.NOT_STARTED) : BuildStatus.NOT_STARTED;
    }

    // Obtiene todas las estructuras que están en progreso para un jugador
    public Set<Structure> getStructuresInProgress(Player player) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        if (structures == null) return Collections.emptySet();
        Set<Structure> inProgress = new HashSet<>();
        for (Map.Entry<Structure, BuildStatus> entry : structures.entrySet()) {
            if (entry.getValue() == BuildStatus.IN_PROGRESS) {
                inProgress.add(entry.getKey());
            }
        }
        return inProgress;
    }

    // Obtiene todas las estructuras que han sido completadas para un jugador
    public Set<Structure> getCompletedStructures(Player player) {
        Map<Structure, BuildStatus> structures = playerStructures.get(player.getUniqueId());
        if (structures == null) return Collections.emptySet();
        Set<Structure> completed = new HashSet<>();
        for (Map.Entry<Structure, BuildStatus> entry : structures.entrySet()) {
            if (entry.getValue() == BuildStatus.COMPLETED) {
                completed.add(entry.getKey());
            }
        }
        return completed;
    }
}
