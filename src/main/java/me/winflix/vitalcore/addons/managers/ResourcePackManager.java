package me.winflix.vitalcore.addons.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// Assuming these interfaces and classes exist and are structured appropriately
import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.addons.interfaces.BbModel;
import me.winflix.vitalcore.addons.interfaces.ProcessedPackData;

public class ResourcePackManager {

    private final ModelEngineManager modelEngine;
    private final File dataFolder; // Plugin's data folder
    public static Material BASE_ITEM_MATERIAL = Material.STICK;
    public static final String VITALCORE_NAMESPACE = "vitalcore"; // Your resource pack namespace
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private String resourcePackUrl = "http://interface-situated.gl.at.ply.gg:14890/resourcepack.zip"; // Configure this URL
    private byte[] currentPackSha1; // Calculated after generation

    public ResourcePackManager(ModelEngineManager engine) throws IOException {
        this.modelEngine = engine;
        this.dataFolder = VitalCore.getPlugin().getDataFolder();
        if (this.dataFolder == null) {
            throw new IllegalStateException("Could not get plugin data folder!");
        }

        getPackBuildDirectory().mkdirs();
    }

    /**
     * Gets the directory where the resource pack is built (unzipped).
     * Example: /plugins/VitalCore/resourcepack_output/
     */
    public File getPackBuildDirectory() {
        return new File(dataFolder, "resourcepack_output");
    }

    /**
     * Gets the final zipped resource pack file.
     * Example: /plugins/VitalCore/resourcepack.zip
     */
    public File getZippedResourcePackFile() {
        return new File(dataFolder, "resourcepack.zip");
    }

    /**
     * Main method to generate (and optionally zip) the resource pack.
     * Conforms to Minecraft 1.21.4+ standards (pack_format 46).
     *
     * @param zipFile If true, compresses the generated folder into a.zip file.
     * @return true if generation (and optionally zipping) was successful, false
     *         otherwise.
     */
    public boolean generateResourcePack(boolean zipFile) {
        VitalCore.Log.info(" Starting resource pack generation (Format 46)...");
        File outputDir = getPackBuildDirectory();

        try {
            if (outputDir.exists()) {
                deleteDirectory(outputDir);
            }
            outputDir.mkdirs();
        } catch (IOException e) {
            VitalCore.Log.log(Level.SEVERE, " Failed to clean output directory: " + outputDir.getPath(), e);
            return false;
        }

        Collection<ProcessedPackData> allPackData = modelEngine.getAllProcessedPackData();

        if (allPackData.isEmpty()) {
            VitalCore.Log.warning(" No models loaded to generate the pack.");
            writePackMeta(outputDir);
            if (zipFile) {
                boolean zipSuccess = zipResourcePack(outputDir, getZippedResourcePackFile());
                if (zipSuccess)
                    calculateCurrentHash();
                return zipSuccess;
            }
            return true;
        }

        Map<String, JsonObject> allGeometryJsons = new HashMap<>();
        Map<String, JsonObject> allItemDefinitionsJsons = new HashMap<>();

        VitalCore.Log.info(" Processing " + allPackData.size() + " models...");
        for (ProcessedPackData packData : allPackData) {
            if (packData != null) {
                allGeometryJsons.putAll(packData.getGeometryJsons());
                allItemDefinitionsJsons.putAll(packData.getItemDefinitionJsons());
            }
        }

        VitalCore.Log.info(" Models processed. Writing files...");

        File assetsRoot = new File(outputDir, "assets");
        boolean success = true;

        success &= writeTextures(allPackData, assetsRoot);
        success &= writeJsonFiles(allGeometryJsons, assetsRoot);
        success &= writeJsonFiles(allItemDefinitionsJsons, assetsRoot);
        success &= writePackMeta(outputDir);
        // success &= copyPackIcon(outputDir); // Si tienes un pack.png

        try {
            JsonObject atlasJson = createAtlasJson(allPackData);
            File atlasDir = new File(assetsRoot, "minecraft" + File.separator + "atlases");
            atlasDir.mkdirs();
            File atlasFile = new File(atlasDir, "blocks.json");
            try (FileWriter writer = new FileWriter(atlasFile, StandardCharsets.UTF_8)) {
                GSON.toJson(atlasJson, writer);
                VitalCore.Log.info(" Atlas file generated: " + atlasFile.getPath());
            }
        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, " Failed to write atlas file (blocks.json)", e);
            success = false;
        }

