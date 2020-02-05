package therealfarfetchd.rswires.common.init

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.hctm.common.block.BaseWireBlockEntity
import therealfarfetchd.hctm.common.block.BaseWireItem
import therealfarfetchd.rswires.ModID
import therealfarfetchd.rswires.common.block.BundledCableBlock
import therealfarfetchd.rswires.common.block.InsulatedWireBlock
import therealfarfetchd.rswires.common.block.RedAlloyWireBlock
import java.util.function.Supplier

object BlockEntityTypes {

  val RedAlloyWire = create(::BaseWireBlockEntity, "red_alloy_wire")
  val InsulatedWire = create(::BaseWireBlockEntity, "insulated_wire")
  val BundledCable = create(::BaseWireBlockEntity, "bundled_cable")

  private fun <T : BlockEntity> create(builder: () -> T, name: String, vararg blocks: Block): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), BlockEntityType.Builder.create(Supplier(builder), *blocks).build(null))
  }

  private fun <T : BlockEntity> create(builder: (BlockEntityType<T>) -> T, name: String): BlockEntityType<T> {
    var type: BlockEntityType<T>? = null
    val s = Supplier { builder(type!!) }
    type = BlockEntityType.Builder.create(s).build(null)
    return Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), type)
  }

}

object Blocks {

  private val wireSettings = FabricBlockSettings.of(Material.STONE).breakByHand(true).noCollision().strength(0.25f, 0.25f).build()

  val RedAlloyWire = create(RedAlloyWireBlock(wireSettings), "red_alloy_wire")
  val InsulatedWires = DyeColor.values().associate { it to create(InsulatedWireBlock(wireSettings, it), "${it.getName()}_insulated_wire") }
  val UncoloredBundledCable = create(BundledCableBlock(wireSettings, null), "bundled_cable")
  val ColoredBundledCables = DyeColor.values().associate { it to create(BundledCableBlock(wireSettings, it), "${it.getName()}_bundled_cable") }

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(ModID, name), block)
  }

}

object Items {

  val RedAlloyWire = create(BaseWireItem(Blocks.RedAlloyWire, Item.Settings()), "red_alloy_wire")
  val InsulatedWires = Blocks.InsulatedWires.mapValues { (color, block) -> create(BaseWireItem(block, Item.Settings()), "${color.getName()}_insulated_wire") }
  val UncoloredBundledCable = create(BaseWireItem(Blocks.UncoloredBundledCable, Item.Settings()), "bundled_cable")
  val ColoredBundledCables = Blocks.ColoredBundledCables.mapValues { (color, block) -> create(BaseWireItem(block, Item.Settings()), "${color.getName()}_bundled_cable") }

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Settings().group(ItemGroup.REDSTONE)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
  }

}

object Packets {

  object Client {
  }

  object Server {
  }

  init {
  }

}