package net.hmjn.hyperstorage.network

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.blockentity.HyperStorageBlockEntity
import net.hmjn.hyperstorage.client.screen.HyperStorageScreen
import net.hmjn.hyperstorage.network.payload.FetchNodeNamesPayload
import net.hmjn.hyperstorage.network.payload.SetCustomNamePayload
import net.hmjn.hyperstorage.network.payload.SetTargetNamePayload
import net.hmjn.hyperstorage.network.payload.SyncNodeNamesPayload
import net.minecraft.client.Minecraft
import net.minecraft.core.GlobalPos
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext

object ModNetworking {
    fun register(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(Hyperstorage.ID)

        // C2S Payloads
        registrar.playToServer(
            SetCustomNamePayload.ID,
            SetCustomNamePayload.STREAM_CODEC,
        ) { payload, context ->
            context.enqueueWork {
                handleSetCustomName(payload, context)
            }
        }

        registrar.playToServer(
            SetTargetNamePayload.ID,
            SetTargetNamePayload.STREAM_CODEC,
        ) { payload, context ->
            context.enqueueWork {
                handleSetTargetName(payload, context)
            }
        }

        registrar.playToServer(
            FetchNodeNamesPayload.ID,
            FetchNodeNamesPayload.STREAM_CODEC,
        ) { payload, context ->
            context.enqueueWork {
                handleFetchNodeNames(payload, context)
            }
        }

        // S2C Payloads
        registrar.playToClient(
            SyncNodeNamesPayload.ID,
            SyncNodeNamesPayload.STREAM_CODEC,
        ) { payload, context ->
            context.enqueueWork {
                handleSyncNodeNames(payload, context)
            }
        }
    }

    private fun handleSetCustomName(
        payload: SetCustomNamePayload,
        context: IPayloadContext,
    ) {
        val player = context.player() as? ServerPlayer ?: return
        val level = player.serverLevel()

        val blockEntity = level.getBlockEntity(payload.pos)
        if (blockEntity is HyperStorageBlockEntity) {
            val oldName = blockEntity.customName

            // Remove old name from registry
            val registry = NodeRegistrySavedData.get(level)
            if (oldName.isNotBlank()) {
                registry.removeNode(oldName)
            }

            // Set new name
            blockEntity.customName = payload.name
            if (payload.name.isNotBlank()) {
                registry.registerNode(payload.name, GlobalPos.of(level.dimension(), payload.pos))
            }

            blockEntity.setChanged()
            level.sendBlockUpdated(payload.pos, blockEntity.blockState, blockEntity.blockState, 3)

            // Sync updated list to all players
            PacketDistributor.sendToAllPlayers(SyncNodeNamesPayload(registry.getAllNames()))
        }
    }

    private fun handleSetTargetName(
        payload: SetTargetNamePayload,
        context: IPayloadContext,
    ) {
        val player = context.player() as? ServerPlayer ?: return
        val level = player.serverLevel()

        val blockEntity = level.getBlockEntity(payload.pos)
        if (blockEntity is HyperStorageBlockEntity) {
            blockEntity.targetName = payload.targetName
            blockEntity.setChanged()
            level.sendBlockUpdated(payload.pos, blockEntity.blockState, blockEntity.blockState, 3)
        }
    }

    private fun handleFetchNodeNames(
        payload: FetchNodeNamesPayload,
        context: IPayloadContext,
    ) {
        val player = context.player() as? ServerPlayer ?: return
        val level = player.serverLevel()
        val registry = NodeRegistrySavedData.get(level)
        PacketDistributor.sendToPlayer(player, SyncNodeNamesPayload(registry.getAllNames()))
    }

    private fun handleSyncNodeNames(
        payload: SyncNodeNamesPayload,
        context: IPayloadContext,
    ) {
        // Client-side execution
        val mc = Minecraft.getInstance()
        val screen = mc.screen
        if (screen is HyperStorageScreen) {
            screen.updateAvailableNames(payload.names)
        }
    }
}
