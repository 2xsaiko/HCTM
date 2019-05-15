package therealfarfetchd.retrocomputers.common.init

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
import therealfarfetchd.hctm.common.block.BaseWireBlockEntity
import therealfarfetchd.hctm.common.block.BaseWireItem
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.common.block.ComputerBlock
import therealfarfetchd.retrocomputers.common.block.ComputerEntity
import therealfarfetchd.retrocomputers.common.block.DiskDriveBlock
import therealfarfetchd.retrocomputers.common.block.DiskDriveEntity
import therealfarfetchd.retrocomputers.common.block.RibbonCableBlock
import therealfarfetchd.retrocomputers.common.block.TerminalBlock
import therealfarfetchd.retrocomputers.common.block.TerminalEntity
import therealfarfetchd.retrocomputers.common.item.ImageDiskItem
import therealfarfetchd.retrocomputers.common.item.UserDiskItem
import therealfarfetchd.retrocomputers.common.packet.server.onKeyTypedTerminal
import java.util.function.Supplier

object BlockEntityTypes {

  val Computer = create(::ComputerEntity, "computer")
  val Terminal = create(::TerminalEntity, "terminal")
  val DiskDrive = create(::DiskDriveEntity, "disk_drive")

  val RibbonCable = create(::BaseWireBlockEntity, "ribbon_cable")

  private fun <T : BlockEntity> create(builder: () -> T, name: String): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY, Identifier(ModID, name), BlockEntityType.Builder.create(builder).build(null))
  }

  private fun <T : BlockEntity> create(builder: (BlockEntityType<T>) -> T, name: String): BlockEntityType<T> {
    var type: BlockEntityType<T>? = null
    val s = Supplier { builder(type!!) }
    type = BlockEntityType.Builder.create(s).build(null)
    return Registry.register(Registry.BLOCK_ENTITY, Identifier(ModID, name), type)
  }

}

object Blocks {

  val Computer = create(ComputerBlock(), "computer")
  val Terminal = create(TerminalBlock(), "terminal")
  val DiskDrive = create(DiskDriveBlock(), "disk_drive")

  val RibbonCable = create(RibbonCableBlock(), "ribbon_cable")

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(ModID, name), block)
  }

}

object Items {

  val Computer = create(Blocks.Computer, "computer")
  val Terminal = create(Blocks.Terminal, "terminal")
  val DiskDrive = create(Blocks.DiskDrive, "disk_drive")

  val RibbonCable = create(BaseWireItem(Blocks.RibbonCable), "ribbon_cable")

  val SysDisks = listOf(
    "forth",
    "extforth",
    "minforth",
    "decompiler",
    "radio",
    "retinal",
    "sortron"
  ).map(::createDisk)

  val UserDisk = create(UserDiskItem(), "user_disk")

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Settings().itemGroup(ItemGroup.REDSTONE)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
  }

  private fun createDisk(path: String): ImageDiskItem {
    return create(ImageDiskItem(Identifier(ModID, path)), "disk_$path")
  }

}

object Packets {

  object Client {
  }

  object Server {
    val TerminalKeyTyped = Identifier(ModID, "terminal_key")
  }

  init {
    ServerSidePacketRegistry.INSTANCE.register(Server.TerminalKeyTyped, ::onKeyTypedTerminal)
  }

}