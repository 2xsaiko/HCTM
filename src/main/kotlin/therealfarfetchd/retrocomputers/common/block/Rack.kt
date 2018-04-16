package therealfarfetchd.retrocomputers.common.block

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByteArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.ITickable
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import therealfarfetchd.quacklib.common.api.block.capability.Capabilities
import therealfarfetchd.quacklib.common.api.extensions.*
import therealfarfetchd.quacklib.common.api.neighborSupport
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.util.Scheduler
import therealfarfetchd.quacklib.common.api.util.math.Mat4
import therealfarfetchd.quacklib.common.api.util.math.Vec3
import therealfarfetchd.quacklib.common.api.util.math.times
import therealfarfetchd.quacklib.common.api.wires.ConnectionResolverTile
import therealfarfetchd.quacklib.common.api.wires.TileConnectable
import therealfarfetchd.quacklib.common.item.ItemWrench
import therealfarfetchd.retrocomputers.ModID
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.RetroComputers.Logger
import therealfarfetchd.retrocomputers.common.api.block.capability.SimpleBusConnectable
import therealfarfetchd.retrocomputers.common.api.component.Component
import therealfarfetchd.retrocomputers.common.api.component.ComponentContainer
import therealfarfetchd.retrocomputers.common.api.component.ComponentRegistry
import therealfarfetchd.retrocomputers.common.api.component.PacketSendContext
import therealfarfetchd.retrocomputers.common.block.capability.RackBusDataContainer
import therealfarfetchd.retrocomputers.common.component.ComponentDummy
import therealfarfetchd.retrocomputers.common.net.PacketComponentChange
import therealfarfetchd.retrocomputers.common.net.PacketComponentClick
import therealfarfetchd.retrocomputers.common.net.PacketComponentUpdate
import therealfarfetchd.retrocomputers.common.util.canPlaceAt
import therealfarfetchd.retrocomputers.common.util.checkIntegrity
import therealfarfetchd.retrocomputers.common.util.getRackPos
import therealfarfetchd.retrocomputers.common.util.placeRackExt
import kotlin.math.ceil
import net.minecraft.block.Block as MCBlock
import net.minecraft.tileentity.TileEntity as MCTile

object Rack {
  val Type = ResourceLocation(ModID, "rack")

  class Tile : MCTile(), ITickable, TileConnectable {
    val cr = ConnectionResolverTile(this)
    val data = RackBusDataContainer(this, neighborSupport())
    val conn = SimpleBusConnectable(data) // for now

    val slotCount = 6
    val height = (5 + 7 * slotCount) / 16f
    val heightBlocks = ceil(height).toInt()

    val container = ContainerImpl(this)

    val facing: EnumFacing
      get() = EnumFacing.HORIZONTALS[blockMetadata]

    override fun update() {
      container.getComponents().forEach(Component::update)
      container.update()
    }

    fun clickBox(player: EntityPlayer, c: Int, bb: Int, hand: EnumHand): Boolean {
      return if (bb == -1) {
        container.extractComponent(c)
        true
      } else {
        container.getComponent(c).onClicked(bb, player, hand)
      }
    }

    fun clickBoxClient(c: Int, bb: Int, hand: EnumHand) {
      val packet = PacketComponentClick(pos, c, bb, hand)
      RetroComputers.Net.sendToServer(packet)
    }

    override fun getRenderBoundingBox(): AxisAlignedBB {
      return MCBlock.FULL_BLOCK_AABB.expand(0.0, height - 1.0, 0.0) + pos
    }

    override fun writeToNBT(tag: NBTTagCompound): NBTTagCompound {
      super.writeToNBT(tag)
      val nbt = QNBTCompound(tag)
      container.saveData(nbt)
      nbt.bytes["C"] = cr.serializeConnections().toByteArray()
      return tag
    }

    override fun readFromNBT(tag: NBTTagCompound) {
      super.readFromNBT(tag)
      val nbt = QNBTCompound(tag)
      container.loadData(nbt)
      cr.deserializeConnections(nbt.bytes["C"].toList())
    }

