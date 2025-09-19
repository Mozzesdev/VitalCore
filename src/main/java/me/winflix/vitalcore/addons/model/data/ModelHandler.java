package me.winflix.vitalcore.addons.model.data;

import javax.annotation.Nullable;

/**
 * Define la lógica para crear, actualizar (tick) y destruir
 * diferentes tipos de instancias de modelos (vinculadas a entidades, estáticas,
 * etc.).
 * Cada implementación manejará un tipo específico de instancia definido por
 * ModelContext.InstanceType.
 */
public interface ModelHandler {

    /**
     * Crea una nueva instancia de un modelo basado en el contexto proporcionado.
     * La implementación específica determinará cómo se utiliza el contexto,
     * por ejemplo, si se necesita una entidad base, si se vincula a un bloque, etc.
     *
     * @param model   El modelo PROCESADO (ProcessedBbModel) que se va a instanciar.
     * @param context El contexto que contiene la información sobre cómo y dónde
     *                crear la instancia (tipo, ubicación, entidad/bloque objetivo,
     *                etc.).
     * @return La ModelInstance creada y configurada, o null si la creación falla.
     */
    @Nullable
    ModelInstance createInstance(ProcessedBbModel model, ModelContext context);

    /**
     * Ejecuta la lógica de actualización para cada tick del servidor para una
     * instancia de modelo específica.
     * Esto puede incluir la actualización de animaciones, la sincronización de la
     * posición (si está vinculada a una entidad), la comprobación de condiciones
     * (como la existencia de un bloque), etc.
     *
     * @param instance La instancia del modelo a actualizar.
     */
    void tick(ModelInstance instance);

    /**
     * Destruye una instancia de modelo, limpiando todos los recursos asociados.
     * Esto incluye eliminar las entidades visuales (ItemDisplay), desvincularla
     * de entidades base o bloques (si aplica), y cualquier otra limpieza necesaria.
     *
     * @param instance La instancia del modelo a destruir.
     */
    void destroy(ModelInstance instance);

}