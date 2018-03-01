package therealfarfetchd.retrocomputers.common

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
import net.minecraftforge.fml.relauncher.Side
import therealfarfetchd.quacklib.common.QGuiHandler
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.common.api.block.capability.IBusConnectable
import therealfarfetchd.retrocomputers.common.net.PacketChangeBusID
import therealfarfetchd.retrocomputers.common.net.PacketComputerAction
import therealfarfetchd.retrocomputers.common.net.PacketTerminalAction

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

    QGuiHandler.registerClientGui(ResourceLocation(ModID, "computer"))
    QGuiHandler.registerClientGui(ResourceLocation(ModID, "terminal"))

    with(DefaultFeatures) {
      FeatureManager.depend(MCMultipartCompat, BlueAlloy, RedAlloy, LumarOrange, SiliconWaferBlue,
        SiliconWaferRed, Brass, Motor, CopperWire)
    }
  }

  open fun init(e: FMLInitializationEvent) {
    LootTableList.register(ResourceLocation(ModID, "disks"))
  }

  open fun postInit(e: FMLPostInitializationEvent) {}

  @SubscribeEvent
  fun registerItems(e: RegistryEvent.Register<Item>) {
    e.registry.register(RetroComputers.disks)
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