    override fun getUpdateTag(): NBTTagCompound {
      val nbt = super.getUpdateTag()
      val types = NBTTagList()
      (0 until slotCount)
        .map(container::getComponent)
        .map(ComponentRegistry::getType)
        .map { it?.toString() ?: "null" }
        .forEach { types.appendTag(NBTTagString(it)) }
      nbt.setTag("types", types)

      val data = NBTTagList()
      (0 until slotCount)
        .map(container::getComponent)
        .map {
          val packets = NBTTagList()
          it.sendDataToClient(PacketSendContext { packets.appendTag(NBTTagByteArray(it.arrayPart().toByteArray())) })
          packets
        }
        .forEach(data::appendTag)
      nbt.setTag("data", data)

      return nbt
    }

    override fun handleUpdateTag(nbt: NBTTagCompound) {
      super.readFromNBT(nbt)
      val types = nbt.getTagList("types", 8)
      (0 until slotCount).forEach { container.setComponent(it, ComponentRegistry.create(ResourceLocation(types.getStringTagAt(it)))) }

      val data = nbt.getTagList("data", 9)
      (0 until slotCount)
        .forEach {
          val component = container.getComponent(it)
          val packets = data[it] as NBTTagList
          (0 until packets.tagCount())
            .forEach {
              val packet = PacketBuffer(Unpooled.wrappedBuffer((packets[it] as NBTTagByteArray).byteArray))
              component.readClientData(packet)
            }
        }
    }

    override fun getConnectionResolver() = cr

    override fun getTile() = this

    override fun getWorldForScan() = getWorld()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
      return when (capability) {
        TileConnectable.Capability -> this as T
        Capabilities.Connectable   -> conn as T
        else                       -> super.getCapability(capability, facing)
      }
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
      getCapability(capability, facing) != null
  }

  @Suppress("OverridingDeprecatedMember")
  object Block : MCBlock(Material.IRON), ITileEntityProvider {
    init {
      registryName = Type
      unlocalizedName = Type.toString()
      setHarvestLevel("pickaxe", 2)
      setHardness(1.0f)
    }

    override fun rotateBlock(world: World?, pos: BlockPos?, axis: EnumFacing?): Boolean {
      return false // this is buggy for whatever reason
    }

    override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState?, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
      if (world.isServer) return true // TODO
      val mc = Minecraft.getMinecraft()
      val raytrace = mc.objectMouseOver
      if (raytrace.hitInfo == null) return false

      val rackPos = getRackPos(world, pos) ?: return false
      val te = world.getTileEntity(rackPos) as Rack.Tile

      var (slotIndex, bbIndex) = raytrace.hitInfo as? Pair<*, *> ?: return false
      slotIndex as? Int ?: return false
      bbIndex as? Int ?: return false

      if (player.getHeldItem(hand).item == ItemWrench) {
        bbIndex = -1
      }

      te.clickBoxClient(slotIndex, bbIndex, hand)

