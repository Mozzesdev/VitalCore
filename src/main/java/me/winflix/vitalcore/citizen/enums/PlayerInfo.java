package me.winflix.vitalcore.citizen.enums;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

public enum PlayerInfo {

    ADD_PLAYER(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
    INITIALIZE_CHAT(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT),
    UPDATE_GAME_MODE(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE),
    UPDATE_LISTED(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED),
    UPDATE_LATENCY(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY),
    UPDATE_DISPLAY_NAME(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);

    private final ClientboundPlayerInfoUpdatePacket.Action nmsAction;

    PlayerInfo(ClientboundPlayerInfoUpdatePacket.Action nmsAction) {
        this.nmsAction = nmsAction;
    }

    public ClientboundPlayerInfoUpdatePacket.Action getNMSAction() {
        return nmsAction;
    }

    public void serialize(FriendlyByteBuf buf, ClientboundPlayerInfoUpdatePacket.Entry entry) {
        switch (nmsAction) {
            case ADD_PLAYER -> {
                buf.writeUtf(entry.profile().getName(), 16);
                buf.writeGameProfileProperties(entry.profile().getProperties());
            }
            case INITIALIZE_CHAT -> buf.writeNullable(entry.chatSession(), RemoteChatSession.Data::write);
            case UPDATE_GAME_MODE -> buf.writeVarInt(entry.gameMode().getId());
            case UPDATE_LISTED -> buf.writeBoolean(entry.listed());
            case UPDATE_LATENCY -> buf.writeVarInt(entry.latency());
            case UPDATE_DISPLAY_NAME -> buf.writeNullable(entry.displayName(), FriendlyByteBuf::writeComponent);
        }
    }

}