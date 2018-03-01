package therealfarfetchd.powerline.client.model

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.*
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.common.property.IExtendedBlockState
import net.minecraftforge.fluids.Fluid
import therealfarfetchd.powerline.ModID
import therealfarfetchd.powerline.common.block.FluidPipe
import therealfarfetchd.quacklib.client.api.model.DynamicSimpleModel
import therealfarfetchd.quacklib.client.api.model.IIconRegister
import therealfarfetchd.quacklib.client.api.model.ModelBuilder
import therealfarfetchd.quacklib.common.api.wires.BlockWireCentered

object ModelFluidPipe : DynamicSimpleModel<FluidPipe>(), IIconRegister {
  lateinit var texture: TextureAtlasSprite

  override val particleTexture: TextureAtlasSprite
    get() = texture

  override fun addShapes(state: IExtendedBlockState, model: ModelBuilder) {
    var connections: Set<EnumFacing> = emptySet()

    if (state.getValue(BlockWireCentered.PropConnDown)) connections += DOWN
    if (state.getValue(BlockWireCentered.PropConnUp)) connections += UP
    if (state.getValue(BlockWireCentered.PropConnNorth)) connections += NORTH
    if (state.getValue(BlockWireCentered.PropConnSouth)) connections += SOUTH
    if (state.getValue(BlockWireCentered.PropConnWest)) connections += WEST
    if (state.getValue(BlockWireCentered.PropConnEast)) connections += EAST

    addShapes(connections, state.getValue(FluidPipe.PropJoints), model)
  }

  override fun addShapes(stack: ItemStack, model: ModelBuilder) {
    addShapes(setOf(UP, DOWN), setOf(UP, DOWN), model)
  }

  private fun addShapes(connections: Set<EnumFacing>, joints: Set<EnumFacing>, model: ModelBuilder) = model {
    box {
      min = vec16(6, 6, 6)
      max = vec16(10, 10, 10)

      cull = false

      val straight = connections.size == 2 && connections.map { it.axis }.toSet().size == 1

      val axis = connections.firstOrNull()?.axis

      val tex = if (straight)
        texture16(texture, 0, 6, 4, 10)
      else
        texture16(texture, 4, 0, 8, 4)

      if (DOWN !in connections) down = tex
      if (UP !in connections) up = tex
      if (NORTH !in connections) north = tex
      if (SOUTH !in connections) south = tex
      if (WEST !in connections) west = tex
      if (EAST !in connections) east = tex

      if (straight) {
        if (axis == Axis.X) {
          up = up?.copy(flip = true)
          down = down?.copy(flip = true)
          north = north?.copy(flip = true)
          south = south?.copy(flip = true)
        }
        if (axis == Axis.Z) {
          west = west?.copy(flip = true)
          east = east?.copy(flip = true)
        }
      }
    }

    for (c in connections) {
      val tr = when (c) {
        EnumFacing.DOWN -> ""
        EnumFacing.UP -> "|Y"
        EnumFacing.NORTH -> "°X…90|Z"
        EnumFacing.SOUTH -> "°X…90"
        EnumFacing.WEST -> "°Z…90|X"
        EnumFacing.EAST -> "°Z…90"
      }

      box {
        min = vec16(6, 0, 6)
        max = vec16(10, 6, 10)

        cull = false

        transform = tr

        val tex = texture16(texture, 0, 6, 4, 0)
        north = tex
        south = tex
        west = tex
        east = tex
      }

      if (c in joints) box {
        min = vec16(4, 0, 4)
        max = vec16(12, 2, 12)

        transform = tr

        up = texture16(texture, 6, 6, 14, 14)
        down = texture16(texture, 6, 6, 14, 14)
        north = texture16(texture, 6, 4, 14, 6)
        south = texture16(texture, 6, 14, 14, 16)
        west = texture16(texture, 4, 6, 6, 14, flip = true)
        east = texture16(texture, 14, 6, 16, 14, flip = true)
      }
    }
  }

  override fun addShapesDynamic(block: FluidPipe, model: ModelBuilder) = model {
    var connections: Set<EnumFacing> = emptySet()

    val state = block.applyProperties(block.container.blockType.defaultState)
    if (state.getValue(BlockWireCentered.PropConnDown)) connections += DOWN
    if (state.getValue(BlockWireCentered.PropConnUp)) connections += UP
    if (state.getValue(BlockWireCentered.PropConnNorth)) connections += NORTH
    if (state.getValue(BlockWireCentered.PropConnSouth)) connections += SOUTH
    if (state.getValue(BlockWireCentered.PropConnWest)) connections += WEST
    if (state.getValue(BlockWireCentered.PropConnEast)) connections += EAST

    val fluid = block.data.fluid
    if (fluid != null) {
      val tex = texture(getStillFluidSprite(fluid))

      val fullness = block.data.totalAmount / block.data.capacity.toDouble()
      if (connections.any { it.axis != Axis.Y }) box {
        min = vec16(6.01, 6.01, 6.01)
        max = vec16(9.99, fullness * 3.98 + 6.01, 9.99)

        up = tex
        down = tex
        if (NORTH !in connections) north = tex
        if (SOUTH !in connections) south = tex
        if (WEST !in connections) west = tex
        if (EAST !in connections) east = tex
      }

      connections.filter { it.axis != Axis.Y }
        .map { "°Y${it.opposite.horizontalAngle.toInt().toString().padStart(3, '…')}" }
        .forEach {
          box {
            transform = it

            min = vec16(6.01, 6.01, 0.0)
            max = vec16(9.99, block.data.totalAmount / block.data.capacity.toDouble() * 3.98 + 6.01, 6.01)

            up = tex
            down = tex
            east = tex
            west = tex
            north = tex
          }
        }

      if (connections.any { it.axis == Axis.Y }) {
        val radius = MathHelper.sqrt(fullness) * 1.99
        var minY = fullness * 3.98 + 6.01
        var maxY = 6.01
        if (UP in connections) maxY = 16.0
        if (DOWN in connections) minY = 0.0

        box {
          min = vec16(8 - radius, minY, 8 - radius)
          max = vec16(8 + radius, maxY, 8 + radius)

          north = tex
          south = tex
          east = tex
          west = tex
        }
      }
    }
  }

  override fun registerIcons(textureMap: TextureMap) {
    texture = textureMap.registerSprite(ResourceLocation(ModID, "blocks/fluid_pipe"))
  }

  override fun createKey(state: IExtendedBlockState, face: EnumFacing?): String {
    return super.createKey(state, face) + state.getValue(FluidPipe.PropJoints)?.map { 1 shl it.index }?.sum()
  }

  private fun getStillFluidSprite(fluid: Fluid): TextureAtlasSprite {
    val textureMapBlocks = Minecraft.getMinecraft().textureMapBlocks
    val fluidStill = fluid.still
    return fluidStill?.let { textureMapBlocks.getTextureExtry(it.toString()) } ?: textureMapBlocks.missingSprite
  }
}