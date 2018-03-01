package therealfarfetchd.tubes.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.extensions.isClient
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.extensions.spawnAt
import therealfarfetchd.quacklib.common.api.qblock.IQBlockRedstone
import therealfarfetchd.quacklib.common.api.qblock.QBlock
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.tubes.ModID


@BlockDef(creativeTab = ModID)
class Destroyer : QBlock(), IQBlockRedstone {
  var facing: EnumFacing = EnumFacing.DOWN
  var powered: Boolean = false
  var success: Boolean = false

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    facing = EnumFacing.getDirectionFromEntityLiving(pos, placer)
    updateRedstone()
  }

  override fun rotateBlock(axis: EnumFacing): Boolean {
    facing = facing.rotateAround(axis.axis)
    dataChanged()
    clientDataChanged()
    return true
  }

  override fun onNeighborChanged(side: EnumFacing) {
    super.onNeighborChanged(side)
    updateRedstone()
  }

  fun updateRedstone() {
    if (world.isClient) return
    val p = isBlockPowered()
    if (p != powered) {
      if (p && tryDestroyBlock() || !p) {
        success = p
        clientDataChanged()
      }
      powered = p
      dataChanged()
    }
  }

  fun tryDestroyBlock(): Boolean {
    val p = pos.offset(facing)
    if (world.isAirBlock(p)) return false
    val state = world.getBlockState(p)
    if (state.getBlockHardness(world, p) < 0) return false
    val items = NonNullList.create<ItemStack>()
    state.block.getDrops(items, world, p, state, 0)
    world.playEvent(2001, p, net.minecraft.block.Block.getStateId(state))
    world.setBlockToAir(p)
    ejectDrops(items)
    return true
  }

  fun ejectDrops(drops: List<ItemStack>) {
    for (drop in drops) {
      drop.spawnAt(world, pos.offset(facing.opposite))
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["f"] = facing.index
    nbt.bool["p"] = powered
    nbt.bool["s"] = success
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getFront(nbt.ubyte["f"])
    powered = nbt.bool["p"]
    success = nbt.bool["s"]
  }

  override fun applyProperties(state: IBlockState): IBlockState =
    super.applyProperties(state)
      .withProperty(PropFacing, facing)
      .withProperty(PropActive, success)

  override fun getItem(): ItemStack = Item.makeStack()
  override fun canConnect(side: EnumFacing): Boolean = side != facing

  override val properties: Set<IProperty<*>> = super.properties + PropFacing + PropActive
  override val material: Material = Material.ROCK
  override val blockType: ResourceLocation = ResourceLocation(ModID, "destroyer")

  companion object {
    val Block by WrapperImplManager.container(Destroyer::class)
    val Item by WrapperImplManager.item(Destroyer::class)

    val PropFacing = PropertyEnum.create("facing", EnumFacing::class.java)
    val PropActive = PropertyBool.create("active")
  }
}