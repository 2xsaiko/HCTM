package net.dblsaiko.hctm.common.init

import net.dblsaiko.hctm.ModID
import net.dblsaiko.hctm.common.util.delegatedNotNull
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

  val Screwdriver by create("screwdriver", Item(Settings().group(ItemGroups.All)))

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