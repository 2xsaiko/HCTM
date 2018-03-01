package therealfarfetchd.powerline.common.block

import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.ITickable
import net.minecraftforge.common.capabilities.Capability
import therealfarfetchd.powerline.common.api.DefaultConductorConfiguration
import therealfarfetchd.powerline.common.api.IConductorConfiguration
import therealfarfetchd.powerline.common.api.PowerConductor
import therealfarfetchd.powerline.common.api.block.capability.SimplePowerConnectable
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.qblock.QBlockConnectable
import therealfarfetchd.quacklib.common.api.util.ChangeListener
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

abstract class BlockPowered(cconf: IConductorConfiguration = DefaultConductorConfiguration.LVDevice) : QBlockConnectable(), ITickable {
  val cond = PowerConductor(neighborSupport(), cconf)
  private val connectable = SimplePowerConnectable(cond)

  protected val clientCL = ChangeListener()
  protected val displayCL = ChangeListener(this::hasPower)
  protected val worldCL = ChangeListener()

  val hasPower
    get() = cond.chargeLevel == 100

  var clientHasPower = false

  override fun update() {
    if (world.isServer) {
      cond.update()

      if (worldCL.valuesChanged() or cond.cl.valuesChanged()) dataChanged()
      if (displayCL.valuesChanged()) {
        clientDataChanged(true)
        clientCL.valuesChanged() // clear clientCL changed flag
      } else if (clientCL.valuesChanged()) {
        clientDataChanged(false)
      }
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Save) {
      cond.save(nbt.nbt["c"])
    } else {
      nbt.bool["p"] = hasPower
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Save) {
      cond.load(nbt.nbt["c"])
    } else {
      clientHasPower = nbt.bool["p"]
    }
  }

  override fun applyProperties(state: IBlockState): IBlockState {
    return super.applyProperties(state).withProperty(PropPowered, clientHasPower)
  }

  override val properties: Set<IProperty<*>> = super.properties + PropPowered

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    return when (capability) {
      Capabilities.Connectable -> connectable as T
      else -> super.getCapability(capability, side)
    }
  }

  companion object {
    val PropPowered = PropertyBool.create("powered")
  }
}