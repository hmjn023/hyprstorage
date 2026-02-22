package net.hmjn.hyperstorage.network.payload

import io.netty.buffer.ByteBuf
import net.hmjn.hyperstorage.Hyperstorage
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class FetchNodeNamesPayload : CustomPacketPayload {
    companion object {
        val ID = CustomPacketPayload.Type<FetchNodeNamesPayload>(ResourceLocation.fromNamespaceAndPath(Hyperstorage.ID, "fetch_node_names"))
        val STREAM_CODEC: StreamCodec<ByteBuf, FetchNodeNamesPayload> =
            StreamCodec.of(
                { _, _ -> },
                { _ -> FetchNodeNamesPayload() },
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
