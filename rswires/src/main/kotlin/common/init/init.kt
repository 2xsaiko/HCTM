package therealfarfetchd.rswires.common.init

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.rswires.ModID
import java.util.function.Supplier

object Blocks {


  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(ModID, name), block)
  }

}

object BlockEntityTypes {


  private fun <T : BlockEntity> create(builder: () -> T, name: String, vararg blocks: Block): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY, Identifier(ModID, name), BlockEntityType.Builder.create(Supplier(builder), *blocks).build(null))
  }

}

object Items {


  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Settings().itemGroup(ItemGroup.REDSTONE)), name)
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