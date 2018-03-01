package therealfarfetchd.powerline.common.item

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.api.PowerConductor
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.util.ItemDef

@ItemDef
object ItemMultimeter : Item() {

  init {
    registryName = ResourceLocation(ModID, "multimeter")

    maxStackSize = 1
  }

  @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  override fun onItemUseFirst(player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand?): EnumActionResult {
    val te = world.getTileEntity(pos)
    if (te != null) {
      val bo = getBusContainerFor(te, side) ?: getBusContainerFor(te, null)
      if (bo is PowerConductor) {
        if (world.isServer) {
          val text = "Reading %.3fV, %.3fA (%.3fW)".format(bo.voltage, bo.current, bo.power)
          player.sendStatusMessage(ITextComponent.Serializer.fromJsonLenient("{text:\"$text\"}"), true)
        }
        return EnumActionResult.SUCCESS
      }
    }
    return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand)
  }

  private fun getBusContainerFor(te: TileEntity, side: EnumFacing?): PowerConductor? {
    if (te.hasCapability(Capabilities.Connectable, side)) {
      val cap = te.getCapability(Capabilities.Connectable, side)!!
      val f = EnumFacing.VALUES.firstOrNull { cap.getEdge(it) != null } // TODO: implement logic for multiple BusObjects (Multiparts!)
      val data = cap.getEdge(f) as? PowerConductor
      if (data != null) return data
    }
    return null
  }
}