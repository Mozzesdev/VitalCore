package me.winflix.vitalcore.addons.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ModelHandler {

    /**
     * Spawnea un modelo en el mundo.
     * @param model El modelo PROCESADO a spawnear. <-- CAMBIO
     * @param location La ubicación base.
     * @param owner El dueño (opcional).
     * @return La instancia del modelo creada.
     */
    ModelInstance spawn(ProcessedBbModel model, Location location, Player owner); // <-- CAMBIO

    /**
     * Ejecuta la lógica de tick para una instancia.
     * @param instance La instancia a actualizar.
     */
    void tick(ModelInstance instance);

    /**
     * Destruye una instancia y limpia sus entidades.
     * @param instance La instancia a destruir.
     */
    void destroy(ModelInstance instance);

}