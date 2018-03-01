package therealfarfetchd.retrocomputers.common.item

import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import therealfarfetchd.quacklib.client.api.gui.GuiApi
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.isClient
import therealfarfetchd.quacklib.common.api.util.ItemDef
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer

/**
 * Created by marco on 15.07.17.
 */
@ItemDef(creativeTab = ModID)
object Screwdriver : Item() {

  init {
    registryName = ResourceLocation(ModID, "screwdriver")
    maxStackSize = 1
  }

  override fun isFull3D(): Boolean = true

  override fun onItemUseFirst(player: EntityPlayer, world: World, pos: BlockPos, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, hand: EnumHand?): EnumActionResult {
    if (player.isSneaking) {
      val te = world.getTileEntity(pos)
      if (te != null) {
        val (f, s, bo) = getBusContainerFor(te, side).takeIf { it.third != null } ?: getBusContainerFor(te, null)
        if (bo is BusDataContainer) {
          if (world.isClient) {
            val gui = GuiApi.loadGui(ResourceLocation("retrocomputers:bus_address"), mapOf(
              "world" to world,
              "pos" to pos,
              "side" to s,
              "facing" to f
            ))
            Minecraft.getMinecraft().displayGuiScreen(gui)
          }
          return EnumActionResult.SUCCESS
        }
      }
    }
    return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand)
  }

  private fun getBusContainerFor(te: TileEntity, side: EnumFacing?): Triple<EnumFacing?, EnumFacing?, BusDataContainer?> {
    if (te.hasCapability(Capabilities.Connectable, side)) {
      val cap = te.getCapability(Capabilities.Connectable, side)!!
      val f = EnumFacing.VALUES.firstOrNull { cap.getEdge(it) != null } // TODO: implement logic for multiple BusObjects (Multiparts!)
      val data = cap.getEdge(f) as? BusDataContainer
      if (data != null) return Triple(f, side, data)
    }
    return Triple(null, null, null)
  }

  // TODO remove
  //  @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
  //  fun point3dto2d(side: EnumFacing, x: Float, y: Float, z: Float): Pair<Float, Float> {
  //    return when (side.axis) {
  //      EnumFacing.Axis.X -> z to y
  //      EnumFacing.Axis.Y -> x to z
  //      EnumFacing.Axis.Z -> x to y
  //    }
  //  }

}