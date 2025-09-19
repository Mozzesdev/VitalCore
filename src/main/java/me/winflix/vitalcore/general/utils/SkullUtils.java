package me.winflix.vitalcore.general.utils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.world.item.component.ResolvableProfile;

public class SkullUtils {

    private static final String DUMMY_PROFILE_NAME = "CustomSkull";

    /**
     * Crea un cráneo personalizado con textura base64 y metadatos.
     *
     * @param base64Texture Textura en formato base64
     * @param displayName   Nombre del ítem (con colores soportados)
     * @param lore          Lista de descripciones (con colores soportados)
     * @return ItemStack con la cabeza personalizada
     */
    public static ItemStack createSkull(String base64Texture, String displayName, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        applyDisplayName(meta, displayName);
        applyLore(meta, lore);
        applyTexture(meta, base64Texture);

        skull.setItemMeta(meta);
        return skull;
    }

    private static void applyDisplayName(SkullMeta meta, String displayName) {
        if (displayName != null && !displayName.isEmpty()) {
            meta.setDisplayName(Utils.useColors(displayName));
        }
    }

    private static void applyLore(SkullMeta meta, List<String> lore) {
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore.stream()
                    .map(Utils::useColors)
                    .toList());
        }
    }

    private static void applyTexture(SkullMeta meta, String base64Texture) {
        try {
            GameProfile gameProfile = createGameProfile(base64Texture);
            ResolvableProfile resolvableProfile = new ResolvableProfile(gameProfile);
            setProfileField(meta, resolvableProfile);
        } catch (Exception e) {
            throw new RuntimeException("Error applying skull texture", e);
        }
    }

    private static GameProfile createGameProfile(String base64Texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), DUMMY_PROFILE_NAME);
        profile.getProperties().put("textures", new Property("textures", base64Texture));
        return profile;
    }

    private static void setProfileField(SkullMeta meta, Object profile) throws Exception {
        Field profileField = meta.getClass().getDeclaredField("profile");
        profileField.setAccessible(true);
        profileField.set(meta, profile);
    }
}
