package therealfarfetchd.hctm.common.wire

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.reflect.KClass

object ConnectionHandlers {
  val Wire = connectionHandler<WirePartExtType, Direction, Constraints> {
    // wires in same block
    connectionRule {
      forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
      connect {
        findNode(pos,
          Constraint(data.cls),
          Constraint(WirePartExtType::class) { it.side == output }
        )
      }
    }

    // planar connections
    connectionRule {
      forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
      connect {
        findNode(pos.offset(output),
          Constraint(data.cls),
          Constraint(WirePartExtType::class) { it.side == self.data.ext.side }
        )
      }
    }

    // machine connections
    connectionRule {
      forOutputs { Direction.values().filter { it != self.data.ext.side.opposite } }
      connect {
        findNode(pos.offset(output),
          Constraint(data.cls),
          Constraint(FullBlockPartExtType::class)
        )
      }
    }

    // corner connections
    connectionRule {
      forOutputs { Direction.values().filter { it.axis != self.data.ext.side.axis } }
      connect {
        findNode(pos.offset(output).offset(self.data.ext.side),
          Constraint(data.cls),
          Constraint(WirePartExtType::class) { it.side == output.opposite }
        )
      }
    }
  }

  private data class Edge(val side: Direction, val edge: Direction)

  private val edges = Direction.values().flatMap { side -> Direction.values().filter { edge -> edge.axis != side.axis }.map { edge -> Edge(side, edge) } }

  val FullBlock = connectionHandler<FullBlockPartExtType, Edge, Constraints> {
    connectionRule {
      forOutputs { edges }
      connect {
        findNode(pos.offset(output.side),
          Constraint(data.cls),
          Constraint(WirePartExtType::class) { it.side == output.edge }
        )
      }
    }

    connectionRule {
      forOutputs { edges }
      connect {
        findNode(pos.offset(output.side),
          Constraint(data.cls),
          Constraint(FullBlockPartExtType::class)
        )
      }
    }
  }


  private data class Constraint<T : Any>(val cls: KClass<T>, val check: (T) -> Boolean = { true }) {
    fun matches(node: NetNode) = cls.isInstance(node.data.ext) && check(node.data.ext as T)
  }

  private fun <E : PartExt, T, D> ConnectScope<E, T, D>.findNode(at: BlockPos, vararg constraints: Constraint<*>): NetNode? {
    return nv.getNodes(at).firstOrNull { node -> constraints.all { c -> c.matches(node) } }
  }

}

data class Constraints(val cls: KClass<out PartExt>)