package therealfarfetchd.retrocomputers.common.component

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.PacketBuffer
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import therealfarfetchd.quacklib.common.api.extensions.*
import therealfarfetchd.quacklib.common.api.util.QNBTCompound
import therealfarfetchd.quacklib.common.api.util.math.Vec2
import therealfarfetchd.retrocomputers.client.render.component.RenderCPU
import therealfarfetchd.retrocomputers.common.api.block.BusDataContainer
import therealfarfetchd.retrocomputers.common.api.component.*
import therealfarfetchd.retrocomputers.common.cpu.BusController
import therealfarfetchd.retrocomputers.common.cpu.Processorv2
import therealfarfetchd.retrocomputers.common.item.ItemCPU
import kotlin.experimental.and
import kotlin.experimental.xor

class ComponentCPU : Component, ComponentBusAware {
  override val busId: Byte = 0

  // TODO
  override fun poke(addr: Byte, b: Byte) {}

  // TODO
  override fun peek(addr: Byte): Byte = 0

  override lateinit var container: ComponentContainer

  val controller: BusController = BusControllerImpl()

  var cpu: Processorv2? = null

  val updateInput = 1
  val updateAddress = 2
  val updateOutput = 4
  val updateMisc = 8

  var inputRegister: Short = 0
  var buffer: Short = 0
  var outputSelect = 0
  var powerOn = false

  // clientside stuff
  var btnAddrLoad = 0
  var btnClear = 0
  var btnContinue = 0
  var btnExamine = 0
  var btnHalt = 0
  var btnSingleStep = 0
  var btnDep = 0
  var btnExtdDep = 0
  var running = false

  val memAddress = FloatArray(16)
  val outputRegister = FloatArray(16)
  val prevMemAddress = FloatArray(16)
  val prevOutputRegister = FloatArray(16)

  override fun update() {
    if (container.world.isClient) {
      if (btnAddrLoad > 0) btnAddrLoad--
      if (btnClear > 0) btnClear--
      if (btnContinue > 0) btnContinue--
      if (btnExamine > 0) btnExamine--
      if (btnHalt > 0) btnHalt--
      if (btnSingleStep > 0) btnSingleStep--
      if (btnDep > 0) btnDep--
      if (btnExtdDep > 0) btnExtdDep--

      memAddress.copyTo(prevMemAddress)
      outputRegister.copyTo(prevOutputRegister)
    } else {
      cpu?.also { cpu ->
        cpu.timeout = false
        controller.resetBusState()

        val lightCounter = IntArray(32)

        val speed = 100000
        val cyclesPerTick = speed / 20
        var counter = 0
        for (i in 0 until cyclesPerTick) {
          if (!cpu.isRunning) break
          if (cpu.timeout) break
          cpu.next()
          unpack(cpu.pc.toShort()).forEachIndexed { index, b -> if (b) lightCounter[index]++ }
          unpack(controls.getOutputDisplay()).forEachIndexed { index, b -> if (b) lightCounter[index + 16]++ }
          counter++
        }

        unpack(cpu.pc.toShort()).forEachIndexed { index, b -> if (b) lightCounter[index] += cyclesPerTick - counter }
        unpack(controls.getOutputDisplay()).forEachIndexed { index, b -> if (b) lightCounter[index + 16] += cyclesPerTick - counter }

        container.markDirty()

        sendPacket {
          writeByte(updateOutput or updateAddress)
          lightCounter.forEach { writeFloat(it / cyclesPerTick.toFloat()) }
        }
        controls.updateMisc()
      }
    }
  }

  override fun onClicked(box: Int, player: EntityPlayer, hand: EnumHand): Boolean {
    if (container.world.isClient) return box in 0 until 31
    when (box) {
      in 0 until 16  -> controls.toggleInputSwitch(box)
      16             -> controls.togglePower()
      17             -> controls.loadAddress()
      18             -> controls.clear()
      19             -> controls.run()
      20             -> controls.examine()
      21             -> controls.halt()
      22             -> controls.step()
      23             -> controls.dep8()
      24             -> controls.dep16()
      in 25 until 31 -> controls.output(box - 25)
      else           -> return false
    }
    return true
  }

