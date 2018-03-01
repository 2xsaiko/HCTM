package therealfarfetchd.retrocomputers.common.block

import mcmultipart.api.slot.EnumFaceSlot
import mcmultipart.api.slot.IPartSlot
import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import therealfarfetchd.quacklib.common.api.extensions.copyTo
import therealfarfetchd.quacklib.common.api.extensions.getQBlock
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.IQBlockMultipart
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal2

@BlockDef(creativeTab = ModID)
class Memory : Horizontal2(), IQBlockMultipart {
  val mem: ByteArray = ByteArray(8192)

  override fun canStay(): Boolean {
    return actualWorld.getQBlock(pos, EnumFaceSlot.DOWN) is Backplane
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    facing = (actualWorld.getQBlock(pos, EnumFaceSlot.DOWN) as Backplane).facing
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Save) {
      nbt.bytes["Memory"] = mem
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Save) {
      nbt.bytes["Memory"].copyTo(mem)
    }
  }

  override fun rotateBlock(axis: EnumFacing): Boolean = false

  override val collisionBox: Collection<AxisAlignedBB> = setOf(AxisAlignedBB(0.0, 0.125, 0.0, 1.0, 1.0, 1.0))
  override val isFullBlock: Boolean = false

  override fun getPartSlot(): IPartSlot = EnumFaceSlot.UP

  override fun getItem(): ItemStack = Item.makeStack()

  override val material: Material = Material.IRON

  override val blockType: ResourceLocation = ResourceLocation(ModID, "memory")

  companion object {
    val Block by WrapperImplManager.container(Memory::class)
    val Item by WrapperImplManager.item(Memory::class)
  }
}