package therealfarfetchd.retrocomputers.client.objloader

import therealfarfetchd.quacklib.common.api.util.math.Vec3
import java.awt.Color

data class OBJRoot(
  val materials: Map<String, Material>,

  val vertPos: List<Vec3>,
  val vertTex: List<Vec3>,
  val vertNormal: List<Vec3>,

  // these don't belong to any object
  val faces: List<Face>,
  val objects: Map<String, Object>
)

data class Object(
  val groups: List<String>,
  val faces: List<Face>
)

data class Face(
  val material: String?,
  val vertices: List<Vertex>
)

data class Vertex(
  val xyz: Int,
  val tex: Int?,
  val normal: Int?
)

data class Material(
  val diffuse: Color,
  val transparency: Float,
  val diffuseTexture: String?
  // for now
)