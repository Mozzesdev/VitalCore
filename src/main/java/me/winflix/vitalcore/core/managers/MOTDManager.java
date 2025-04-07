package me.winflix.vitalcore.core.managers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.winflix.vitalcore.VitalCore;
import me.winflix.vitalcore.general.utils.Utils;

public class MOTDManager {

    private List<String> motdList;
    private Map<String, String> iconMappings; // Mapea patrones de MOTD a nombres de iconos.
    private boolean downsampleColors;
    private int fakePlayerCount;
    private int additionalMaxPlayers;
    private final File iconsFolder;
    private final Random random;

    public MOTDManager() {
        this.random = new Random();
        loadConfig();
        this.iconsFolder = new File(VitalCore.getPlugin().getDataFolder(), "motd/icons");
        if (!iconsFolder.exists()) {
            iconsFolder.mkdirs();
        }
    }

    private void loadConfig() {
        FileConfiguration config = VitalCore.fileManager.getMotdFile().getConfig();

        this.motdList = config.getStringList("motds");
        if (this.motdList == null || this.motdList.isEmpty()) {
            this.motdList = Collections.singletonList("&aBienvenido al servidor!");
        }

        this.downsampleColors = config.getBoolean("downsample-colors", false);
        this.fakePlayerCount = config.getInt("fake-player-count", -1);
        this.additionalMaxPlayers = config.getInt("additional-max-players", 0);

        this.iconMappings = new HashMap<>();
        ConfigurationSection iconSection = config.getConfigurationSection("icon-mappings");
        if (iconSection != null) {
            for (String key : iconSection.getKeys(false)) {
                String iconFileName = iconSection.getString(key);
                iconMappings.put(key, iconFileName);
            }
        }
    }

    /**
     * Obtiene un MOTD procesado aplicando colores RGB, gradientes y downsample si
     * está activado.
     */
    public String getProcessedMotd() {
        String rawMotd = getRandomMotd();
        String processedMotd = applyColorsAndGradients(rawMotd);
        if (downsampleColors) {
            processedMotd = downsampleRGB(processedMotd);
        }
        return processedMotd;
    }

    /**
     * Selecciona aleatoriamente un MOTD de la lista.
     */
    public String getRandomMotd() {
        int index = random.nextInt(motdList.size());
        return motdList.get(index);
    }

    /**
     * Aplica la traducción de códigos de colores (& -> §) y procesa códigos
     * hexadecimales.
     * Esta implementación es básica y asume que los códigos hex ya son compatibles.
     * Para gradientes se requiere una implementación adicional.
     */
    private String applyColorsAndGradients(String motd) {
        return Utils.useColors(motd);
    }

    /**
     * Downsample RGB: método simplificado que elimina los códigos hexadecimales
     * para clientes antiguos.
     * Una implementación real mapearía los colores hex a los más cercanos de la
     * paleta legacy.
     */
    private String downsampleRGB(String motd) {
        // Remueve códigos hex (por ejemplo, "#FFAA00")
        return motd.replaceAll("#[a-fA-F0-9]{6}", "");
    }

    /**
     * Devuelve la cantidad de jugadores que se mostrará, utilizando fakePlayerCount
     * si está habilitado.
     * 
     * @param actualOnline la cantidad real de jugadores en línea.
     */
    public int getDisplayedPlayerCount(int actualOnline) {
        if (fakePlayerCount >= 0) {
            return fakePlayerCount;
        }
        return actualOnline;
    }

    /**
     * Devuelve la cantidad máxima de jugadores a mostrar, sumando
     * additionalMaxPlayers al valor real.
     * 
     * @param actualMax la cantidad máxima real de jugadores.
     */
    public int getDisplayedMaxPlayers(int actualMax) {
        return actualMax + additionalMaxPlayers;
    }

    /**
     * Retorna un icono de servidor aleatorio desde la carpeta "icons".
     */
    public File getRandomServerIcon() {
        File[] pngFiles = iconsFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".png");
            }
        });
        if (pngFiles != null && pngFiles.length > 0) {
            int index = random.nextInt(pngFiles.length);
            return pngFiles[index];
        }
        return null;
    }

    /**
     * Retorna el icono asociado a un MOTD en particular, si existe en la
     * configuración.
     * Si no hay un icono emparejado, retorna null.
     * 
     * @param motd el MOTD a evaluar.
     */
    public File getIconForMotd(String motd) {
        for (Map.Entry<String, String> entry : iconMappings.entrySet()) {
            String motdPattern = entry.getKey();
            // Si el MOTD contiene el patrón, se asocia el icono.
            if (motd.contains(motdPattern)) {
                File iconFile = new File(iconsFolder, entry.getValue());
                if (iconFile.exists() && iconFile.isFile()) {
                    return iconFile;
                }
            }
        }
        
        File icon = getRandomServerIcon();

        if (icon != null && icon.exists()) {
            return icon;
        }

        return null;
    }
}