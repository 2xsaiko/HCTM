package therealfarfetchd.retrocomputers.client.keybind

import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.input.Keyboard
import therealfarfetchd.quacklib.common.api.util.AutoLoad
import therealfarfetchd.retrocomputers.ModID

@AutoLoad
@SideOnly(Side.CLIENT)
object Keybindings {
  val ComponentView = KeyBinding("$ModID:component_view", KeyConflictContext.IN_GAME, Keyboard.KEY_R, ModID)

  init {
    ClientRegistry.registerKeyBinding(ComponentView)
  }
}