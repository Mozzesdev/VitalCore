package me.winflix.vitalcore.residents.utils.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class EmptyNetHandler extends ServerGamePacketListenerImpl {
    public EmptyNetHandler(MinecraftServer minecraftServer, Connection networkManager, ServerPlayer entityPlayer) {
        super(minecraftServer, networkManager, entityPlayer, CommonListenerCookie.createInitial(entityPlayer.getGameProfile(), true));
    }

    @Override
    public void send(Packet<?> packet) {
    }
}