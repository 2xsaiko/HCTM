package therealfarfetchd.powerline.common

import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.client.gui.GuiBatteryBox
import therealfarfetchd.powerline.client.gui.GuiBlueAlloyFurnace
import therealfarfetchd.powerline.common.block.BatteryBox
import therealfarfetchd.powerline.common.block.BlueAlloyFurnace
import therealfarfetchd.powerline.common.block.ContainerBatteryBox
import therealfarfetchd.powerline.common.block.ContainerBlueAlloyFurnace
import therealfarfetchd.quacklib.common.QGuiHandler
import therealfarfetchd.quacklib.common.api.autoconf.DefaultFeatures
import therealfarfetchd.quacklib.common.api.autoconf.Feature
import therealfarfetchd.quacklib.common.api.autoconf.FeatureManager
import therealfarfetchd.quacklib.common.api.qblock.IQBlockInventory

open class Proxy {
  open fun preInit(e: FMLPreInitializationEvent) {
    MinecraftForge.EVENT_BUS.register(this)
    FeatureManager.depend(Feature("power") { provides(DefaultFeatures.VirtualPower) })
    FeatureManager.depend(DefaultFeatures.Lumar, DefaultFeatures.MCMultipartCompat, DefaultFeatures.Brass,
      DefaultFeatures.SiliconWaferBlue, DefaultFeatures.SiliconWaferRed, DefaultFeatures.AlloyFurnace)

    QGuiHandler.registerClientGui(ResourceLocation(ModID, "blue_alloy_furnace")) { _, qb, player -> GuiBlueAlloyFurnace(player.inventory, qb as BlueAlloyFurnace) }
    QGuiHandler.registerServerGui(ResourceLocation(ModID, "blue_alloy_furnace")) { _, qb, player -> ContainerBlueAlloyFurnace(player.inventory, qb as IQBlockInventory) }
    QGuiHandler.registerClientGui(ResourceLocation(ModID, "battery_box")) { _, qb, player -> GuiBatteryBox(player.inventory, qb as BatteryBox) }
    QGuiHandler.registerServerGui(ResourceLocation(ModID, "battery_box")) { _, qb, player -> ContainerBatteryBox(player.inventory, qb as IQBlockInventory) }
  }

  open fun init(e: FMLInitializationEvent) {}

  open fun postInit(e: FMLPostInitializationEvent) {}
}