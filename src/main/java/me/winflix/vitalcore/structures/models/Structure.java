package me.winflix.vitalcore.structures.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.annotations.Expose;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.utils.Utils;
import me.winflix.vitalcore.structures.interfaces.StructureRecipeHolder;
import me.winflix.vitalcore.structures.interfaces.StructuresType;
import me.winflix.vitalcore.structures.region.RegionManager;
import me.winflix.vitalcore.structures.utils.StructureBlockPosition;
import me.winflix.vitalcore.structures.utils.StructureManager;

public class Structure extends StructureRecipeHolder {
    @Expose
    protected String name = "";
    @Expose
    protected StructuresType type = null;
    @Expose
    protected Material[][][] matriz = {};
    @Expose
    protected List<StructureBlockPosition> blockPositions = new ArrayList<>();
    @Expose
    protected String id = "";
    @Expose
    protected float health = 0;
    @Expose
    protected int buildTime = 3;
    @Expose
    protected Facing face;
    @Expose
    protected Location buildLocation;

    public Structure(String name, StructuresType type, float health, Material[][][] matriz, String id) {
        this.name = name;
        this.type = type;
        this.health = health;
        this.matriz = matriz;
        this.id = id;
    }

    public Structure(String name, StructuresType type, float health, Material[][][] matriz, String id,
            StructureItem item) {
        this.name = name;
        this.type = type;
        this.health = health;
        this.matriz = matriz;
        this.id = id;
        this.item = item;
    }

    public Facing getFace() {
        return face;
    }

    public void setFace(Facing face) {
        this.face = face;
    }

    public Material[][][] getMatriz() {
        return matriz;
    }

    public Location getBuildLocation() {
        return buildLocation;
    }

    public void setBuildLocation(Location buildLocation) {
        this.buildLocation = buildLocation;
    }

