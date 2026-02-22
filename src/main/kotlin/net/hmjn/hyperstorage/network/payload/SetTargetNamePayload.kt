package net.hmjn.hyperstorage.network.payload

import net.hmjn.hyperstorage.Hyperstorage
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class SetTargetNamePayload(val pos: BlockPos, val targetName: String) : CustomPacketPayload {
    companion object {
        val ID = CustomPacketPayload.Type<SetTargetNamePayload>(ResourceLocation.fromNamespaceAndPath(Hyperstorage.ID, "set_target_name"))
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, SetTargetNamePayload> =
            StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                SetTargetNamePayload::pos,
                ByteBufCodecs.STRING_UTF8,
                SetTargetNamePayload::targetName,
                ::SetTargetNamePayload,
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
