/*
 * Copyright (c) 2017 Marco Rebhan (the_real_farfetchd)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package therealfarfetchd.powerline.common.api

enum class DefaultConductorConfiguration(
  override val resistance: Double,
  override val decel: Double,
  override val voltageSpec: Double,
  override val powerThreshold: Double,
  override val capacitance: Double
) : IConductorConfiguration {
  // generic devices
  LVCable(0.005, 0.95, 100.0, 0.0, 1.0),
  LVDevice(0.005, 0.95, 100.0, 60.0, 1.0),

  HVCable(1.0, 0.95, 1000.0, 0.0, 1.0),
  HVDevice(1.0, 0.95, 1000.0, 600.0, 1.0),

  // specific devices
  BatteryBox(0.005, 0.95, 100.0, 90.0, 1.0)
}