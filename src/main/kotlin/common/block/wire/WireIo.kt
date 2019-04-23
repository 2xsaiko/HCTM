package therealfarfetchd.retrocomputers.common.block.wire

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.experimental.or

/**
 * Returns a wrapper for device IO.
 * Only valid for the current tick!
 */
fun accessIoNet(world: World, pos: BlockPos, ext: PartExt<out Any?>): IoNetwork? {
  if (world.isClient || world !is ServerWorld) return null

  val wns = world.getWireNetworkState()
  val node = wns.controller.getNodesAt(pos).singleOrNull { it.data.ext == ext } ?: return null
  val net = wns.controller.getNetwork(wns.controller.getNetIdForNode(node)) ?: return null
  return IoNetworkImpl(world, net)
}

interface PartIoProvider {

  fun isBusId(world: World, pos: BlockPos, busId: Byte): Boolean

  fun read(world: World, pos: BlockPos, at: Byte): Byte

  fun store(world: World, pos: BlockPos, at: Byte, data: Byte)

  /**
   * Use this if you want to send multiple operations for less overhead.
   * Only valid for the current tick!
   */
  fun cached(world: World, pos: BlockPos): Cached?

  interface Cached {
    fun read(at: Byte): Byte

    fun store(at: Byte, data: Byte)
  }

}

interface IoNetwork {

  /**
   * Returns a handler for one (or multiple, if there's multiple devices with the same bus id on the network)
   * Only valid for the current tick!
   */
  fun device(busId: Byte): Device

}

interface Device {

  fun read(at: Byte): Byte

  fun store(at: Byte, data: Byte)

}

private class IoNetworkImpl(val world: World, val network: Network) : IoNetwork {

  override fun device(busId: Byte): Device {
    val devs = network.getNodes().asSequence()
      .filter { it.data.ext is PartIoProvider && it.data.ext.isBusId(world, it.data.pos, busId) }
      .mapNotNull { (it.data.ext as PartIoProvider).cached(world, it.data.pos) }
      .toSet()

    return when (devs.size) {
      0 -> EmptyDeviceImpl
      1 -> SingleDeviceImpl(devs.single())
      else -> JoinedDeviceImpl(devs)
    }
  }

}

private object EmptyDeviceImpl : Device {

  override fun read(at: Byte): Byte {
    return 0
  }

  override fun store(at: Byte, data: Byte) {}

}

private class SingleDeviceImpl(val provider: PartIoProvider.Cached) : Device {

  override fun read(at: Byte): Byte {
    return provider.read(at)
  }

  override fun store(at: Byte, data: Byte) {
    provider.store(at, data)
  }

}

private class JoinedDeviceImpl(val providers: Set<PartIoProvider.Cached>) : Device {

  override fun read(at: Byte): Byte {
    return providers.fold(0.toByte()) { acc, p -> acc or p.read(at) }
  }

  override fun store(at: Byte, data: Byte) {
    for (provider in providers) {
      provider.store(at, data)
    }
  }

}