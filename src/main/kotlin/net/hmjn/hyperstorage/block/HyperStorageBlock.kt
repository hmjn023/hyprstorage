package net.hmjn.hyperstorage.block

import net.hmjn.hyperstorage.blockentity.HyperStorageBlockEntity
import net.hmjn.hyperstorage.blockentity.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

/** Hyper Storage Block - A high-performance storage block with Wasm integration */
class HyperStorageBlock(properties: Properties) : Block(properties), EntityBlock {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return HyperStorageBlockEntity(pos, state)
    }

    override fun <T : BlockEntity> getTicker(
            level: Level,
            state: BlockState,
            blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (level.isClientSide) {
            null
        } else {
            createTickerHelper(
                    blockEntityType,
                    ModBlockEntities.HYPER_STORAGE_BLOCK_ENTITY.get()
            ) { level, pos, state, blockEntity ->
                HyperStorageBlockEntity.serverTick(level, pos, state, blockEntity)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E : BlockEntity, A : BlockEntity> createTickerHelper(
            givenType: BlockEntityType<A>,
            expectedType: BlockEntityType<E>,
            ticker: BlockEntityTicker<in E>
    ): BlockEntityTicker<A>? {
        return if (expectedType == givenType) ticker as BlockEntityTicker<A> else null
    }

    override fun useWithoutItem(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            player: Player,
            hitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is HyperStorageBlockEntity) {
                // Open the menu
                player.openMenu(blockEntity, pos)
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    override fun onRemove(
            state: BlockState,
            level: Level,
            pos: BlockPos,
            newState: BlockState,
            movedByPiston: Boolean
    ) {
        if (!state.`is`(newState.block)) {
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is HyperStorageBlockEntity) {
                blockEntity.drops()
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston)
    }
}