    public List<StructureBlockPosition> getBlockPositions() {
        return blockPositions;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public StructuresType getType() {
        return type;
    }

    public float getHealth() {
        return health;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public void setMatriz(Material[][][] matriz) {
        this.matriz = matriz;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(StructuresType type) {
        this.type = type;
    }

    public void setBlockPositions(List<StructureBlockPosition> blockPositions) {
        this.blockPositions = blockPositions;
    }

    public int getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(int buildTime) {
        this.buildTime = buildTime;
    }

    protected List<StructureBlockPosition> calculateBlockPositions(Location loc) {
        Material[][][] matrizEstructura = this.getMatriz();
        int offsetX = matrizEstructura[0][0].length / 2;
        int offsetZ = matrizEstructura[0].length / 2;

        List<StructureBlockPosition> blockPositions = new ArrayList<>();
        Location reusableLocation = loc.clone(); // Reutiliza una sola instancia de Location.

        for (int capaY = 0; capaY < matrizEstructura.length; capaY++) {
            for (int fila = 0; fila < matrizEstructura[capaY].length; fila++) {
                for (int columna = 0; columna < matrizEstructura[capaY][fila].length; columna++) {
                    Material material = matrizEstructura[capaY][fila][columna];
                    if (material != null) {
                        // Ajustamos las coordenadas usando los métodos individuales
                        reusableLocation.setX(loc.getX() + columna - offsetX);
                        reusableLocation.setY(loc.getY() + capaY);
                        reusableLocation.setZ(loc.getZ() + fila - offsetZ);

                        double distanceToCenter = Math
                                .sqrt(Math.pow(columna - offsetX, 2) + Math.pow(fila - offsetZ, 2));
                        blockPositions
                                .add(new StructureBlockPosition(reusableLocation.clone(), material, distanceToCenter));
                    }
                }
            }
        }
        return blockPositions;
    }

    public List<Location> getBlockLocations(Location loc) {
        List<Location> locations = new ArrayList<>();
        List<StructureBlockPosition> blockPositions = calculateBlockPositions(loc);

        for (StructureBlockPosition blockPos : blockPositions) {
            locations.add(blockPos.getLocation());
        }

        return locations;
    }

    public boolean build(Location loc, Player player) {
        List<StructureBlockPosition> blockPositions = calculateBlockPositions(loc);
        this.blockPositions = blockPositions;
        this.buildLocation = loc;

        // Verifica si ya hay bloques problemáticos (por ejemplo, cofres o inventarios)
        for (StructureBlockPosition blockPos : blockPositions) {
            Block existingBlock = blockPos.getLocation().getBlock();
            if (existingBlock.getState() instanceof InventoryHolder) {
                player.sendMessage(
                        Utils.useColors("&cNo se puede construir aquí debido a un bloque con inventario."));
                return false;
            }
        }

        int totalBlocks = blockPositions.size();
        long totalTicks = this.buildTime * 20L; // Convertimos los segundos a ticks (1 segundo = 20 ticks)

        // Calculamos el intervalo de ticks entre cada bloque
        long ticksPerBlock = Math.max(1, totalTicks / totalBlocks);

        // Ordenamos las posiciones desde el centro
        blockPositions.sort(Comparator.comparingDouble(StructureBlockPosition::getDistanceToCenter));

        this.face = determineFacing(player);

        // Usamos una tarea asíncrona para construir la estructura gradualmente
        new BukkitRunnable() {
            private int currentBlockIndex = 0;

            @Override
            public void run() {
                if (currentBlockIndex >= totalBlocks) {
                    this.cancel(); // Cancelar la tarea cuando todos los bloques hayan sido colocados
                    executeBuildEffect(loc, player);
                    return;
                }

                // Colocamos el siguiente bloque solo si no existe ya el mismo material en ese
                // lugar
                StructureBlockPosition blockPos = blockPositions.get(currentBlockIndex);
                Block blockToPlace = blockPos.getLocation().getBlock();

                if (blockToPlace.getType() != blockPos.getMaterial()) {
                    blockToPlace.setType(blockPos.getMaterial());
                }
                currentBlockIndex++;
            }
        }.runTaskTimer(VitalCore.getPlugin(), 0L, ticksPerBlock); // Ejecutar cada "ticksPerBlock" ticks

        return true;
    }

    public boolean build(Location loc, Player player, RegionManager regionManager, StructureManager structureManager) {
        return false;
    }

    private void executeBuildEffect(Location loc, Player player) {
        World world = loc.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.FALLING_DUST, loc.clone().add(0, 1, 0), 100, 1, 1, 1,
                    Material.OAK_PLANKS.createBlockData());
            player.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
        }
    }

    protected Facing determineFacing(Player player) {
        float yaw = player.getLocation().getYaw();

        // Normalizar el yaw para que esté entre -180 y 180
        if (yaw < -180) {
            yaw += 360;
        } else if (yaw > 180) {
            yaw -= 360;
        }

        if (yaw >= -45 && yaw < 45) {
            return Facing.SOUTH; // Sur
        } else if (yaw >= 45 && yaw < 135) {
            return Facing.WEST; // Oeste
        } else if (yaw >= 135 || yaw < -135) {
            return Facing.NORTH; // Norte
        } else {
            return Facing.EAST; // Este
        }
    }

    public void destroy() {
        for (StructureBlockPosition loc : blockPositions) {
            Block block = loc.getLocation().getBlock();
            block.setType(Material.AIR);
        }
    }

    public void destroy(Player player, RegionManager regionManager, String regionId) {
    };

    @Override
    public ItemStack getItemStack() {
        if (item != null) {
            return item.toItemStack();
        }
        return null;
    }

    @Override
    public void dropItem() {
        if (blockPositions.isEmpty())
            return;

        double centerX = 0, centerY = 0, centerZ = 0;
        for (StructureBlockPosition blockPosition : blockPositions) {
            Location loc = blockPosition.getLocation();
            centerX += loc.getX();
            centerY += loc.getY();
            centerZ += loc.getZ();
        }

        int totalBlocks = blockPositions.size();
        Location centerLocation = new Location(blockPositions.get(0).getLocation().getWorld(), centerX / totalBlocks,
                centerY / totalBlocks, centerZ / totalBlocks);

        if (this.requireFullDrop()) {
            centerLocation.getWorld().dropItemNaturally(centerLocation, this.getItemStack());
            return;
        }

        // Precalcula el conteo de ingredientes solo una vez
        Map<Material, Integer> ingredientCount = calculateIngredientCount(rawRecipe);

        for (Map.Entry<Material, Integer> entry : ingredientCount.entrySet()) {
            Material material = entry.getKey();
            int amountToDrop = Math.max(1, entry.getValue() / 2);
            centerLocation.getWorld().dropItemNaturally(centerLocation, new ItemStack(material, amountToDrop));
        }
    }

    private Map<Material, Integer> calculateIngredientCount(Recipe rawRecipe) {
        Map<Material, Integer> ingredientCount = new HashMap<>();

        for (String row : rawRecipe.getShape()) {
            for (char key : row.toCharArray()) {
                if (rawRecipe.getIngredients().containsKey(key)) {
                    Material material = rawRecipe.getIngredients().get(key);
                    ingredientCount.put(material, ingredientCount.getOrDefault(material, 0) + 1);
                }
            }
        }
        return ingredientCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Structure {")
                .append("\n  Name: ").append(name)
                .append("\n  Type: ").append(type != null ? type.toString() : "null")
                .append("\n  ID: ").append(id)
                .append("\n  Health: ").append(health)
                .append("\n  Block Positions: ").append(blockPositions)
                .append("\n  Matrix Dimensions: ").append(matriz.length).append(" x ")
                .append(matriz.length > 0 ? matriz[0].length : 0).append(" x ")
                .append(matriz.length > 0 && matriz[0].length > 0 ? matriz[0][0].length : 0)
                .append("\n}");
        return sb.toString();
    }

}
