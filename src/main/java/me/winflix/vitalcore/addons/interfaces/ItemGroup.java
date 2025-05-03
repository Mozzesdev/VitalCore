package me.winflix.vitalcore.addons.interfaces;

import java.util.List;
import java.util.Locale;
import java.util.Map;

// Imports JOML actualizados para double
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import me.winflix.vitalcore.addons.interfaces.BbModel.ModelTexture;
import me.winflix.vitalcore.addons.interfaces.JavaItemModel.JavaElement;
import me.winflix.vitalcore.addons.interfaces.JavaItemModel.JavaElement.Face;
import me.winflix.vitalcore.addons.interfaces.ProcessedCube.Direction;
import me.winflix.vitalcore.addons.managers.ResourcePackManager;
import me.winflix.vitalcore.addons.utils.MathUtils;
import me.winflix.vitalcore.addons.utils.MiscUtils;

/**
 * Representa un grupo de cubos (ahora usando double internamente)
 * que compartirán una rotación de display y pertenecerán al mismo archivo JSON.
 */
public class ItemGroup {
        final Quaterniond displayQuaternion;
        final Vector3d displayRotation;
        final List<ProcessedCube> cubes;

        public ItemGroup(Quaterniondc displayQuaternion, Vector3dc displayRotation,
                        List<ProcessedCube> cubes) {
                this.displayQuaternion = new Quaterniond(displayQuaternion);
                this.displayRotation = new Vector3d(displayRotation);
                this.cubes = cubes;
        }

        public List<ProcessedCube> getCubes() {
                return cubes;
        }

        public Quaterniond getDisplayQuaternion() {
                return new Quaterniond(displayQuaternion);
        }

        public Vector3d getDisplayRotation() {
                return new Vector3d(displayRotation);
        }

        public JavaItemModel toJavaItemModel(final String name, final BbModel model) {
                final JavaItemModel javaItem = new JavaItemModel();
                javaItem.setName(name);
                for (ProcessedCube cube : this.cubes) {
                        final JavaElement element = new JavaElement();
                        element.from(MathUtils.unwrap(cube.getFrom()), cube.getInflate());
                        element.to(MathUtils.unwrap(cube.getTo()), cube.getInflate());
                        element.setRotation(cube.rotation());
                        for (Map.Entry<Direction, ProcessedCube.Face> faceEntry : cube.getFaces().entrySet()) {
                                final Direction dir = faceEntry.getKey();
                                final ProcessedCube.Face bbFace = faceEntry.getValue();
                                if (bbFace.isEmpty()) {
                                        continue;
                                }
                                final int id = bbFace.texture();

                                final ModelTexture tex = model.getTextures().get(id);
                                final Face face = new Face();
                                face.setRotation(bbFace.uv().rotation());
                                face.uv(MiscUtils.or(tex.getUvWidth()),
                                                MiscUtils.or(tex.getUvHeight()),
                                                new float[] { bbFace.uv().u1(), bbFace.uv().v1(), bbFace.uv().u2(),
                                                                bbFace.uv().v2() });
                                face.setTexture("#" + id);
                                element.getFaces().put(dir.name().toLowerCase(Locale.ENGLISH), face);
                                final Map<String, String> textures = javaItem.getTextures();

                                textures.computeIfAbsent(String.valueOf(id),
                                                s -> ResourcePackManager.VITALCORE_NAMESPACE + ":" + model.getName()
                                                                + "/" + tex.getName().replace(".png", ""));

                                if (textures.size() != 1) {
                                        continue;
                                }

                                textures.computeIfAbsent("particle", s -> "#" + id);
                        }
                        javaItem.addElement(element);
                }
                javaItem.setDisplay(JavaDisplay.THIRDPERSON_RIGHTHAND, JavaDisplay.Transform.ROTATION,
                                (float) this.displayRotation.x, (float) this.displayRotation.y,
                                (float) this.displayRotation.z);
                javaItem.setDisplay(JavaDisplay.FIRSTPERSON_RIGHTHAND, JavaDisplay.Transform.ROTATION,
                                (float) this.displayRotation.x, (float) this.displayRotation.y,
                                (float) this.displayRotation.z);
                javaItem.setDisplay(JavaDisplay.GROUND, JavaDisplay.Transform.ROTATION, (float) this.displayRotation.x,
                                (float) this.displayRotation.y, (float) this.displayRotation.z);
                javaItem.setDisplay(JavaDisplay.HEAD, JavaDisplay.Transform.ROTATION, (float) this.displayRotation.x,
                                (float) this.displayRotation.y, (float) this.displayRotation.z);
                javaItem.setDisplay(JavaDisplay.FIXED, JavaDisplay.Transform.ROTATION, (float) this.displayRotation.x,
                                (float) this.displayRotation.y, (float) this.displayRotation.z);
                final Vector3d guiRotation = MathUtils.toEulerXYZ(
                                MathUtils.fromEulerXYZ(new Vector3d(30.0, 225.0, 0.0))
                                                .mul((Quaterniondc) this.displayQuaternion));
                javaItem.setDisplay(JavaDisplay.GUI, JavaDisplay.Transform.ROTATION, (float) guiRotation.x,
                                (float) guiRotation.y, (float) guiRotation.z);
                javaItem.setDisplay(JavaDisplay.THIRDPERSON_LEFTHAND, JavaDisplay.Transform.ROTATION,
                                (float) this.displayRotation.x, (float) (-this.displayRotation.y),
                                (float) (-this.displayRotation.z));
                javaItem.setDisplay(JavaDisplay.FIRSTPERSON_LEFTHAND, JavaDisplay.Transform.ROTATION,
                                (float) this.displayRotation.x, (float) (-this.displayRotation.y),
                                (float) (-this.displayRotation.z));
                return javaItem;
        }

        /**
         * Devuelve una representación en String del ItemGroup (double).
         * 
         * @return String representando el objeto ItemGroup.
         */
        @Override
        public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("ItemGroup[");
                sb.append("displayRotation=").append(displayRotation);
                sb.append(", displayQuaternion=").append(displayQuaternion);
                sb.append(", cubes=").append(cubes);
                sb.append(']');
                return sb.toString();
        }
}
