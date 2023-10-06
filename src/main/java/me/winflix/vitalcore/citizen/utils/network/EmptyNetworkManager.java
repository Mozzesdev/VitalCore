package me.winflix.vitalcore.citizen.utils;

import me.winflix.vitalcore.core.nms.NMS;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

import java.io.IOException;

public class EmptyNetworkManager extends Connection {
    public EmptyNetworkManager(PacketFlow flag) throws IOException {
        super(flag);
        NMS.initNetworkManager(this);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(Packet packet, PacketSendListener genericfuturelistener) {
    }
}