package therealfarfetchd.powerline.common.jei

import mezz.jei.api.IModPlugin
import mezz.jei.api.IModRegistry
import mezz.jei.api.JEIPlugin
import therealfarfetchd.powerline.client.gui.GuiBlueAlloyFurnace
import therealfarfetchd.powerline.common.block.BlueAlloyFurnace
import therealfarfetchd.powerline.common.block.ContainerBlueAlloyFurnace
import therealfarfetchd.quacklib.common.api.extensions.makeStack
import therealfarfetchd.quacklib.common.jei.RecipeCategoryAlloy

@JEIPlugin
class JEIPlugin : IModPlugin {
  override fun register(registry: IModRegistry) {
    registry.addRecipeCatalyst(BlueAlloyFurnace.Item.makeStack(), RecipeCategoryAlloy.Instance.uid)

    registry.recipeTransferRegistry.addRecipeTransferHandler(ContainerBlueAlloyFurnace::class.java,
      RecipeCategoryAlloy.Instance.uid, 1, 9, 10, 36)
    registry.addRecipeClickArea(GuiBlueAlloyFurnace::class.java, 102, 34, 22, 16, RecipeCategoryAlloy.Instance.uid)
  }
}