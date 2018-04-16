package therealfarfetchd.retrocomputers.client.api.component

import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import therealfarfetchd.retrocomputers.common.api.component.Component

@SideOnly(Side.CLIENT)
interface ComponentRender<in T : Component> {
  fun render(component: T, partialTicks:Float)
}