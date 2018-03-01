package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import therealfarfetchd.quacklib.common.api.extensions.copyTo
import therealfarfetchd.quacklib.common.api.extensions.isServer
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.api.extensions.unsigned
import therealfarfetchd.quacklib.common.api.qblock.WrapperImplManager
import therealfarfetchd.quacklib.common.api.util.BlockDef
import therealfarfetchd.quacklib.common.api.util.DataTarget
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.templates.Horizontal
import java.nio.charset.Charset

/**
 * Half-Life!
 *
 * Address map:
 * $00-$0F: Last player name
 * $10-$13: Last player ID
 * $14: Last player health
 * $15: State (0: clear, 1: scanning, 2: success, 3: failure)
 */
@BlockDef(creativeTab = ModID)
class RetinalScanner : Horizontal(6) {
  private val playerName: ByteArray = ByteArray(15, { 0x20 })
  private var playerID: Int = 0
  private var playerHealth: Byte = 0
  private var state: Byte = 0
  private var lastStateChange: Long = 0L

  val renderState: Int
    get() = when (state.unsigned) {
      1 -> if ((world.totalWorldTime - lastStateChange) % 4 < 2) 0 else 1
      2 -> 1
      3 -> if ((world.totalWorldTime - lastStateChange) % 4 < 2) 2 else 1
      else -> 0
    }

  override fun onActivated(player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
    if (facing == this.facing) {
      if (world.isServer && state.unsigned == 0) {
        state = 1
        lastStateChange = world.totalWorldTime
        player.name.toByteArray(Charset.forName("US-ASCII")).copyTo(playerName)
        playerID = player.name.hashCode()
        playerHealth = (255 * minOf(1f, player.health / player.maxHealth)).toByte()
        dataChanged()
        clientDataChanged(renderUpdate = false)
      }
      return true
    }
    return false
  }

  override fun peek(addr: Byte): Byte {
    return when (addr.unsigned) {
      in 0x00..0x0F -> playerName[addr.unsigned]
      0x10 -> playerID.toByte()
      0x11 -> (playerID shr 8).toByte()
      0x12 -> (playerID shr 16).toByte()
      0x13 -> (playerID shr 24).toByte()
      0x14 -> playerHealth
      0x15 -> state
      else -> 0
    }
  }

  override fun poke(addr: Byte, b: Byte) {
    when (addr.unsigned) {
      0x15 -> {
        lastStateChange = world.totalWorldTime
        when (b.unsigned) {
          1, 2, 3 -> state = b
          else -> {
            state = 0
            // clear vars
            ByteArray(15, { 0x20 }).copyTo(playerName)
            playerID = 0
            playerHealth = 0
          }
        }
        clientDataChanged(renderUpdate = false)
      }
    }
    dataChanged()
  }

  override fun loadData(nbt: QNBTCompound, target: DataTarget) {
    super.loadData(nbt, target)
    state = nbt.byte["S"]
    lastStateChange = nbt.long["T"]
    if (target == DataTarget.Save) {
      nbt.bytes["PlayerName"].copyTo(playerName)
      playerID = nbt.int["PlayerID"]
      playerHealth = nbt.byte["PlayerHealth"]
    }
  }

  override fun saveData(nbt: QNBTCompound, target: DataTarget) {
    super.saveData(nbt, target)
    nbt.byte["S"] = state
    nbt.long["T"] = lastStateChange
    if (target == DataTarget.Save) {
      nbt.bytes["PlayerName"] = playerName
      nbt.int["PlayerID"] = playerID
      nbt.byte["PlayerHealth"] = playerHealth
    }
  }

  //  override val hasFastRenderer: Boolean = true

  override val material: Material = Material.IRON

  override fun getItem(): ItemStack = Item.makeStack()

  override val blockType: ResourceLocation = ResourceLocation(ModID, "retinal_scanner")

  companion object {
    val Block by WrapperImplManager.container(RetinalScanner::class)
    val Item by WrapperImplManager.item(RetinalScanner::class)
  }
}