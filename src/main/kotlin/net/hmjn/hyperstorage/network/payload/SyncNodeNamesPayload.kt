package net.hmjn.hyperstorage.network.payload

import net.hmjn.hyperstorage.Hyperstorage
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class SyncNodeNamesPayload(val names: List<String>) : CustomPacketPayload {
    companion object {
        val ID = CustomPacketPayload.Type<SyncNodeNamesPayload>(ResourceLocation.fromNamespaceAndPath(Hyperstorage.ID, "sync_node_names"))
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, SyncNodeNamesPayload> =
            StreamCodec.of(
                { buf, payload ->
                    buf.writeInt(payload.names.size)
                    for (name in payload.names) {
                        buf.writeUtf(name)
                    }
                },
                { buf ->
                    val size = buf.readInt()
                    val list = mutableListOf<String>()
                    for (i in 0 until size) {
                        list.add(buf.readUtf())
                    }
                    SyncNodeNamesPayload(list)
                },
            )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> {
        return ID
    }
}
