package me.winflix.vitalcore.residents.utils.network;

import java.io.IOException;

import me.winflix.vitalcore.residents.utils.nms.NMS;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public class EmptyNetManager extends Connection {
    public EmptyNetManager(PacketFlow flag) throws IOException {
        super(flag);
        NMS.initNetworkManager(this);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(Packet<?> packet, PacketSendListener genericfuturelistener) {
    }
}
