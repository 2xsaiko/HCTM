package therealfarfetchd.retrocomputers.common.block

import mcmultipart.api.slot.EnumFaceSlot
import mcmultipart.api.slot.IPartSlot
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import therealfarfetchd.quacklib.common.api.extensions.getQBlock
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.IQBlockMultipart
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal2

@BlockDef(creativeTab = ModID)
class Backplane : Horizontal2(), IQBlockMultipart {

  override fun canStay(): Boolean {
    val p = pos.down()
    return world.getBlockState(p).isSideSolid(world, p, EnumFacing.UP) && getFacing() != null
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    facing = getFacing()!!.first
  }

  private fun getFacing(): Pair<EnumFacing, Int>? {
    val result = EnumFacing.HORIZONTALS.map { it to getDist(pos, it) }.filter { it.second != -1 }
    return if (result.size == 1) result.first() else null
  }

  private fun getDist(pos: BlockPos, f: EnumFacing): Int {
    val p = pos.offset(f)
    val te = world.getQBlock(p) ?: world.getQBlock(p, EnumFaceSlot.DOWN)
    return when (te) {
      is Backplane -> {
        val d = getDist(p, f) + 1
        if (d in 1..6) d else -1
      }
      is Computer -> if (te.facing == f) 0 else -1
      else -> -1
    }
  }

  override fun rotateBlock(axis: EnumFacing): Boolean = false

  override val collisionBox: Collection<AxisAlignedBB> = setOf(AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0))
  override val isFullBlock: Boolean = false

  override fun getPartSlot(): IPartSlot = EnumFaceSlot.DOWN

  override fun getItem(): ItemStack = Item.makeStack()

  override val material: Material = Material.IRON

  override val blockType: ResourceLocation = ResourceLocation(ModID, "backplane")

  companion object {
    val Block by WrapperImplManager.container(Backplane::class)
    val Item by WrapperImplManager.item(Backplane::class)
  }
}