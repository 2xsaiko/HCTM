package therealfarfetchd.retrocomputers.client.render.component

import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.util.ResourceLocation
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.client.api.component.ComponentRender
import therealfarfetchd.retrocomputers.client.objloader.OBJGLRenderer
import therealfarfetchd.retrocomputers.client.objloader.loadOBJ
import therealfarfetchd.retrocomputers.client.objloader.orEmpty
import therealfarfetchd.retrocomputers.common.component.ComponentDummy

object RenderDummy : ComponentRender<ComponentDummy> {
  val obj = OBJGLRenderer(loadOBJ(ResourceLocation(ModID, "models/component/dummyplate/dummyplate.obj")).orEmpty(),
    mapOf("Material" to "retrocomputers:textures/blocks/mainframe.png"))

  override fun render(component: ComponentDummy, partialTicks: Float) {
    color(1f, 1f, 1f, 1f)
    translate(0.5f, 0f, 0.5f)
    rotate(-90f, 0f, 1f, 0f)
    translate(-0.5f, 0f, -0.5f)
    obj.draw()
  }
}