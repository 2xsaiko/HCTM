package therealfarfetchd.retrocomputers.common.api.cpu

import therealfarfetchd.quacklib.common.api.util.QNBTCompound

/**
 * Created by marco on 24.06.17.
 */
interface IProcessor {
  var mem: IMemoryProvider

  // Execute 1 CPU instruction.
  fun next()

  // Reset the computer.
  fun reset(hard: Boolean)

  fun saveData(tag: QNBTCompound)
  fun loadData(tag: QNBTCompound)

  // If this is set, wait until the next world tick to continue executing cycles.
  var timeout: Boolean

  // Enable the purely cosmetical red light in the computer GUI.
  var error: Boolean

  // How many cycles can be buffered. This should be equal to or greater than insnGain()
  val insnBufferSize: Int

  // How many cycles the CPU can execute in 1 world tick. Remaining cycles that build up (through timeouts) will be buffered up to insnBufferSize().
  val insnGain: Int
}