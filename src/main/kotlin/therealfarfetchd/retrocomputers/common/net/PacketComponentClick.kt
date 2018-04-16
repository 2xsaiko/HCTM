package therealfarfetchd.retrocomputers.common.net

import io.netty.buffer.ByteBuf
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import therealfarfetchd.retrocomputers.common.block.Rack

class PacketComponentClick(var pos: BlockPos, var slotId: Int, var bb: Int, var hand: EnumHand) : IMessage {
  @Suppress("unused")
  constructor() : this(BlockPos.ORIGIN, 0, 0, EnumHand.MAIN_HAND)

  override fun fromBytes(buf: ByteBuf) {
    val pb = PacketBuffer(buf)
    pos = pb.readBlockPos()
    slotId = pb.readVarInt()
    bb = pb.readVarInt()
    hand = pb.readEnumValue(EnumHand::class.java)
  }

  override fun toBytes(buf: ByteBuf) {
    val pb = PacketBuffer(buf)
    pb.writeBlockPos(pos)
    pb.writeVarInt(slotId)
    pb.writeVarInt(bb)
    pb.writeEnumValue(hand)
  }

  object Handler : IMessageHandler<PacketComponentClick, Nothing> {
    override fun onMessage(message: PacketComponentClick, ctx: MessageContext): Nothing? {
      val player = ctx.serverHandler.player
      val world = player.world
      val te = world.getTileEntity(message.pos) as? Rack.Tile ?: return null

      te.clickBox(player, message.slotId, message.bb, message.hand)
      return null
    }
  }
}