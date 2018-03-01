package therealfarfetchd.retrocomputers.common.net

import io.netty.buffer.ByteBuf
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.unsigned
import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer

class PacketChangeBusID(var busID: Byte, var dim: Int, var pos: BlockPos, var side: EnumFacing?, var facing: EnumFacing?) : IMessage {

  @Suppress("unused") // this is used when the packet is received on the server
  constructor() : this(0, 0, BlockPos(0, 0, 0), EnumFacing.DOWN, null)

  override fun fromBytes(buf: ByteBuf) {
    with(PacketBuffer(buf)) {
      busID = readByte()
      dim = readInt()
      pos = readBlockPos()

      val r1 = readByte().unsigned
      side = when (r1) {
        in (0..5) -> EnumFacing.VALUES[r1]
        else -> null
      }

      val r = readByte().unsigned
      facing = when (r) {
        in (0..5) -> EnumFacing.VALUES[r]
        else -> null
      }
    }
  }

  override fun toBytes(buf: ByteBuf) {
    with(PacketBuffer(buf)) {
      writeByte(busID.toInt())
      writeInt(dim)
      writeBlockPos(pos)
      writeByte(side?.index ?: 6)
      writeByte(facing?.index ?: 6)
    }
  }

  object Handler : IMessageHandler<PacketChangeBusID, IMessage> {
    override fun onMessage(message: PacketChangeBusID, ctx: MessageContext): IMessage? {
      with(message) {
        val world = DimensionManager.getWorld(dim)
        val te = world.getTileEntity(pos)
        if (te != null) {
          if (te.hasCapability(Capabilities.Connectable, side)) {
            val cap = te.getCapability(Capabilities.Connectable, side)!!
            val bo = cap.getEdge(facing)
            if (bo is BusDataContainer) {
              bo.busId = busID
              val blockState = world.getBlockState(pos)
              world.markAndNotifyBlock(pos, null, blockState, blockState, 3)
            }
          }
        }
      }
      return null
    }
  }
}