package therealfarfetchd.retrocomputers.client.gui

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import therealfarfetchd.quacklib.client.api.gui.AbstractGuiLogic
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.client.gui.elements.AddressBoard
import therealfarfetchd.retrocomputers.common.api.block.SimpleBusDataContainer
import therealfarfetchd.retrocomputers.common.net.PacketChangeBusID

class BusAddressLogic : AbstractGuiLogic() {

  val world: World by params()
  val pos: BlockPos by params()
  val side: EnumFacing? by params()
  val facing: EnumFacing? by params()

  val address: AddressBoard by component()

  override fun init() {
    address.action {
      val packet = PacketChangeBusID(value, world.provider.dimension, pos, side, facing)
      RetroComputers.Net.sendToServer(packet)
    }
  }

  override fun update() {
    val te = world.getTileEntity(pos)
    if (te != null) {
      if (te.hasCapability(Capabilities.Connectable, side)) {
        val cap = te.getCapability(Capabilities.Connectable, side)!!
        val bo = cap.getEdge(facing)
        if (bo is SimpleBusDataContainer) {
          address.value = bo.busId
          return
        }
      }
    }
    close()
  }

}