package therealfarfetchd.rswires.common.init

import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.hctm.common.block.BaseWireItem
import therealfarfetchd.hctm.common.util.delegatedNotNull
import therealfarfetchd.hctm.common.util.flatten
import therealfarfetchd.rswires.ModID
import kotlin.properties.ReadOnlyProperty

object Items {

  private val tasks = mutableListOf<() -> Unit>()

  val RedAlloyWire by create("red_alloy_wire", BaseWireItem(Blocks.RedAlloyWire, Settings().group(ItemGroups.All)))
  val InsulatedWires by Blocks.InsulatedWires.mapValues { (color, block) -> create("${color.getName()}_insulated_wire", BaseWireItem(block, Settings().group(ItemGroups.All))) }.flatten()
  val UncoloredBundledCable by create("bundled_cable", BaseWireItem(Blocks.UncoloredBundledCable, Settings().group(ItemGroups.All)))
  val ColoredBundledCables by Blocks.ColoredBundledCables.mapValues { (color, block) -> create("${color.getName()}_bundled_cable", BaseWireItem(block, Settings().group(ItemGroups.All))) }.flatten()

  private fun <T : Block> create(name: String, block: T): ReadOnlyProperty<Items, BlockItem> {
    return create(name, BlockItem(block, Settings().group(ItemGroup.REDSTONE)))
  }

  private fun <T : Item> create(name: String, item: T): ReadOnlyProperty<Items, T> {
    var regItem: T? = null
    tasks += { regItem = Registry.register(Registry.ITEM, Identifier(ModID, name), item) }
    return delegatedNotNull { regItem }
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}