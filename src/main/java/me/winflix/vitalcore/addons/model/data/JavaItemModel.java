// Path: me/winflix/vitalcore/addons/model/data/JavaItemModel.java
package me.winflix.vitalcore.addons.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material; // Importar Material
import org.bukkit.inventory.ItemStack; // Importar ItemStack
import org.bukkit.inventory.meta.ItemMeta; // Importar ItemMeta

import me.winflix.vitalcore.addons.utils.MathUtils;

public class JavaItemModel {
    private static final float DIST_DIVIDER = 0.041666668f;
    private final Map<String, String> textures;
    private final List<JavaElement> elements;
    private transient String name; // Nombre del hueso/parte, ej. "head", "body_part_1"
    private transient float maxDistToOrigin;
    private Map<String, Map<String, float[]>> display;
    private transient int customModelData = 0; // Campo para almacenar el CMD

    public JavaItemModel() {
        this.textures = new HashMap<String, String>();
        this.elements = new ArrayList<JavaElement>();
        this.maxDistToOrigin = 0.0f;
        this.display = new HashMap<String, Map<String, float[]>>();
        // El constructor original llamaba a this.setDisplay, lo cual está bien.
        // this.setDisplay(JavaDisplay.GUI, JavaDisplay.Transform.ROTATION, 30.0f, 225.0f, 0.0f);
        // Si JavaDisplay y JavaDisplay.Transform no están disponibles, esta línea daría error.
        // Comentado temporalmente si esas clases no están en el contexto actual.
    }

