package me.winflix.vitalcore.addons.interfaces;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

/**
 * Interfaz que representa una instancia activa de un modelo en el mundo.
 */
public interface ModelInstance {

    /**
     * Obtiene la definición del modelo procesado asociado a esta instancia.
     * @return El ProcessedBbModel.
     */
    ProcessedBbModel getModel(); // <-- Devuelve el modelo procesado

    /**
     * Obtiene la ubicación base donde se spawneó originalmente la instancia.
     * @return Una copia de la ubicación base.
     */
    Location getBaseLocation();

    /**
     * Obtiene el jugador dueño de esta instancia (puede ser null).
     * @return El Player dueño o null.
     */
    Player getOwner();

    /**
     * Obtiene el UUID único asignado a esta instancia específica.
     * @return El UUID de la instancia.
     */
    UUID getInstanceUuid();

    /**
     * Obtiene el mapa que relaciona los UUIDs de los huesos del modelo
     * con las entidades ItemDisplay que los representan visualmente.
     * La modificación de este mapa debe hacerse con cuidado (preferiblemente
     * a través de métodos en la implementación si se requiere).
     * @return El mapa de Bone UUID a ItemDisplay.
     */
    Map<String, ItemDisplay> getBoneEntities();

    /**
     * Obtiene el ModelHandler responsable de gestionar
     * la lógica (tick, destroy) de esta instancia específica.
     * @return El ModelHandler asociado.
     */
    ModelHandler getHandler();
}