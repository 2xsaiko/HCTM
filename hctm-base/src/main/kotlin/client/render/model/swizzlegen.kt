// package net.dblsaiko.hctm.client.render.model
//
// val scalar = Type("Float", "%s.toFloat()", "x")
// val vec2 = Type("Vector2fc", "vec2(%s, %s)", "xy")
// val vec3 = Type("Vector3fc", "vec3(%s, %s, %s)", "xyz")
// val vec4 = Type("Vector4fc", "vec4(%s, %s, %s, %s)", "xyzw")
//
// fun main() {
//   for ((from, into) in iterAll(listOf(scalar, vec2, vec3, vec4), 2)) {
//     gen(from, into)
//   }
// }
//
// fun gen(from: Type, into: Type) {
//   for (chars in iterAll(from.components.toList(), into.components.length)) {
//     genSingle(from, into, String(chars.toCharArray()))
//   }
// }
//
// fun genSingle(from: Type, into: Type, fields: String) {
//   println("val ${from.name}.$fields: ${into.name}\n  get() = ${String.format(into.make, *fields.toCharArray().map { "$it()" }.toTypedArray())}")
// }
//
// fun <T> iterAll(values: List<T>, len: Int): Sequence<List<T>> = sequence {
//   val indices = IntArray(len)
//   outer@ while (true) {
//     yield(List(len) { values[indices[it]] })
//     indices[0] += 1
//     for (i in indices.indices) {
//       if (indices[i] >= values.size) {
//         if (i == len - 1) break@outer
//         indices[i] = 0
//         indices[i + 1] += 1
//       }
//     }
//   }
// }
//
// data class Type(val name: String, val make: String, val components: String)