  private val controls = Controls()

  private inner class Controls {
    fun toggleInputSwitch(s: Int) {
      inputRegister = inputRegister xor (65536 shr (s+1)).toShort()
      updateInput()
    }

    fun togglePower() {
      powerOn = !powerOn
      cpu = if (powerOn) Processorv2(this@ComponentCPU, controller) else null
      sendPacket(::writeAll)
    }

    fun loadAddress() {
      cpu?.pc = inputRegister.toInt()
      updateMisc(1)
    }

    fun clear() {
      cpu?.reset()
      updateMisc(2)
    }

    fun run() {
      cpu?.isRunning = true
      updateMisc(4)
    }

    fun examine() {
      cpu?.also { cpu ->
        buffer = cpu.peek2(cpu.pc).toShort()
        cpu.pc++
      }
      updateMisc(8)
    }

    fun halt() {
      cpu?.isRunning = false
      updateMisc(16)
    }

    fun step() {
      cpu?.next()
      updateMisc(32)
    }

    fun dep8() {
      cpu?.also { cpu ->
        cpu.poke1(cpu.pc, inputRegister.toInt())
        cpu.pc++
      }
      updateMisc(64)
    }

    fun dep16() {
      cpu?.also { cpu ->
        cpu.poke2(cpu.pc, inputRegister.toInt())
        cpu.pc += 2
      }
      updateMisc(128)
    }

    fun output(i: Int) {
      outputSelect = i
      updateMisc()
    }

    fun getOutputDisplay(): Short = if (!powerOn) 0 else when (outputSelect) {
      0    -> getProcessorStateFlags()
      1    -> cpu!!.rA.toShort()
      2    -> cpu!!.rX.toShort()
      3    -> cpu!!.rY.toShort()
      4    -> buffer
      5    -> controller.targetBus.toShort() and 0xFF
      else -> 0
    }

    fun getProcessorStateFlags(): Short =
      ((packShort(*cpu!!.flags)) +
       (if (controller.isBusConnected) 512 else 0) +
       (if (controller.busFailed) 1024 else 0) +
       (if (!cpu!!.busEnabled) 2048 else 0) +
       (if (!controller.allowWrite) 4096 else 0) +
       (if (cpu!!.wait) 8192 else 0) +
       (if (cpu!!.stop) 16384 else 0) +
       (if (cpu!!.error) 32768 else 0)).toShort()

    fun updateMisc(extra: Int = 0) {
      sendPacket {
        writeByte(updateMisc)
        writeShort((outputSelect and 7) or (if (powerOn) 8 else 0) or (if (cpu?.isRunning == true) 16 else 0) or (extra shl 5))
      }
    }

    fun updateInput() {
      sendPacket {
        writeByte(updateInput)
        writeShort(inputRegister)
      }
    }
  }

  override fun getBoundingBoxes(): List<AxisAlignedBB> =
    getElements() +
    listOf(AxisAlignedBB(0.0, 0.0, 0.375 / 16f, 0.75, 0.375, 1.0))

  private fun getElements(): List<AxisAlignedBB> {
    val pxSize = 0.75 / 960
    fun tex2coord(x: Int, y: Int) = Vec2(0.75 - pxSize * x, 0.375 - pxSize * y)
    fun box(x: Int, y: Int, x1: Int, y1: Int): Pair<Vec2, Vec2> = Pair(tex2coord(x, y), tex2coord(x1, y1))

    val switches =
      ((0 until 16).map { 121 + 29 * it } + // input register
       65 +                                 // power button
       612 +                                // address load
       (0 until 5).map { 667 + 29 * it } +  // run control
       (0 until 2).map { 839 + 29 * it }    // deposit
      ).map { box(it, 395, it + 29, 444) }

    val buttons =
      (0 until 6)
        .map { 250 + 16 * it }
        .map { box(626, it, 639, it + 13) }

    return switches.map { (a, b) -> AxisAlignedBB(a.x, a.y, 0.0, b.x, b.y, 0.375 / 16f) } +
           buttons.map { (a, b) -> AxisAlignedBB(a.x, a.y, 0.3 / 16f, b.x, b.y, 0.375 / 16f) }
  }

