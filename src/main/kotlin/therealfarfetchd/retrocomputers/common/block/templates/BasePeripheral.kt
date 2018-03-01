package therealfarfetchd.retrocomputers.common.block.templates

import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.block.capability.IConnectable
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.qblock.QBlockConnectable
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer
import therealfarfetchd.retrocomputers.common.api.block.capability.SimpleBusConnectable

/**
 * Created by marco on 14.07.17.
 */
abstract class BasePeripheral(busId: Byte) : QBlockConnectable() {

  @Suppress("LeakingThis")
  protected val data: BusDataContainer = BusDataContainer(this::peek, this::poke, neighborSupport())

  private val standardConnectable = SimpleBusConnectable(data)

  init {
    data.busId = busId
  }

  abstract fun peek(addr: Byte): Byte

  abstract fun poke(addr: Byte, b: Byte)

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    data.save(nbt.nbt["B"])
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    data.load(nbt.nbt["B"])

    // backward compat
    if ("BusID" in nbt) data.busId = nbt.byte["BusID"]
  }

  open fun connectionForSide(f: EnumFacing?): IConnectable? = standardConnectable.takeIf { f != null }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    if (capability == Capabilities.Connectable) return connectionForSide(side) as T
    return super.getCapability(capability, side)
  }
}