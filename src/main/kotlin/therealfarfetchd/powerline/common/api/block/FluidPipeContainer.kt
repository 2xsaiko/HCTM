package therealfarfetchd.powerline.common.api.block

import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import therealfarfetchd.quacklib.common.api.INeighborSupport
import therealfarfetchd.quacklib.common.api.extensions.mapWithCopy
import therealfarfetchd.quacklib.common.api.util.QNBTCompound

class FluidPipeContainer(val ns: INeighborSupport<FluidPipeContainer>) {

  var fluid: Fluid? = null

  var amount: Int = 0
  var acqAmount: Int = 0
  val totalAmount: Int
    get() = amount + acqAmount

  var capacity: Int = 500

  var pressure: Double = 0.0

  fun update() {
    val neighbors = EnumFacing.VALUES.mapWithCopy(ns::getNeighborAt).filter { it.second != null }.map { it.first to it.second!! }.toMap()

    val pressureWants = neighbors.mapValues { (_, it) -> (pressure - it.pressure) / (2 * neighbors.size) }

    amount += acqAmount
    acqAmount = 0

    if (amount == 0) {
      fluid = null
    } else if (fluid == null) {
      amount = 0
      acqAmount = 0
    }

    for ((f, n) in neighbors) {
      val d = pressureWants.getValue(f)
      n.pressure += d
      pressure -= d

      val fluid = this.fluid ?: n.fluid
      if (fluid != null && this.fluid ?: fluid == n.fluid ?: fluid) {
        val transferAmtMax = (d * 10).toInt()
        val transferAmount = maxOf(-1, minOf(transferAmtMax, 1)) * minOf(Math.abs(n.accept(transferAmtMax, fluid, simulate = true)), Math.abs(accept(-transferAmtMax, fluid, simulate = true)))
        if (transferAmount != 0) {
          n.accept(transferAmount, fluid, applyPressure = false)
          accept(-transferAmount, fluid, applyPressure = false)
        }
      }
    }
  }

  fun canAccept(amt: Int, fluidIn: Fluid): Boolean {
    if (fluid == null) {
      amount = 0
      acqAmount = 0
    } else if (fluidIn != fluid) return false
    return totalAmount + amt in 0..capacity
  }

  fun accept(amt: Int, fluidIn: Fluid, applyPressure: Boolean = true, simulate: Boolean = false): Int {
    if (fluid == null) {
      amount = 0
      acqAmount = 0
    } else if (fluidIn != fluid) return 0
    val accepted = maxOf(-totalAmount, minOf(amt, capacity - totalAmount))
    if (!simulate && accepted != 0) {
      acqAmount += accepted
      if (fluid == null) fluid = fluidIn
      if (applyPressure) pressure += accepted / 10.0
    }
    return accepted
  }

  fun save(nbt: QNBTCompound) {
    if (fluid != null) nbt.string["Fluid"] = FluidRegistry.getFluidName(fluid)
    nbt.int["Amount"] = amount
    nbt.int["AcqAmt"] = acqAmount
    nbt.double["Pressure"] = pressure
  }

  fun load(nbt: QNBTCompound) {
    if ("Fluid" in nbt) fluid = FluidRegistry.getFluid(nbt.string["Fluid"])
    amount = nbt.int["Amount"]
    acqAmount = nbt.int["AcqAmt"]
    pressure = nbt.double["Pressure"]
  }

}