package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.block.BaseWireItem
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.block.ComputerBlock
import net.dblsaiko.retrocomputers.common.block.ComputerEntity
import net.dblsaiko.retrocomputers.common.block.DiskDriveBlock
import net.dblsaiko.retrocomputers.common.block.DiskDriveEntity
import net.dblsaiko.retrocomputers.common.block.RibbonCableBlock
import net.dblsaiko.retrocomputers.common.block.TerminalBlock
import net.dblsaiko.retrocomputers.common.block.TerminalEntity
import net.dblsaiko.retrocomputers.common.item.ImageDiskItem
import net.dblsaiko.retrocomputers.common.item.UserDiskItem
import net.dblsaiko.retrocomputers.common.packet.server.onKeyTypedTerminal
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

object Blocks {

  val COMPUTER = create(ComputerBlock(), "computer")
  val TERMINAL = create(TerminalBlock(), "terminal")
  val DISK_DRIVE = create(DiskDriveBlock(), "disk_drive")

  val RIBBON_CABLE = create(RibbonCableBlock(), "ribbon_cable")

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(MOD_ID, name), block)
  }

}

object BlockEntityTypes {

  val COMPUTER = create(::ComputerEntity, "computer", Blocks.COMPUTER)
  val TERMINAL = create(::TerminalEntity, "terminal", Blocks.TERMINAL)
  val DISK_DRIVE = create(::DiskDriveEntity, "disk_drive", Blocks.DISK_DRIVE)

  val RIBBON_CABLE = create(::BaseWireBlockEntity, "ribbon_cable", Blocks.RIBBON_CABLE)

  private fun <T : BlockEntity> create(builder: () -> T, name: String, vararg blocks: Block): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(MOD_ID, name), BlockEntityType.Builder.create(Supplier(builder), *blocks).build(null))
  }

  private fun <T : BlockEntity> create(builder: (BlockEntityType<T>) -> T, name: String, vararg blocks: Block): BlockEntityType<T> {
    var type: BlockEntityType<T>? = null
    val s = Supplier { builder(type!!) }
    type = BlockEntityType.Builder.create(s, *blocks).build(null)
    return Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(MOD_ID, name), type)
  }

}

object Items {

  val COMPUTER = create(Blocks.COMPUTER, "computer")
  val TERMINAL = create(Blocks.TERMINAL, "terminal")
  val DISK_DRIVE = create(Blocks.DISK_DRIVE, "disk_drive")

  val RIBBON_CABLE = create(BaseWireItem(Blocks.RIBBON_CABLE, Item.Settings()), "ribbon_cable")

  val SYS_DISKS = listOf(
    "forth",
    "extforth",
    "minforth",
    "decompiler",
    "radio",
    "retinal",
    "sortron"
  ).map(::createDisk)

  val USER_DISK = create(UserDiskItem(), "user_disk")

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Settings().group(ItemGroup.REDSTONE)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(MOD_ID, name), item)
  }

  private fun createDisk(path: String): ImageDiskItem {
    return create(ImageDiskItem(Identifier(MOD_ID, path)), "disk_$path")
  }

}

object Packets {

  object Client {
  }

  object Server {
    val TERMINAL_KEY_TYPED = Identifier(MOD_ID, "terminal_key")
  }

  init {
    ServerSidePacketRegistry.INSTANCE.register(Server.TERMINAL_KEY_TYPED, ::onKeyTypedTerminal)
  }

}