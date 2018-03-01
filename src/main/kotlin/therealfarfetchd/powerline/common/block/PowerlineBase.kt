package therealfarfetchd.powerline.common.block

import net.minecraft.block.SoundType
import net.minecraft.util.ITickable
import therealfarfetchd.powerline.common.api.IConductorConfiguration
import therealfarfetchd.powerline.common.api.PowerConductor
import therealfarfetchd.powerline.common.api.PowerType
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.wires.BlockWire

abstract class PowerlineBase(width: Double, height: Double, conf: IConductorConfiguration) : BlockWire<PowerConductor>(width, height), ITickable {
  override val data: PowerConductor = PowerConductor(neighborSupport(), conf)

  abstract val wireType: PowerType

  override val soundType: SoundType = SoundType.CLOTH

  override fun update() {
    if (world.isServer) data.update()
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Save) {
      data.save(nbt.nbt["c"])
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Save) {
      data.load(nbt.nbt["c"])
    }
  }
}