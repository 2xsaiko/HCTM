package therealfarfetchd.retrocomputers.common.block.wire

import net.minecraft.util.math.Direction
import therealfarfetchd.retrocomputers.common.block.MachinePartExt
import therealfarfetchd.retrocomputers.common.block.WirePartExt

object ConnectionHandlers {
  val Wire = connectionHandler<WirePartExt, Direction> {
    // wires in same block
    connectionRule {
      forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
      connect { nv.getNodes(pos).firstOrNull { it.data.ext is WirePartExt && it.data.ext.side == output } }
    }

    // planar connections
    connectionRule {
      forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
      connect { nv.getNodes(pos.offset(output)).firstOrNull { it.data.ext is WirePartExt && it.data.ext.side == self.data.ext.side } }
    }

    // machine connections
    connectionRule {
      forOutputs { Direction.values().filter { it != self.data.ext.side.opposite } }
      connect { nv.getNodes(pos.offset(output)).firstOrNull { it.data.ext is MachinePartExt } }
    }

    // corner connections
    connectionRule {
      forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
      connect { nv.getNodes(pos.offset(output).offset(self.data.ext.side)).firstOrNull { it.data.ext is WirePartExt && it.data.ext.side == output.opposite } }
    }
  }

  private data class Edge(val side: Direction, val edge: Direction)

  private val edges = Direction.values().flatMap { side -> Direction.values().filter { edge -> edge.axis != side.axis }.map { edge -> Edge(side, edge) } }

  val StandardMachine = connectionHandler<MachinePartExt, Edge> {
    connectionRule {
      forOutputs { edges }
      connect { nv.getNodes(pos.offset(output.side)).firstOrNull { it.data.ext is WirePartExt && it.data.ext.side == output.edge } }
    }

    connectionRule {
      forOutputs { edges }
      connect { nv.getNodes(pos.offset(output.side)).firstOrNull { it.data.ext is MachinePartExt } }
    }
  }
}