package therealfarfetchd.tubes.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerDispenser
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.world.WorldServer
import net.minecraftforge.common.util.FakePlayerFactory
import therealfarfetchd.quacklib.common.api.extensions.isClient
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.IQBlockRedstone
import therealfarfetchd.quacklib.common.api.qblock.QBlockInventory
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.util.math.Random
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.tubes.ModID

@BlockDef(creativeTab = ModID)
class Deployer : QBlockInventory(), IQBlockRedstone {
  var facing: EnumFacing = EnumFacing.DOWN
  var powered: Boolean = false

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

  override fun createContainer(inventory: InventoryPlayer, player: EntityPlayer): Container = ContainerDispenser(inventory, this)

  override fun onNeighborChanged(side: EnumFacing) {
    super.onNeighborChanged(side)
    updateRedstone()
  }

  fun updateRedstone() {
    if (world.isClient) return
    val p = isBlockPowered()
    if (p != powered) {
      powered = p
      dataChanged()
      clientDataChanged()
      if (p) tryPlaceBlock()
    }
  }

  fun tryPlaceBlock() {
    val fp = FakePlayerFactory.getMinecraft(world as WorldServer)
    fp.setPositionAndRotation(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), facing.horizontalAngle, when (facing) {
      EnumFacing.UP -> 90f
      EnumFacing.DOWN -> -90f
      else -> 0f
    })

    // place block in front of deployer
    getItemToPlace()?.also { itemIndex ->
      getFacingToPlace()?.also { f ->
        val i = fp.inventory.currentItem
        val item = stacks[itemIndex]
        fp.inventory.mainInventory[i] = item
        val v = getPlacementVec(f)
        item.onItemUse(fp, world, pos.offset(facing), EnumHand.MAIN_HAND, f, v.xf, v.yf, v.zf)
        stacks[itemIndex] = fp.inventory.mainInventory[i]
      }
    }

    // right click block in front of deployer if no block is in the inventory
    ?: {
      val fp = FakePlayerFactory.getMinecraft(world as WorldServer)
      val p = pos.offset(facing)
      val bs = world.getBlockState(p)
      val v = getPlacementVec(facing.opposite)
      bs.block.onBlockActivated(world, p, bs, fp, EnumHand.MAIN_HAND, facing.opposite, v.xf, v.yf, v.zf)
    }()
  }

  fun getFacingToPlace(): EnumFacing? {
    val newBlockPos = pos.offset(facing)
    return if (world.isAirBlock(newBlockPos)) {
      val filter = { f: EnumFacing -> newBlockPos.offset(f).let { pos -> world.getBlockState(pos).let { bs -> !bs.block.isAir(bs, world, pos) } } }

      facing.opposite.takeIf(filter) ?:
      EnumFacing.DOWN.takeIf(filter) ?:
      facing.takeIf(filter) ?:
      EnumFacing.UP.takeIf(filter)
    } else null
  }

  fun getPlacementVec(facing: EnumFacing) = when (facing) {
    EnumFacing.DOWN -> Vec3(0.5f, 0.0f, 0.5f)
    EnumFacing.UP -> Vec3(0.5f, 1.0f, 0.5f)
    EnumFacing.NORTH -> Vec3(0.5f, 0.5f, 0.0f)
    EnumFacing.SOUTH -> Vec3(0.5f, 0.5f, 1.0f)
    EnumFacing.WEST -> Vec3(0.0f, 0.5f, 0.5f)
    EnumFacing.EAST -> Vec3(1.0f, 0.5f, 0.5f)
  }

  fun getItemToPlace(): Int? {
    val stacks = stacks.withIndex().filterNot { it.value.isEmpty }

    return if (stacks.isEmpty()) null
    else stacks[Random.nextInt(stacks.size)].index
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["f"] = facing.index
    nbt.bool["p"] = powered
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    facing = EnumFacing.getFront(nbt.ubyte["f"])
    powered = nbt.bool["p"]
  }

  override fun getSlotsForFace(side: EnumFacing): IntArray {
    return when (side) {
      facing -> intArrayOf()
      else -> super.getSlotsForFace(side)
    }
  }

  override fun applyProperties(state: IBlockState): IBlockState =
    super.applyProperties(state)
      .withProperty(PropFacing, facing)
      .withProperty(PropActive, powered)

  override fun getItem(): ItemStack = Item.makeStack()
  override fun getSizeInventory(): Int = 9
  override fun canConnect(side: EnumFacing): Boolean = side != facing

  override val properties: Set<IProperty<*>> = super.properties + PropFacing + PropActive
  override val material: Material = Material.ROCK
  override val blockType: ResourceLocation = ResourceLocation(ModID, "deployer")

  companion object {
    val Block by WrapperImplManager.container(Deployer::class)
    val Item by WrapperImplManager.item(Deployer::class)

    val PropFacing = PropertyEnum.create("facing", EnumFacing::class.java)
    val PropActive = PropertyBool.create("active")
  }
}