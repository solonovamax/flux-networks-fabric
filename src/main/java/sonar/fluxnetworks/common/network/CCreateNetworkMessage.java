package sonar.fluxnetworks.common.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import sonar.fluxnetworks.api.gui.EnumFeedbackInfo;
import sonar.fluxnetworks.api.misc.IMessage;
import sonar.fluxnetworks.api.network.NetworkSecurity;
import sonar.fluxnetworks.common.handler.NetworkHandler;
import sonar.fluxnetworks.common.misc.FluxUtils;
import sonar.fluxnetworks.common.storage.FluxNetworkData;

import javax.annotation.Nonnull;

public class CCreateNetworkMessage implements IMessage {

    protected String name;
    protected int color;
    protected NetworkSecurity.Type security;
    protected String password;

    public CCreateNetworkMessage() {
    }

    public CCreateNetworkMessage(String name, int color, NetworkSecurity.Type security, String password) {
        this.name = name;
        this.color = color;
        this.security = security;
        this.password = password;
    }

    @Override
    public void encode(@Nonnull PacketBuffer buffer) {
        buffer.writeString(name, 256);
        buffer.writeInt(color);
        buffer.writeVarInt(security.ordinal());
        buffer.writeString(password, 256);
    }

    @Override
    public final void handle(@Nonnull PacketBuffer buffer, @Nonnull NetworkEvent.Context context) {
        PlayerEntity player = NetworkHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        name = buffer.readString(256);
        color = buffer.readInt();
        security = NetworkSecurity.Type.values()[buffer.readVarInt()];
        password = buffer.readString(256);
        if (FluxUtils.checkPassword(password)) {
            handle(buffer, context, player);
        } else {
            NetworkHandler.INSTANCE.reply(new SFeedbackMessage(EnumFeedbackInfo.ILLEGAL_PASSWORD), context);
        }
    }

    protected void handle(@Nonnull PacketBuffer buffer, @Nonnull NetworkEvent.Context context, PlayerEntity player) {
        if (FluxNetworkData.get().createNetwork(player, name, color, security, password) != null) {
            NetworkHandler.INSTANCE.reply(new SFeedbackMessage(EnumFeedbackInfo.SUCCESS), context);
        } else {
            NetworkHandler.INSTANCE.reply(new SFeedbackMessage(EnumFeedbackInfo.NO_SPACE), context);
        }
    }
}