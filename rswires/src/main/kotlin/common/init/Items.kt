package net.dblsaiko.rswires.common.init

import net.dblsaiko.hctm.common.block.BaseWireItem
import net.dblsaiko.hctm.common.util.delegatedNotNull
import net.dblsaiko.hctm.common.util.flatten
import net.dblsaiko.rswires.MOD_ID
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.properties.ReadOnlyProperty

object Items {

  private val tasks = mutableListOf<() -> Unit>()

  val RED_ALLOY_WIRE by create("red_alloy_wire", BaseWireItem(Blocks.RED_ALLOY_WIRE, Settings().group(ItemGroups.ALL)))
  val INSULATED_WIRES by Blocks.INSULATED_WIRES.mapValues { (color, block) -> create("${color.getName()}_insulated_wire", BaseWireItem(block, Settings().group(ItemGroups.ALL))) }.flatten()
  val UNCOLORED_BUNDLED_CABLE by create("bundled_cable", BaseWireItem(Blocks.UNCOLORED_BUNDLED_CABLE, Settings().group(ItemGroups.ALL)))
  val COLORED_BUNDLED_CABLES by Blocks.COLORED_BUNDLED_CABLES.mapValues { (color, block) -> create("${color.getName()}_bundled_cable", BaseWireItem(block, Settings().group(ItemGroups.ALL))) }.flatten()

  val NULL_CELL by create("null_cell", BlockItem(Blocks.NULL_CELL, Item.Settings().group(ItemGroups.ALL)))

  val RED_ALLOY_COMPOUND by create("red_alloy_compound", Item(Item.Settings().group(ItemGroups.ALL)))
  val RED_ALLOY_INGOT by create("red_alloy_ingot", Item(Item.Settings().group(ItemGroups.ALL)))

  private fun <T : Block> create(name: String, block: T): ReadOnlyProperty<Items, BlockItem> {
    return create(name, BlockItem(block, Settings().group(ItemGroup.REDSTONE)))
  }

  private fun <T : Item> create(name: String, item: T): ReadOnlyProperty<Items, T> {
    var regItem: T? = null
    tasks += { regItem = Registry.register(Registry.ITEM, Identifier(MOD_ID, name), item) }
    return delegatedNotNull { regItem }
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}