package therealfarfetchd.rswires.common

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.oredict.OreDictionary
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.Feature
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.rswires.common.block.BundledCable
import therealfarfetchd.rswires.common.block.InsulatedWire
import therealfarfetchd.rswires.common.util.NetworkPropagator

open class Proxy {
  open fun preInit(e: FMLPreInitializationEvent) {
    MinecraftForge.EVENT_BUS.register(this)
    FeatureManager.depend(Feature("bundled cable", { provides(DefaultFeatures.VirtualBundledCable) }))
    FeatureManager.depend(DefaultFeatures.MCMultipartCompat, DefaultFeatures.RedAlloy)
  }

  open fun init(e: FMLInitializationEvent) {
    (0..15)
      .map { InsulatedWire.Item.makeStack(meta = it) }
      .forEach { OreDictionary.registerOre("wireInsulated", it) }

    (0..16)
      .map { BundledCable.Item.makeStack(meta = it) }
      .forEach { OreDictionary.registerOre("wireBundled", it) }

    (1..16)
      .map { BundledCable.Item.makeStack(meta = it) }
      .forEach { OreDictionary.registerOre("wireBundledDyed", it) }
  }

  open fun postInit(e: FMLPostInitializationEvent) {}

  @SubscribeEvent
  fun onTick(e: TickEvent.WorldTickEvent) {
    NetworkPropagator.tickPropagation()
  }
}