package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.BlockState
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes
import therealfarfetchd.retrocomputers.common.item.ext.ItemDisk
import therealfarfetchd.retrocomputers.common.util.ext.makeStack

class DiskDriveBlock : BaseBlock() {

  override fun activate(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): Boolean {
    val ent = world.getBlockEntity(pos) as? DiskDriveEntity ?: return false
    return ent.ejectDisk() || ent.insertDisk(player.getStackInHand(hand))
  }

  override fun onBlockRemoved(state: BlockState, world: World, pos: BlockPos, newState: BlockState, boolean_1: Boolean) {
    if (state.block != newState.block) {
      (world.getBlockEntity(pos) as? DiskDriveEntity)?.ejectDisk(breakBlock = true)
    }

    super.onBlockRemoved(state, world, pos, newState, boolean_1)
  }

  override fun createBlockEntity(view: BlockView) = DiskDriveEntity()

}

class DiskDriveEntity : BaseBlockEntity(BlockEntityTypes.DiskDrive) {

  private var stack = Items.AIR.makeStack()
  private var clientHasStack = false

  override var busId: Byte = 2

  fun ejectDisk(breakBlock: Boolean = false): Boolean {
    val world = getWorld() ?: return false
    if (!hasDisk()) return false

    if (!breakBlock) {
      val dirVec = Vec3d(cachedState[BaseBlock.Direction].vector)
      val pos = Vec3d(pos)
        .add(0.5, 0.5, 0.5)
        .add(dirVec.multiply(0.75))
      val item = ItemEntity(world, pos.x, pos.y, pos.z, stack)
      item.velocity = dirVec.multiply(0.1)
      world.spawnEntity(item)
    } else {
      ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
    }

    stack = Items.AIR.makeStack()

    return true
  }

  private fun hasDisk(): Boolean {
    return if ((getWorld() ?: return false).isClient) clientHasStack else !stack.isEmpty
  }

  fun insertDisk(stack: ItemStack): Boolean {
    if (stack.isEmpty || stack.item !is ItemDisk) return false

    this.stack = stack.split(1)

    return true
  }

  override fun readData(at: Byte): Byte {
    return 0
  }

  override fun storeData(at: Byte, data: Byte) {

  }

}