      return te.clickBox(player, slotIndex, bbIndex, hand)
    }

    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
      val te = world.getTileEntity(pos) as Tile
      (0 until te.slotCount).forEach { te.container.extractComponent(it) }
      super.breakBlock(world, pos, state)
      notifyWires()
    }

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
      super.onBlockAdded(worldIn, pos, state)
      Scheduler.schedule(0) {
        (worldIn.getTileEntity(pos) as? Tile)?.cr?.updateCableConnections()
      }
    }

    private fun notifyWires() {
      // TODO
    }

    override fun collisionRayTrace(blockState: IBlockState, world: World, pos: BlockPos, start: Vec3d, end: Vec3d): RayTraceResult? {
      val pos = getRackPos(world, pos) ?: return null
      val tile = world.getTileEntity(pos) as? Tile ?: return null

      var result: RayTraceResult? = null

      val containerOcclusionBoxes = Boxes.getContainerOcclusionBoxes(tile.slotCount, tile.facing)

      // test for the rack bounding box, but exclude the spots where the components are
      val collisionTestFrame = Boxes.getMainBoxes(tile.height, tile.facing)
        .mapNotNull { rayTrace(pos, start, end, it) }
        .minBy { it.hitVec.squareDistanceTo(start) }

      if (collisionTestFrame != null) {
        val range = collisionTestFrame.hitVec.squareDistanceTo(start)
        val occlusionTest = containerOcclusionBoxes
          .any { rayTrace(pos, start, end, it)?.let { it.hitVec.squareDistanceTo(start) <= range } ?: false }
        if (!occlusionTest) result = collisionTestFrame
      }

      // test for component-provided bounding boxes
      (0 until tile.slotCount)
        .mapNotNull { raytraceComponent(tile, start, end, it) }
        .minBy { it.hitVec.squareDistanceTo(start) }
        ?.takeIf { it.hitVec.squareDistanceTo(start) < result?.hitVec?.squareDistanceTo(start) ?: Double.POSITIVE_INFINITY }
        ?.also { result = it }

      return result
    }

    private fun raytraceComponent(tile: Tile, start: Vec3d, end: Vec3d, c: Int): RayTraceResult? {
      val boundingBoxes = tile.container.getComponent(c).getBoundingBoxes()
      val translate = Mat4.translateMat(-(2 / 16f), -(1 / 16f + 7 / 16f * c), 0f)
      val rotate = Mat4.rotationMat(0f, 1f, 0f, -tile.facing.horizontalAngle)
      val mat = translate * Mat4.translateMat(tile.pos.toVec3() + Vec3(0.5, 0.5, 0.5)) * rotate * Mat4.translateMat(-tile.pos.toVec3() - Vec3(0.5, 0.5, 0.5))

      val start1 = mat * start
      val end1 = mat * end

      val result = boundingBoxes
        .mapIndexedNotNull { index, bb -> raytraceComponentBB(tile, start1, end1, c, index, bb) }
        .minBy { it.hitVec.squareDistanceTo(start1) }

      return result?.let {
        RayTraceResult(mat.inverse * it.hitVec,
          (rotate.inverse * it.sideHit.directionVec.toVec3()).let { EnumFacing.getFacingFromVector(it.xf, it.yf, it.zf) },
          it.blockPos).apply { hitInfo = it.hitInfo }
      }
    }

    private fun raytraceComponentBB(tile: Tile, start: Vec3d, end: Vec3d, c: Int, bbi: Int, bb: AxisAlignedBB): RayTraceResult? {
      val r = rayTrace(tile.pos, start, end, bb)
      r?.hitInfo = Pair(c, bbi)
      return r
    }

    override fun canPlaceBlockAt(world: World, pos: BlockPos) =
      super.canPlaceBlockAt(world, pos) && canPlaceAt(world, pos, 6)

    override fun createBlockState() = BlockStateContainer(this, Property.Facing)

    override fun getStateForPlacement(world: World?, pos: BlockPos?, facing: EnumFacing?, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand?) =
      defaultState.withProperty(Property.Facing, placer.adjustedHorizontalFacing)

    override fun getStateFromMeta(meta: Int) = defaultState.withProperty(Property.Facing, EnumFacing.HORIZONTALS[meta])

    override fun getMetaFromState(state: IBlockState) = state.getValue(Property.Facing).horizontalIndex

    override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, blockIn: MCBlock?, fromPos: BlockPos?) {
      if (!checkIntegrity(world, pos)) {
        dropBlockAsItem(world, pos, state, 0)
        world.setBlockToAir(pos)
        return
      }
      (world.getTileEntity(pos) as? Tile)?.cr?.updateCableConnections()
    }

    override fun onNeighborChange(world: IBlockAccess, pos: BlockPos, neighbor: BlockPos) {
      super.onNeighborChange(world, pos, neighbor)
      (world.getTileEntity(pos) as? Tile)?.cr?.updateCableConnections()
    }

    override fun onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState?, placer: EntityLivingBase?, stack: ItemStack?) {
      placeRackExt(world, pos)
    }

    fun getCompleteBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB =
      (world.getTileEntity(pos) as? Tile)?.let { Boxes.getBoxApproximation(it.height) } ?: FULL_BLOCK_AABB

    override fun getBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos) =
      getCompleteBoundingBox(state, world, pos).intersect(FULL_BLOCK_AABB)

    override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos) =
      getCompleteBoundingBox(state, world, pos) + pos

    override fun isOpaqueCube(state: IBlockState?) = false

    override fun isFullCube(state: IBlockState?) = false

    override fun createNewTileEntity(worldIn: World?, meta: Int) = Tile()
  }

  class ContainerImpl(val rack: Tile) : ComponentContainer {
    override val world: World
      get() = rack.world

    override val pos: BlockPos
      get() = rack.pos

    override val facing: EnumFacing
      get() = EnumFacing.HORIZONTALS[rack.blockMetadata]

    override val slotCount: Int
      get() = rack.slotCount

    private val components = Array<Component>(rack.slotCount) { ComponentDummy().also { it.container = this } }

    var changedSlots: Set<Int> = emptySet()

    override fun setComponent(slot: Int, component: Component?) {
      val component = component ?: ComponentDummy()
      components[slot] = component
      component.container = this
      changedSlots += slot
      markDirty()
    }

    override fun getComponents() = components.asIterable()

    override fun extractComponent(slot: Int) {
      if (world.isClient) return
      components[slot].onExtract()
      setComponent(slot, null)
    }

    override fun getComponent(slot: Int) = components[slot]

    override fun getSlotId(component: Component) = components.indexOfFirst { it == component }.takeIf { it != -1 }

    override fun resolveNetwork() = rack.data.resolveNetwork()

    override fun markDirty() = rack.markDirty()

    fun update() {
      if (world.isServer) {
        for (slot in changedSlots) {
          val packet = PacketComponentChange(pos, slot, ComponentRegistry.getType(getComponent(slot))
                                                        ?: ResourceLocation("null"))
          RetroComputers.Net.sendToAllWatching(packet, world.provider.dimension, pos)
          val component = components[slot]
          component.sendDataToClient(PacketSendContext {
            RetroComputers.Net.sendToAllWatching(PacketComponentUpdate(pos, getSlotId(component)!!, it.arrayPart()),
              world.provider.dimension, pos)
          })
        }
      }
      changedSlots = emptySet()
    }

    fun saveData(nbt: QNBTCompound) {
      nbt.nbts["components"] = components.map {
        val cnbt = QNBTCompound()
        cnbt.string["type"] = ComponentRegistry.getType(it).toString()
        it.saveData(cnbt.nbt["data"])
        cnbt
      }
    }

    fun loadData(nbt: QNBTCompound) {
      nbt.nbts["components"].withIndex().forEach { (index, it) ->
        val c = ComponentRegistry.create(ResourceLocation(it.string["type"]))
        if (c != null) {
          c.loadData(it.nbt["data"])
          setComponent(index, c)
        } else {
          Logger.warn("Can't find component for type '${it.string["type"]}', ignoring")
        }
      }
    }
  }

  val Item = ItemBlock(Block).apply {
    registryName = Type
  }

  object Boxes {
    fun getBoxApproximation(height: Float) =
      MCBlock.FULL_BLOCK_AABB.setMaxY(height.toDouble())

    fun getMainBoxes(height: Float, facing: EnumFacing) = listOf(
      AxisAlignedBB(0.0, 0.0, 0.0, 1.0, height.toDouble(), 15 / 16.0),
      AxisAlignedBB(0.0, 0.0, 15 / 16.0, 1 / 16.0, height.toDouble(), 1.0),
      AxisAlignedBB(15 / 16.0, 0.0, 15 / 16.0, 1.0, height.toDouble(), 1.0),
      AxisAlignedBB(2 / 16.0, height - 4 / 16.0, 15 / 16.0, 14 / 16.0, height - 1 / 16.0, 1.0)
    ).map { it.rotateY(facing) }

    fun getContainerOcclusionBoxes(height: Int, facing: EnumFacing) = (0 until height)
      .map { AxisAlignedBB(2 / 16.0, 1 / 16.0 + 7 / 16.0 * it, 0.0, 14 / 16.0, 7 / 16.0 + 7 / 16.0 * it, 15 / 16.0) }
      .map { it.rotateY(facing) }
  }

  object Property {
    val Facing: PropertyEnum<EnumFacing> = PropertyEnum.create("facing", EnumFacing::class.java, *EnumFacing.HORIZONTALS)
  }

  fun ByteBuf.arrayPart() = array().slice(arrayOffset() until arrayOffset() + writerIndex())
}