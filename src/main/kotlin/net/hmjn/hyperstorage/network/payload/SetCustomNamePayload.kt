package net.hmjn.hyperstorage.network.payload

import net.hmjn.hyperstorage.Hyperstorage
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class SetCustomNamePayload(val pos: BlockPos, val name: String) : CustomPacketPayload {
    companion object {
        val ID = CustomPacketPayload.Type<SetCustomNamePayload>(ResourceLocation.fromNamespaceAndPath(Hyperstorage.ID, "set_custom_name"))
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, SetCustomNamePayload> =
            StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                SetCustomNamePayload::pos,
                ByteBufCodecs.STRING_UTF8,
                SetCustomNamePayload::name,
                ::SetCustomNamePayload,
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
