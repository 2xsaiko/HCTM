package therealfarfetchd.powerline.common.block

import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.property.IExtendedBlockState
import net.minecraftforge.common.property.IUnlistedProperty
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.block.FluidPipeContainer
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.wires.BlockWireCentered
import therealfarfetchd.quacklib.common.api.wires.EnumWireConnection

@BlockDef(registerModels = false, creativeTab = ModID)
class FluidPipe : BlockWireCentered<FluidPipeContainer>(0.5), ITickable {
  var joints: Set<EnumFacing> = emptySet()

  override val data: FluidPipeContainer = FluidPipeContainer(neighborSupport { !it.isVertical })

  val worldCL = ChangeListener(data::pressure, data::fluid, data::amount)
  val clientCL = ChangeListener(data::fluid, data::amount)

  override fun update() {
    if (world.isServer) {
      data.update()

      if (worldCL.valuesChanged()) dataChanged()
      if (clientCL.valuesChanged()) clientDataChanged()
    }
  }

  override fun connectionsChanged() {
    super.connectionsChanged()
    val changed = true // TODO: add hook that gets called after every updateCableConnections()

    val conns = cr.connections.filter { it.value != EnumWireConnection.None }.map { it.key.direction }
    val oldjoints = joints
    joints = emptySet()
    if (conns.map { it.axis }.toSet().size != 1) {
      joints = conns.toSet()
    } else {
      if (!changed) for (c in conns) {
        val otherConn = world.getTileEntity(pos.offset(c))?.getCapability(Capabilities.Connectable, c.opposite)
        if (otherConn?.getAdditionalData(null, "joints") as? Boolean == true) joints += c
      }
    }

    if (joints != oldjoints || changed) {
      dataChanged()
      clientDataChanged()

      val changedJoints = (oldjoints - joints) + (joints - oldjoints)

      for (j in changedJoints) {
        world.neighborChanged(pos.offset(j), FluidPipe.Block, pos)
      }

      if (changed) {
        world.neighborChanged(pos, Block, pos)
      }

      // changed = true
    }
  }

  override fun getAdditionalData(side: EnumFacing, facing: EnumFacing?, key: String): Any? {
    if (key == "joints") return joints.contains(side)
    return super.getAdditionalData(side, facing, key)
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.ubyte["Joints"] = joints.map { 1 shl it.index }.sum()
    data.save(nbt.nbt["Fl"])
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    joints = EnumFacing.VALUES.filter { (1 shl it.index) and nbt.ubyte["Joints"] != 0 }.toSet()
    data.load(nbt.nbt["Fl"])
  }

  override val dataType: ResourceLocation = DataType

  override val renderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override fun applyExtendedProperties(state: IExtendedBlockState): IExtendedBlockState {
    return super.applyExtendedProperties(state).withProperty(PropJoints, joints)
  }

  override val unlistedProperties: Set<IUnlistedProperty<*>> = super.unlistedProperties + PropJoints

  override val blockType: ResourceLocation = ResourceLocation(ModID, "fluid_pipe")

  override fun getItem(): ItemStack = Item.makeStack()

  companion object {
    val Block by WrapperImplManager.container(FluidPipe::class)
    val Item by WrapperImplManager.item(FluidPipe::class)

    val DataType = ResourceLocation(ModID, "fluid")

    val PropJoints = object : IUnlistedProperty<Set<EnumFacing>> {
      @Suppress("UNCHECKED_CAST")
      override fun getType(): Class<Set<EnumFacing>> = Set::class.java as Class<Set<EnumFacing>>

      override fun valueToString(value: Set<EnumFacing>?): String = value?.joinToString() ?: "null"
      override fun getName(): String = "joints"
      override fun isValid(value: Set<EnumFacing>?): Boolean = value != null
    }
  }
}