package therealfarfetchd.retrocomputers.common

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.world.storage.loot.LootEntryTable
import net.minecraft.world.storage.loot.LootTableList
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import therealfarfetchd.quacklib.common.QGuiHandler
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.common.api.block.capability.IBusConnectable
import therealfarfetchd.retrocomputers.common.api.component.ComponentRegistry
import therealfarfetchd.retrocomputers.common.block.Rack
import therealfarfetchd.retrocomputers.common.block.RackExt
import therealfarfetchd.retrocomputers.common.component.ComponentCPU
import therealfarfetchd.retrocomputers.common.component.ComponentDummy
import therealfarfetchd.retrocomputers.common.net.*

/**
 * Created by marco on 25.06.17.
 */
open class Proxy {
  open fun preInit(e: FMLPreInitializationEvent) {
    for (s in magic) RetroComputers.Logger.warn(s)
    MinecraftForge.EVENT_BUS.register(this)
    CapabilityManager.INSTANCE.register(IBusConnectable::class.java, object : Capability.IStorage<IBusConnectable> {
      override fun readNBT(capability: Capability<IBusConnectable>?, instance: IBusConnectable?, side: EnumFacing?, nbt: NBTBase?) {}
      override fun writeNBT(capability: Capability<IBusConnectable>?, instance: IBusConnectable?, side: EnumFacing?): NBTBase? = null
    }, { null })

    RetroComputers.Net.registerMessage(PacketChangeBusID.Handler, PacketChangeBusID::class.java, 0, Side.SERVER)
    RetroComputers.Net.registerMessage(PacketComputerAction.Handler, PacketComputerAction::class.java, 1, Side.SERVER)
    RetroComputers.Net.registerMessage(PacketTerminalAction.Handler, PacketTerminalAction::class.java, 2, Side.SERVER)
    RetroComputers.Net.registerMessage(PacketComponentUpdate.Handler, PacketComponentUpdate::class.java, 3, Side.CLIENT)
    RetroComputers.Net.registerMessage(PacketComponentChange.Handler, PacketComponentChange::class.java, 4, Side.CLIENT)
    RetroComputers.Net.registerMessage(PacketComponentClick.Handler, PacketComponentClick::class.java, 5, Side.SERVER)

    QGuiHandler.registerClientGui(ResourceLocation(ModID, "computer"))
    QGuiHandler.registerClientGui(ResourceLocation(ModID, "terminal"))

    with(DefaultFeatures) {
      FeatureManager.depend(MCMultipartCompat, BlueAlloy, RedAlloy, LumarOrange, SiliconWaferBlue,
        SiliconWaferRed, Brass, Motor, CopperWire)
    }

    GameRegistry.registerTileEntity(Rack.Tile::class.java, Rack.Type.toString())
    GameRegistry.registerTileEntity(RackExt.Tile::class.java, RackExt.Type.toString())

    ComponentRegistry.register(ResourceLocation(ModID, "dummy"), ComponentDummy::class)
    ComponentRegistry.register(ResourceLocation(ModID, "cpu"), ComponentCPU::class)
  }

  open fun init(e: FMLInitializationEvent) {
    LootTableList.register(ResourceLocation(ModID, "disks"))
  }

  open fun postInit(e: FMLPostInitializationEvent) {}

  @SubscribeEvent
  fun registerItems(e: RegistryEvent.Register<Item>) {
    e.registry.register(RetroComputers.disks)
    e.registry.register(Rack.Item)
  }

  @SubscribeEvent
  fun registerBlocks(e: RegistryEvent.Register<Block>) {
    e.registry.register(Rack.Block)
    e.registry.register(RackExt.Block)
  }

  @SubscribeEvent
  fun loadLootTables(e: LootTableLoadEvent) {
    if (e.name.toString() in listOf("minecraft:chests/simple_dungeon", "minecraft:chests/abandoned_mineshaft")) {
      val entry = LootEntryTable(ResourceLocation(ModID, "disks"), 100, 1, emptyArray(), ModID)
      e.table.getPool("main").addEntry(entry)
    }
  }

  private val magic = SuperCompressor.decompress("EBLi0tLS0tLS0uCnwgIF8gICBfIF9fXyAgICAuICAgX19ffAp8IHxfKSB8fCAgfF8pIHwgfCB8ICB8" +
                                                 "fFwvXykgXyB8KF8gIHwKIFwgfF8gXCB8X3wgfF98fCBcIExvYWRpbmcuLi4gCictJwo=|AAQBACADA" +
                                                 "EAFAGBHAEBFBIAFBJAKBFAJAHALAMANAHAOAPAQARASAQATARAUARBSAVAUAWAXARAYAZBQAaAbAZA" +
                                                 "cBQAOAMBOAZAdAFANADRJALGJAeAfAgAhGJAXAOQJASAiRBAj").lines()
}