    /**
     * Crea un ItemStack para este modelo de item.
     * Utiliza Material.PAPER por defecto y asigna el CustomModelData.
     * @param amount La cantidad de items en el stack.
     * @return El ItemStack creado.
     */
    public ItemStack createItemStack(int amount) {
        // Usar un material base, comúnmente paper o un item que no tenga muchas variantes visuales.
        ItemStack itemStack = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (this.customModelData != 0) {
                 meta.setCustomModelData(this.customModelData);
            } else if (this.name != null && !this.name.isEmpty()){
                meta.setCustomModelData(Math.abs(this.name.hashCode() % 100000));
            }
            // Podrías querer añadir el nombre del modelo/hueso al display name del item para debugging
            // meta.setDisplayName(this.name != null ? this.name : "Modelo de Item");
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
    
    // Método para establecer el CustomModelData desde ModelProcessor
    public void setCustomModelData(int cmd) {
        this.customModelData = cmd;
    }

    public int getCustomModelData() {
        return this.customModelData;
    }


    public void setDisplay(final JavaDisplay display, final JavaDisplay.Transform transform, final float x,
            final float y, final float z) {
        // Asumiendo que JavaDisplay y JavaDisplay.Transform existen y funcionan como antes.
        // Si no, esta parte necesitaría ser ajustada o eliminada si no es relevante para el nuevo flujo.
        if (display == null || transform == null) return; // Chequeo de nulos
        final float[] array = this.display.computeIfAbsent(display.toString(), s -> new HashMap<>())
                .computeIfAbsent(transform.toString(), s -> new float[3]);
        array[0] = x;
        array[1] = y;
        array[2] = z;
        // Asumiendo que transform.sanitize(array) existe.
        // transform.sanitize(array); 
    }

    public void addElement(final JavaElement element) {
        this.elements.add(element);
        for (int i = 0; i < 3; ++i) {
            this.maxDistToOrigin = Math.max(Math.max(Math.abs(element.from[i] - 8.0f), Math.abs(element.to[i] - 8.0f)),
                    this.maxDistToOrigin);
        }
    }

    public int scaleToFit() {
        if (this.maxDistToOrigin <= 24.0f) {
            return 1;
        }
        final int size = (int) Math.ceil(this.maxDistToOrigin * DIST_DIVIDER); // Usar la constante
        final float scaleFactor = 1.0f / size; // Renombrado a scaleFactor para claridad
        for (final JavaElement element : this.elements) {
            final float[] origin = (element.getRotation() == null) ? null : element.getRotation().origin;
            for (int i = 0; i < 3; ++i) {
                element.from[i] = MathUtils.clamp((element.from[i] - 8.0f) * scaleFactor + 8.0f, -16.0f, 32.0f);
                element.to[i] = MathUtils.clamp((element.to[i] - 8.0f) * scaleFactor + 8.0f, -16.0f, 32.0f);
                if (origin != null) {
                    origin[i] = (origin[i] - 8.0f) * scaleFactor + 8.0f;
                }
            }
        }
        return size;
    }

    public void finalizeModel() {
        // Lógica de finalización si es necesaria
    }

    public Map<String, String> getTextures() {
        return this.textures;
    }

    public List<JavaElement> getElements() {
        return this.elements;
    }

    public String getName() {
        return this.name;
    }

    public float getMaxDistToOrigin() {
        return this.maxDistToOrigin;
    }

    public Map<String, Map<String, float[]>> getDisplay() {
        return this.display;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setMaxDistToOrigin(final float maxDistToOrigin) {
        this.maxDistToOrigin = maxDistToOrigin;
    }

    public void setDisplay(final Map<String, Map<String, float[]>> display) {
        this.display = display;
    }

    public static class JavaElement {
        private final float[] from;
        private final float[] to;
        private final Map<String, Face> faces;
        private Rotation rotation;

        public JavaElement() {
            this.from = new float[3];
            this.to = new float[3];
            this.faces = new HashMap<String, Face>();
        }

        public void from(final float[] origin, final float[] globalFrom, final float inflate) {
            this.from[0] = globalFrom[0] - origin[0] + 8.0f - inflate;
            this.from[1] = globalFrom[1] - origin[1] + 8.0f - inflate;
            this.from[2] = globalFrom[2] - origin[2] + 8.0f - inflate;
        }

        public void from(final float[] origin, final float inflate) {
            this.from[0] = origin[0] + 8.0f - inflate;
            this.from[1] = origin[1] + 8.0f - inflate;
            this.from[2] = origin[2] + 8.0f - inflate;
        }

        public void to(final float[] origin, final float[] globalTo, final float inflate) {
            this.to[0] = globalTo[0] - origin[0] + 8.0f + inflate;
            this.to[1] = globalTo[1] - origin[1] + 8.0f + inflate;
            this.to[2] = globalTo[2] - origin[2] + 8.0f + inflate;
        }

        public void to(final float[] origin, final float inflate) {
            this.to[0] = origin[0] + 8.0f + inflate;
            this.to[1] = origin[1] + 8.0f + inflate;
            this.to[2] = origin[2] + 8.0f + inflate;
        }

        public float[] getFrom() {
            return this.from;
        }

        public float[] getTo() {
            return this.to;
        }

        public Map<String, Face> getFaces() {
            return this.faces;
        }

        public Rotation getRotation() {
            return this.rotation;
        }

        public void setRotation(final Rotation rotation) {
            this.rotation = rotation;
        }

        public static class Rotation {
            private final float[] origin;
            private float angle;
            private String axis;

            public Rotation() {
                this.origin = new float[] { 8.0f, 8.0f, 8.0f };
                this.axis = "x";
            }

            public void origin(final float[] origin, final float[] globalOrigin) {
                this.origin[0] = globalOrigin[0] - origin[0] + 8.0f;
                this.origin[1] = globalOrigin[1] - origin[1] + 8.0f;
                this.origin[2] = globalOrigin[2] - origin[2] + 8.0f;
            }

            public void origin(final float[] origin) {
                this.origin[0] = origin[0] + 8.0f;
                this.origin[1] = origin[1] + 8.0f;
                this.origin[2] = origin[2] + 8.0f;
            }

            public float[] getOrigin() {
                return this.origin;
            }

            public float getAngle() {
                return this.angle;
            }

            public String getAxis() {
                return this.axis;
            }

            public void setAngle(final float angle) {
                this.angle = angle;
            }

            public void setAxis(final String axis) {
                this.axis = axis;
            }
        }

        public static class Face {
            private final float[] uv;
            private final int tintindex = 0; // Campo final, no se puede reasignar.
            private int rotation;
            private String texture;

            public Face() {
                this.uv = new float[4];
                this.texture = "";
            }

            public void uv(final int width, final int height, final float[] uv) {
                final float factorU = 16.0f / width;
                final float factorV = 16.0f / height;
                this.uv[0] = uv[0] * factorU;
                this.uv[1] = uv[1] * factorV;
                this.uv[2] = uv[2] * factorU;
                this.uv[3] = uv[3] * factorV;
            }

            public float[] getUv() {
                return this.uv;
            }

            public int getTintindex() {
                // Objects.requireNonNull(this); // No es necesario aquí
                return 0; // Devuelve el valor final
            }

            public int getRotation() {
                return this.rotation;
            }

            public String getTexture() {
                return this.texture;
            }

            public void setRotation(final int rotation) {
                this.rotation = rotation;
            }

            public void setTexture(final String texture) {
                this.texture = texture;
            }
        }
    }
}