  override fun saveData(nbt: QNBTCompound) {
    nbt.short["input"] = inputRegister
    nbt.short["buffer"] = buffer
    nbt.ubyte["outsel"] = outputSelect
    nbt.bool["power"] = powerOn
    nbt.byte["bus"] = controller.targetBus
    nbt.bool["write"] = controller.allowWrite
    nbt.short["write_pos"] = controller.writePos
    if (powerOn) cpu!!.saveData(nbt.nbt["cpu"])
  }

  override fun loadData(nbt: QNBTCompound) {
    inputRegister = nbt.short["input"]
    buffer = nbt.short["buffer"]
    outputSelect = nbt.ubyte["outsel"]
    powerOn = nbt.bool["power"]
    controller.targetBus = nbt.byte["bus"]
    controller.allowWrite = nbt.bool["write"]
    controller.writePos = nbt.short["write_pos"]
    cpu = if (powerOn) Processorv2(this, controller).also { it.loadData(nbt.nbt["cpu"]) } else null
  }

  override fun sendDataToClient(context: PacketSendContext) {
    context.sendPacket(::writeAll)
  }

  private fun writeAll(pb: PacketBuffer) {
    pb.writeByte(-1)
    pb.writeShort(inputRegister)
    pb.writeFloatArray(memAddress)
    pb.writeFloatArray(outputRegister)
    pb.writeShort(outputSelect or if (powerOn) 0b1000 else 0)
  }

  override fun readClientData(buf: PacketBuffer) {
    val updates = buf.readByte().toInt() and 0xFF
    if (updateInput and updates != 0) inputRegister = buf.readShort()
    if (updateAddress and updates != 0) buf.readFloatArray(memAddress, 16)
    if (updateOutput and updates != 0) buf.readFloatArray(outputRegister, 16)
    if (updateMisc and updates != 0) {
      val data = buf.readShort().toInt() and 0xFFFF
      outputSelect = data and 0b111
      powerOn = data and 0b1000 != 0
      running = data and 0b10000 != 0
      if (data and 0b0000000100000 != 0) btnAddrLoad = 5
      if (data and 0b0000001000000 != 0) btnClear = 5
      if (data and 0b0000010000000 != 0) btnContinue = 5
      if (data and 0b0000100000000 != 0) btnExamine = 5
      if (data and 0b0001000000000 != 0) btnHalt = 5
      if (data and 0b0010000000000 != 0) btnSingleStep = 5
      if (data and 0b0100000000000 != 0) btnDep = 5
      if (data and 0b1000000000000 != 0) btnExtdDep = 5
    }
  }

  override fun getItem() = ItemCPU.makeStack()

  override fun getRenderer() = RenderCPU

  private inner class BusControllerImpl : BusController {
    var bus: BusDataContainer? = null

    override var targetBus: Byte = 0

    override var busFailed: Boolean = false

    override val isBusConnected: Boolean
      get() = bus != null

    override fun bus(): BusDataContainer? {
      return if (!busFailed) {
        if (bus == null)
          bus = container.resolveNetwork().mapNotNull { it as? BusDataContainer }.find { it.canAccess(targetBus) }
        if (bus == null)
          busFailed = true
        bus
      } else null
    }

    override fun resetBusState() {
      bus = null
      busFailed = false
    }

    override var allowWrite: Boolean = false
    override var writePos: Short = 0
  }
}

fun PacketBuffer.writeByte(b: Byte) = writeByte(b.toInt())
fun PacketBuffer.writeShort(s: Short) = writeShort(s.toInt())

fun PacketBuffer.writeFloatArray(a: FloatArray) = a.forEach { writeFloat(it) }
fun PacketBuffer.readFloatArray(a: FloatArray, size: Int) = (0 until size).forEach { i -> a[i] = readFloat() }