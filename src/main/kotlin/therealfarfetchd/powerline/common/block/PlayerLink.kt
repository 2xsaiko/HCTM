package therealfarfetchd.powerline.common.block

import net.minecraft.block.material.Material
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.WorldServer
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler
import net.minecraftforge.items.wrapper.PlayerInvWrapper
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.items.PoweredItemHandler
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import java.util.*

@BlockDef(creativeTab = ModID, registerModels = false)
class PlayerLink : BlockPowered() {
  var player: UUID = UUID(0L, 0L)

  private var stackHandler: IItemHandler? = null

  var isConnected = false

  init {
    clientCL.addProperties(this::isConnected)
  }

  override fun onPlaced(placer: EntityLivingBase?, stack: ItemStack?, sidePlaced: EnumFacing, hitX: Float, hitY: Float, hitZ: Float) {
    super.onPlaced(placer, stack, sidePlaced, hitX, hitY, hitZ)
    placer?.also { player = it.uniqueID }
  }

  override fun update() {
    super.update()
    if (world.isServer) {
      stackHandler = null
      if (world.totalWorldTime % 200 == 0L)
        getStackHandler() // refreshes isConnected
    }
  }

  private fun getInventory() =
    (world as WorldServer).minecraftServer?.playerList?.getPlayerByUUID(player)

  private fun getStackHandler(): IItemHandler {
    if (stackHandler == null) {
      stackHandler = getInventory()
                       ?.let { PoweredItemHandler(this, PlayerInvWrapper(it.inventory)) }
                     ?: EmptyHandler.INSTANCE
      isConnected = stackHandler !is EmptyHandler
    }
    return stackHandler!!
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    if (target == DataTarget.Save) {
      nbt.uuid["Player"] = player
    }
    if (target == DataTarget.Client) {
      nbt.bool["l"] = isConnected
    }
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    if (target == DataTarget.Save) {
      player = nbt.uuid["Player"]
    }
    if (target == DataTarget.Client) {
      isConnected = nbt.bool["l"]
    }
  }

  override fun getItem() = Item.makeStack()

  override fun canRenderInLayer(layer: BlockRenderLayer) =
    layer in setOf(BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.CUTOUT)

  override val material = Material.IRON
  override val blockType = ResourceLocation(ModID, "player_link")

  @Suppress("UNCHECKED_CAST")
  override fun <T> getCapability(capability: Capability<T>, side: EnumFacing?): T? {
    return when (capability) {
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> when {
        side?.axis != EnumFacing.Axis.Y -> getStackHandler() as T
        else -> null
      }
      else -> super.getCapability(capability, side)
    }
  }

  companion object {
    val Block by WrapperImplManager.container(PlayerLink::class)
    val Item by WrapperImplManager.item(PlayerLink::class)
  }
}