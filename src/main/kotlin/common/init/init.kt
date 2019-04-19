package therealfarfetchd.retrocomputers.common.init

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.item.block.BlockItem
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.ComputerBlock
import therealfarfetchd.retrocomputers.common.block.ComputerEntity
import therealfarfetchd.retrocomputers.common.block.DiskDriveBlock
import therealfarfetchd.retrocomputers.common.block.DiskDriveEntity
import therealfarfetchd.retrocomputers.common.block.TerminalBlock
import therealfarfetchd.retrocomputers.common.block.TerminalEntity
import therealfarfetchd.retrocomputers.common.block.WireBlock
import therealfarfetchd.retrocomputers.common.block.WireItem

object BlockEntityTypes {

  val Computer = create(::ComputerEntity, "computer")
  val Terminal = create(::TerminalEntity, "terminal")
  val DiskDrive = create(::DiskDriveEntity, "disk_drive")

//  val Wire = create(::WireEntity, "wire")

  private fun <T : BlockEntity> create(builder: () -> T, name: String): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY, Identifier(ModID, name), BlockEntityType.Builder.create(builder).build(null))
  }

}

object Blocks {

  val Computer = create(ComputerBlock(), "computer")
  val Terminal = create(TerminalBlock(), "terminal")
  val DiskDrive = create(DiskDriveBlock(), "disk_drive")

  val Wire = create(WireBlock(), "wire")

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(ModID, name), block)
  }

}

object Items {

  val Computer = create(Blocks.Computer, "computer")
  val Terminal = create(Blocks.Terminal, "terminal")
  val DiskDrive = create(Blocks.DiskDrive, "disk_drive")

  val Wire = create(WireItem(), "wire")

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Settings().itemGroup(ItemGroup.REDSTONE)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
  }

}