package therealfarfetchd.retrocomputers.client.gui

import therealfarfetchd.quacklib.client.api.gui.AbstractGuiLogic
import therealfarfetchd.quacklib.client.api.gui.elements.Button
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.client.gui.elements.AddressBoard
import therealfarfetchd.retrocomputers.client.gui.elements.Light
import therealfarfetchd.retrocomputers.common.block.Computer
import therealfarfetchd.retrocomputers.common.net.PacketComputerAction

class ComputerLogic : AbstractGuiLogic() {

  val qb: Computer by params()

  val diskaddr: AddressBoard by component()
  val termaddr: AddressBoard by component()
  val run_button: Button by component()
  val reset_button: Button by component()
  val hreset_button: Button by component()
  val link_light: Light by component()
  val fail_light: Light by component()
  val cpu_light: Light by component()
  val timeout_light: Light by component()
  val error_light: Light by component()
  val reset_err: Button by component()

  override fun init() {
    diskaddr.action {
      val packet = PacketComputerAction(qb.world.provider.dimension, qb.pos, 0x04, diskaddr.value)
      RetroComputers.Net.sendToServer(packet)
    }
    termaddr.action {
      val packet = PacketComputerAction(qb.world.provider.dimension, qb.pos, 0x05, termaddr.value)
      RetroComputers.Net.sendToServer(packet)
    }
    run_button.action {
      qb.running = run_button.toggled
      val act: Byte = if (run_button.toggled) 0x01 else 0x00
      val packet = PacketComputerAction(qb.world.provider.dimension, qb.pos, act, 0)
      RetroComputers.Net.sendToServer(packet)
    }
    reset_button.action {
      val packet = PacketComputerAction(qb.world.provider.dimension, qb.pos, 0x02, 0)
      RetroComputers.Net.sendToServer(packet)
    }
    hreset_button.action {
      val packet = PacketComputerAction(qb.world.provider.dimension, qb.pos, 0x03, 0)
      RetroComputers.Net.sendToServer(packet)
    }
    reset_err.action {
      val packet = PacketComputerAction(qb.world.provider.dimension, qb.pos, 0x06, 0)
      RetroComputers.Net.sendToServer(packet)
    }
  }

  override fun update() {
    if (qb.container.isInvalid) close()
    else {
      diskaddr.value = qb.diskAddr
      termaddr.value = qb.termAddr
      run_button.toggled = qb.running
      link_light.value = qb.clientBCon
      fail_light.value = qb.clientBFai
      cpu_light.value = qb.clientCpu
      timeout_light.value = qb.clientTimeout
      error_light.value = qb.clientError
    }
  }
}