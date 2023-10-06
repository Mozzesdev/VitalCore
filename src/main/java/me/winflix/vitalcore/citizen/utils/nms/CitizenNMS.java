package me.winflix.vitalcore.citizen.utils.nms;

import com.mojang.datafixers.util.Pair;
import me.winflix.vitalcore.citizen.models.NPC;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CitizenNMS  {

    public CitizenNMS() {
    }

    public static ClientboundMoveEntityPacket.Rot getEntityLookPacket(NPC npc) {
        return new ClientboundMoveEntityPacket.Rot(npc.getEntityID(),
                (byte) ((int) (npc.getLocation().getYaw() * 256.0F / 360.0F)),
                (byte) ((int) (npc.getLocation().getPitch() * 256.0F / 360.0F)), true);
    }

    public static ClientboundMoveEntityPacket.Pos getEntityMovePacket(double x, double y, double z,
                                                                      boolean onGround, NPC npc) {
        return new ClientboundMoveEntityPacket.Pos(npc.getEntityID(), (short) (x * 4096), (short) (y * 4096),
                (short) (z * 4096), onGround);
    }

    public static ClientboundSetEquipmentPacket getEntityEquipmentPacket(EquipmentSlot slot, ItemStack itemStack, NPC npc) {
        return new ClientboundSetEquipmentPacket(npc.getEntityID(),
                List.of(new Pair<>(slot, CraftItemStack.asNMSCopy(itemStack))));
    }


    public static ClientboundRemoveEntitiesPacket getEntityDestroyPacket(NPC npc) {
        return new ClientboundRemoveEntitiesPacket(npc.getEntityID());
    }

}