        if (!success) {
            VitalCore.Log.severe(" Errors occurred during file writing. Pack may be incomplete.");
        } else {
            VitalCore.Log.info(" Files generated successfully.");
        }

        if (zipFile) {
            VitalCore.Log.info(" Compressing resource pack...");
            success &= zipResourcePack(outputDir, getZippedResourcePackFile());
            if (success) {
                VitalCore.Log.info(" Pack compressed successfully: " + getZippedResourcePackFile().getName());
                calculateCurrentHash();
            } else {
                VitalCore.Log.severe(" Failed to compress the resource pack.");
            }
        }

        return success;
    }

    /**
     * Writes texture files (.png) to the correct assets directory.
     * Corrected path to assets/namespace/textures/item/
     */
    private boolean writeTextures(Collection<ProcessedPackData> allPackData, File assetsRoot) {
        VitalCore.Log.info(" Writing textures (organized by model)...");
        boolean success = true;
        Set<String> writtenTexturePaths = new HashSet<>(); // Para evitar duplicados

        for (ProcessedPackData packData : allPackData) {
            if (packData == null)
                continue;

            String modelName = packData.getModelName().toLowerCase().replaceAll("[^a-z0-9/._-]", "_");

            File textureModelDir = new File(assetsRoot,
                    VITALCORE_NAMESPACE + File.separator + "textures" + File.separator + modelName);

            for (BbModel.ModelTexture texture : packData.getTexturesUsed()) {
                try {
                    String textureFileName = texture.getName().toLowerCase();
                    String relativeTexturePath = VITALCORE_NAMESPACE + "/textures/" + modelName + "/" + textureFileName;
                    if (!textureFileName.endsWith(".png")) {
                        continue;
                    }
                    if (!writtenTexturePaths.add(relativeTexturePath))
                        continue;

                    File textureFile = new File(textureModelDir, textureFileName);
                    textureFile.getParentFile().mkdirs();

                    byte[] data = texture.getData();
                    if (data != null && data.length > 0) {
                        Files.write(textureFile.toPath(), data);
                    }

                } catch (Exception e) {
                    success = false;
                }
            }
        }
        VitalCore.Log.info(" Written " + writtenTexturePaths.size() + " unique texture files.");
        return success;
    }

    /**
     * Writes multiple JSON files (e.g., geometry models, item definitions)
     * based on a map where the key is the namespaced ID ("namespace:path/to/file").
     */
    private boolean writeJsonFiles(Map<String, JsonObject> jsonMap, File assetsRoot) {
        if (jsonMap.isEmpty()) {
            VitalCore.Log.info(" No JSON files of this type to write.");
            return true; // Nothing to do is considered success
        }
        VitalCore.Log.info(" Writing " + jsonMap.size() + " JSON files...");
        boolean success = true;
        for (Map.Entry<String, JsonObject> entry : jsonMap.entrySet()) {
            String namespacedId = entry.getKey(); // e.g., "vitalcore:models/item/sword" or "vitalcore:items/sword"
            JsonObject jsonObject = entry.getValue();

            // Convert namespaced ID to a relative file path from 'assets'
            String[] parts = namespacedId.split(":", 2);
            if (parts.length != 2) {
                VitalCore.Log.warning(" Invalid namespaced ID format, skipping: " + namespacedId);
                success = false;
                continue;
            }
            String namespace = parts[0];
            String pathWithoutExtension = parts[1]; // e.g., "models/item/sword" or "items/sword"

            // Construct the final file path
            File jsonFile = new File(assetsRoot, namespace + File.separator + pathWithoutExtension + ".json");

            try {
                jsonFile.getParentFile().mkdirs(); // Ensure parent directories exist
                try (FileWriter writer = new FileWriter(jsonFile, StandardCharsets.UTF_8)) {
                    GSON.toJson(jsonObject, writer); // Write formatted JSON
                }
            } catch (Exception e) {
                VitalCore.Log.log(Level.SEVERE, " Error writing JSON file: " + jsonFile.getPath(), e);
                success = false;
            }
        }
        return success;
    }

    /**
     * Writes the pack.mcmeta file with the correct pack_format for 1.21.4+.
     */
    private boolean writePackMeta(File outputDirectory) {
        VitalCore.Log.info(" Writing pack.mcmeta...");
        File metaFile = new File(outputDirectory, "pack.mcmeta");
        JsonObject packJson = new JsonObject();
        JsonObject packSection = new JsonObject();

        // --- CRITICAL FIX: Set correct pack_format for 1.21.4 ---
        packSection.addProperty("pack_format", 46); // Format for 1.21.4
        // ---------------------------------------------------------

        packSection.addProperty("description", "VitalCore Addons Resource Pack"); // Make configurable if needed
        // Add other sections like "supported_formats" if necessary
        packJson.add("pack", packSection);

        try (FileWriter writer = new FileWriter(metaFile, StandardCharsets.UTF_8)) {
            GSON.toJson(packJson, writer);
            return true;
        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, " Error writing pack.mcmeta", e);
            return false;
        }
    }

    /**
     * Genera el contenido JSON para el archivo de atlas (blocks.json).
     * 
     * @param allPackData Colección de datos procesados para obtener nombres de
     *                    modelo.
     * @return JsonObject representando blocks.json
     */
    private JsonObject createAtlasJson(Collection<ProcessedPackData> allPackData) {
        JsonObject atlasJson = new JsonObject();
        JsonArray sources = new JsonArray();

        // Añadir fuentes por defecto que Minecraft espera (Opcional pero recomendado)
        JsonObject defaultBlockSource = new JsonObject();
        defaultBlockSource.addProperty("type", "minecraft:directory");
        defaultBlockSource.addProperty("source", "block"); // Incluye texturas de /assets/minecraft/textures/block/
        defaultBlockSource.addProperty("prefix", "block/");
        sources.add(defaultBlockSource);

        JsonObject defaultItemSource = new JsonObject();
        defaultItemSource.addProperty("type", "minecraft:directory");
        defaultItemSource.addProperty("source", "item"); // Incluye texturas de /assets/minecraft/textures/item/
        defaultItemSource.addProperty("prefix", "item/");
        sources.add(defaultItemSource);

        Set<String> modelTextureFolders = new HashSet<>();
        for (ProcessedPackData packData : allPackData) {
            if (packData == null || packData.getTexturesUsed() == null || packData.getTexturesUsed().isEmpty())
                continue;
            String modelName = packData.getModelName().toLowerCase().replaceAll("[^a-z0-9/._-]", "_");
            modelTextureFolders.add(modelName); // Añadir nombre de carpeta único
        }

        for (String modelFolderName : modelTextureFolders) {
            JsonObject modelDirSource = new JsonObject();
            modelDirSource.addProperty("type", "minecraft:directory");
            modelDirSource.addProperty("source", modelFolderName);
            modelDirSource.addProperty("prefix", modelFolderName + "/");
            sources.add(modelDirSource);
            VitalCore.Log.info("[Atlas] Añadiendo fuente de directorio al atlas: " + modelFolderName);
        }

        atlasJson.add("sources", sources);
        return atlasJson;
    }

    /** Compresses the contents of a source directory into a target ZIP file. */
    private boolean zipResourcePack(File sourceDir, File outputFile) {
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            VitalCore.Log.severe(
                    " Source directory for zipping does not exist or is not a directory: " + sourceDir.getPath());
            return false;
        }
        try {
            if (outputFile.exists()) {
                Files.delete(outputFile.toPath()); // Delete existing zip file safely
            }
            Path zipPath = outputFile.toPath();
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                Path sourcePath = sourceDir.toPath();
                Files.walk(sourcePath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            // Create relative path for ZIP entry
                            String entryName = sourcePath.relativize(path).toString().replace("\\", "/");
                            ZipEntry zipEntry = new ZipEntry(entryName);
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                // Use a more robust way to handle stream errors
                                throw new RuntimeException("Error adding file to ZIP: " + path, e);
                            }
                        });
            } // ZipOutputStream is closed automatically here
            return true; // Success if no RuntimeException was thrown
        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, " Error creating ZIP file: " + outputFile.getPath(), e);
            // Attempt to delete partially created zip file on error
            try {
                Files.deleteIfExists(outputFile.toPath());
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    /** Recursively deletes a directory and its contents. */
    private void deleteDirectory(File directory) throws IOException {
        if (!directory.exists())
            return;
        Files.walk(directory.toPath())
                .sorted(Comparator.reverseOrder()) // Delete contents before directories
                .map(Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        // Log or throw if deletion fails, might indicate file lock
                        VitalCore.Log.warning(" Could not delete file/directory: " + file.getPath());
                        // throw new IOException("Failed to delete: " + file); // Option to make it
                        // stricter
                    }
                });
    }

    /**
     * Sends the resource pack prompt to a player.
     * Requires the URL and SHA-1 hash to be set/calculated.
     *
     * @param player The player to send the pack to.
     */
    public void sendResourcePackToPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        if (resourcePackUrl == null || resourcePackUrl.isEmpty()) {
            VitalCore.Log.warning(" Resource pack URL is not configured. Cannot send pack to " + player.getName());
            // Consider sending a message to the player or admin
            return;
        }

        if (currentPackSha1 == null || currentPackSha1.length != 20) { // SHA-1 hash is 20 bytes
            VitalCore.Log.warning(" SHA-1 hash is not calculated or invalid. Cannot send pack to " + player.getName());
            return;
        }

        VitalCore.Log.info(" Sending pack to " + player.getName() + " (URL: " + resourcePackUrl + ")");

        try {
            byte[] sha1Bytes = new byte[currentPackSha1.length];
            for (int i = 0; i < currentPackSha1.length; i++) {
                sha1Bytes[i] = currentPackSha1[i];
            }
            player.setResourcePack(resourcePackUrl, sha1Bytes);

        } catch (Exception e) {
            VitalCore.Log.log(Level.SEVERE, " Error calling player.setResourcePack for " + player.getName(), e);
        }
    }

    /** Calculates the SHA-1 hash of the given file. */
    private byte[] calculateSHA1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = new FileInputStream(file);
                DigestInputStream dis = new DigestInputStream(fis, sha1)) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            while (dis.read(buffer) != -1) {
                // Reading updates the digest automatically
            }
        } // Streams are closed automatically
        return sha1.digest(); // Returns the 20-byte SHA-1 hash
    }

    /**
     * Updates the stored SHA-1 hash based on the current zipped resource pack file.
     */
    private void calculateCurrentHash() {
        File zipFile = getZippedResourcePackFile();
        if (zipFile.exists() && zipFile.isFile()) {
            try {
                this.currentPackSha1 = calculateSHA1(zipFile);
                VitalCore.Log.info(" Calculated new SHA-1 hash for the resource pack.");
            } catch (IOException | NoSuchAlgorithmException e) {
                VitalCore.Log.log(Level.SEVERE, " Failed to calculate SHA-1 hash for " + zipFile.getName(), e);
            }
        } else {
            VitalCore.Log.warning(" Zipped resource pack file not found, cannot calculate hash: " + zipFile.getPath());
        }
    }

    // --- Getters for URL and Hash (if needed externally) ---

    public String getResourcePackUrl() {
        return resourcePackUrl;
    }

    public void setResourcePackUrl(String resourcePackUrl) {
        this.resourcePackUrl = resourcePackUrl;
    }

    public byte[] getCurrentPackSha1() {
        return currentPackSha1; // Returns the raw byte array
    }

    // Example method to trigger regeneration and hash update
    public boolean regenerateAndZip() {
        boolean success = generateResourcePack(true);
        // calculateCurrentHash() is called within generateResourcePack if zipping
        // succeeds
        return success;
    }

}