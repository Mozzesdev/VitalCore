package me.winflix.vitalcore.addons.utils;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class GsonUtils {
    public static void ifPresent(JsonElement element, String key, Consumer<JsonElement> consumer) {
        if (!(element instanceof JsonObject)) {
            return;
        }
        JsonObject object = (JsonObject) element;
        if (!object.has(key)) {
            return;
        }
        consumer.accept(object.get(key));
    }

    public static void ifArray(JsonElement element, String key, Consumer<JsonElement> forEach) {
        if (!(element instanceof JsonObject)) {
            return;
        }
        JsonObject object = (JsonObject) element;
        if (!object.has(key)) {
            return;
        }
        JsonElement value = object.get(key);
        if (value instanceof JsonArray array) {
            array.forEach(forEach::accept);
        }
    }

    public static void ifArray(JsonElement element, String key,
            BiConsumer<Integer, JsonElement> forEach) {
        if (!(element instanceof JsonObject)) {
            return;
        }
        JsonObject object = (JsonObject) element;
        if (!object.has(key)) {
            return;
        }
        JsonElement value = object.get(key);
        if (value instanceof JsonArray array) {
            for (int i = 0; i < array.size(); ++i) {
                forEach.accept(i, array.get(i));
            }
        }
    }

    @Nullable
    public static <T> T get(JsonElement element, String key, Function<JsonElement, T> func) {
        if (!(element instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) element;
        if (!object.has(key)) {
            return null;
        }
        JsonElement value = object.get(key);
        if (value.isJsonNull()) {
            return null;
        }
        try {
            return func.apply(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Nonnull
    public static <T> T get(JsonElement element, String key, Function<JsonElement, T> func,
            @Nonnull T def) {
        T val = get(element, key, func);
        return (val == null) ? def : val;
    }

    @Nonnull
    public static <T> T get(JsonElement element, Function<JsonElement, T> func, T def) {
        try {
            return func.apply(element);
        } catch (Exception ignored) {
            return def;
        }
    }

    public static Float[] getAsFloatArray(JsonElement jsonElement) {
        if (!jsonElement.isJsonArray()) {
            return null;
        }
        JsonArray array = jsonElement.getAsJsonArray();
        return (Float[]) array.asList().stream().map(JsonElement::getAsFloat).toArray(Float[]::new);
    }

    /**
     * Intenta convertir un JsonElement en un Vector3f.
     * El JsonElement debe ser un JsonArray con 3 números flotantes
     * o un JsonObject con las claves "x", "y", "z" y valores numéricos.
     *
     * @param element El JsonElement a convertir.
     * @return Un nuevo objeto Vector3f con los valores extraídos.
     * @throws JsonParseException si el JsonElement no tiene el formato esperado
     *                            o si los valores no son números válidos.
     */
    public static Vector3f getAsVector3f(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new JsonParseException("El JsonElement proporcionado es nulo.");
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() == 3) {
                try {
                    float x = array.get(0).getAsFloat();
                    float y = array.get(1).getAsFloat();
                    float z = array.get(2).getAsFloat();
                    return new Vector3f(x, y, z);
                } catch (NumberFormatException | IllegalStateException e) {
                    throw new JsonParseException("El array JSON contiene elementos no numéricos o inválidos.", e);
                }
            } else {
                throw new JsonParseException(
                        "El array JSON debe contener exactamente 3 elementos para un Vector3f, pero tiene "
                                + array.size() + ".");
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("x") && obj.has("y") && obj.has("z")) {
                try {
                    float x = obj.get("x").getAsFloat();
                    float y = obj.get("y").getAsFloat();
                    float z = obj.get("z").getAsFloat();
                    return new Vector3f(x, y, z);
                } catch (NumberFormatException | IllegalStateException e) {
                    throw new JsonParseException(
                            "El objeto JSON contiene valores no numéricos o inválidos para las claves 'x', 'y' o 'z'.",
                            e);
                }
            } else {
                throw new JsonParseException(
                        "El objeto JSON debe contener las claves 'x', 'y' y 'z' para un Vector3f.");
            }
        } else {
            throw new JsonParseException(
                    "El JsonElement no es un JsonArray ni un JsonObject con el formato esperado para Vector3f.");
        }
    }

    /**
     * Intenta convertir un JsonElement (esperado como array [pitch, yaw, roll] en
     * grados desde Blockbench)
     * en un Quaternionf.
     *
     * @param element El JsonElement que representa la rotación [pitch, yaw, roll]
     *                en grados.
     * @return Un nuevo objeto Quaternionf representando la rotación.
     * @throws JsonParseException si el JsonElement no es un array de 3 números.
     */
    public static Quaternionf getAsQuaternionfFromEuler(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new JsonParseException("El JsonElement proporcionado es nulo.");
        }

        // Esperamos un array [pitch, yaw, roll] en grados
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() == 3) {
                try {
                    // 1. Extraer ángulos en grados
                    float pitchDeg = array.get(0).getAsFloat(); // X
                    float yawDeg = array.get(1).getAsFloat(); // Y
                    float rollDeg = array.get(2).getAsFloat(); // Z

                    // 2. Convertir grados a radianes
                    // Math.toRadians() convierte grados a radianes
                    float pitchRad = (float) Math.toRadians(pitchDeg);
                    float yawRad = (float) Math.toRadians(yawDeg);
                    float rollRad = (float) Math.toRadians(rollDeg);

                    // 3. Crear el cuaternión desde los ángulos de Euler en radianes
                    return new Quaternionf().rotationZYX(rollRad, yawRad, pitchRad);

                    // Alternativa (si el orden fuera XYZ intrínseco):
                    // return new Quaternionf().rotationXYZ(pitchRad, yawRad, rollRad);

                } catch (NumberFormatException | IllegalStateException e) {
                    throw new JsonParseException("El array JSON contiene elementos no numéricos o inválidos.", e);
                }
            } else {
                throw new JsonParseException(
                        "El array JSON de rotación Blockbench debe contener exactamente 3 elementos [pitch, yaw, roll], pero tiene "
                                + array.size() + ".");
            }
        } else {
            throw new JsonParseException("La rotación de Blockbench debe ser un JsonArray, pero se recibió: "
                    + element.getClass().getSimpleName());
        }
    }

    public static UUID getAsUUID(JsonElement jsonElement) {
        return UUID.fromString(jsonElement.getAsString());
    }
}