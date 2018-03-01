package therealfarfetchd.tubes.common

import net.minecraft.client.gui.inventory.GuiDispenser
import net.minecraft.inventory.ContainerDispenser
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import therealfarfetchd.quacklib.common.QGuiHandler
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.Feature
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.quacklib.common.api.qblock.IQBlockInventory
import therealfarfetchd.tubes.ModID

open class Proxy {
  open fun preInit(e: FMLPreInitializationEvent) {
    MinecraftForge.EVENT_BUS.register(this)
    FeatureManager.depend(Feature("itemtubes") { provides(DefaultFeatures.VirtualItemTubes) })
    FeatureManager.depend(DefaultFeatures.Brass)
    QGuiHandler.registerClientGui(ResourceLocation(ModID, "deployer")) { _, qb, p -> GuiDispenser(p.inventory, qb as IQBlockInventory) }
    QGuiHandler.registerServerGui(ResourceLocation(ModID, "deployer")) { _, qb, p -> ContainerDispenser(p.inventory, qb as IQBlockInventory) }
  }

  open fun init(e: FMLInitializationEvent) {}

  open fun postInit(e: FMLPostInitializationEvent) {}
}