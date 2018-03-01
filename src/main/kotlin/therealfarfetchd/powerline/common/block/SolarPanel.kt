package therealfarfetchd.powerline.common.block

import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.common.capabilities.Capability
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.capability.SolarPanelConnectable
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef

@BlockDef(creativeTab = ModID)
class SolarPanel : BlockPowered() {
  private val connectable = SolarPanelConnectable(cond)

  init {
    displayCL.removeProperties(this::hasPower)
  }

  override fun update() {
    super.update()

    if (world.isServer) {
      if (cond.voltage < cond.conf.voltageSpec && world.canBlockSeeSky(pos.up()) && world.isDaytime && !world.isRaining && !world.isThundering) {
        cond.applyPower(0.75)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    return when (capability) {
      Capabilities.Connectable -> when (side) {
        EnumFacing.DOWN -> super.getCapability(capability, side)
        EnumFacing.UP -> null
        EnumFacing.NORTH,
        EnumFacing.SOUTH,
        EnumFacing.WEST,
        EnumFacing.EAST -> connectable as T
        null -> super.getCapability(capability, side)
      }
      else -> super.getCapability(capability, side)
    }
  }

  override fun canStay(): Boolean = world.getBlockState(pos.down()).getBlockFaceShape(world, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID
  override fun getItem(): ItemStack = Item.makeStack()

  override val isFullBlock: Boolean = false
  override val collisionBox: Collection<AxisAlignedBB> = setOf(AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0))
  override val material: Material = Material.WOOD
  override val blockType: ResourceLocation = ResourceLocation(ModID, "solar_panel")

  companion object {
    val Block by WrapperImplManager.container(SolarPanel::class)
    val Item by WrapperImplManager.item(SolarPanel::class)
  }
}