@file:Suppress("unused")

package therealfarfetchd.hctm.client.render.model

import org.joml.Vector2fc
import org.joml.Vector3fc
import org.joml.Vector4fc

val Float.x: Float
  inline get() = this
val Vector2fc.x: Float
  inline get() = x()
val Vector2fc.y: Float
  inline get() = y()
val Vector3fc.x: Float
  inline get() = x()
val Vector3fc.y: Float
  inline get() = y()
val Vector3fc.z: Float
  inline get() = z()
val Vector4fc.x: Float
  inline get() = x()
val Vector4fc.y: Float
  inline get() = y()
val Vector4fc.z: Float
  inline get() = z()
val Vector4fc.w: Float
  inline get() = w()
val Float.xx: Vector2fc
  inline get() = vec2(this, this)
val Vector2fc.xx: Vector2fc
  inline get() = vec2(x(), x())
val Vector2fc.yx: Vector2fc
  inline get() = vec2(y(), x())
val Vector2fc.xy: Vector2fc
  inline get() = vec2(x(), y())
val Vector2fc.yy: Vector2fc
  inline get() = vec2(y(), y())
val Vector3fc.xx: Vector2fc
  inline get() = vec2(x(), x())
val Vector3fc.yx: Vector2fc
  inline get() = vec2(y(), x())
val Vector3fc.zx: Vector2fc
  inline get() = vec2(z(), x())
val Vector3fc.xy: Vector2fc
  inline get() = vec2(x(), y())
val Vector3fc.yy: Vector2fc
  inline get() = vec2(y(), y())
val Vector3fc.zy: Vector2fc
  inline get() = vec2(z(), y())
val Vector3fc.xz: Vector2fc
  inline get() = vec2(x(), z())
val Vector3fc.yz: Vector2fc
  inline get() = vec2(y(), z())
val Vector3fc.zz: Vector2fc
  inline get() = vec2(z(), z())
val Vector4fc.xx: Vector2fc
  inline get() = vec2(x(), x())
val Vector4fc.yx: Vector2fc
  inline get() = vec2(y(), x())
val Vector4fc.zx: Vector2fc
  inline get() = vec2(z(), x())
val Vector4fc.wx: Vector2fc
  inline get() = vec2(w(), x())
val Vector4fc.xy: Vector2fc
  inline get() = vec2(x(), y())
val Vector4fc.yy: Vector2fc
  inline get() = vec2(y(), y())
val Vector4fc.zy: Vector2fc
  inline get() = vec2(z(), y())
val Vector4fc.wy: Vector2fc
  inline get() = vec2(w(), y())
val Vector4fc.xz: Vector2fc
  inline get() = vec2(x(), z())
val Vector4fc.yz: Vector2fc
  inline get() = vec2(y(), z())
val Vector4fc.zz: Vector2fc
  inline get() = vec2(z(), z())
val Vector4fc.wz: Vector2fc
  inline get() = vec2(w(), z())
val Vector4fc.xw: Vector2fc
  inline get() = vec2(x(), w())
val Vector4fc.yw: Vector2fc
  inline get() = vec2(y(), w())
val Vector4fc.zw: Vector2fc
  inline get() = vec2(z(), w())
val Vector4fc.ww: Vector2fc
  inline get() = vec2(w(), w())
val Float.xxx: Vector3fc
  inline get() = vec3(this, this, this)
val Vector2fc.xxx: Vector3fc
  inline get() = vec3(x(), x(), x())
val Vector2fc.yxx: Vector3fc
  inline get() = vec3(y(), x(), x())
val Vector2fc.xyx: Vector3fc
  inline get() = vec3(x(), y(), x())
val Vector2fc.yyx: Vector3fc
  inline get() = vec3(y(), y(), x())
val Vector2fc.xxy: Vector3fc
  inline get() = vec3(x(), x(), y())
val Vector2fc.yxy: Vector3fc
  inline get() = vec3(y(), x(), y())
val Vector2fc.xyy: Vector3fc
  inline get() = vec3(x(), y(), y())
val Vector2fc.yyy: Vector3fc
  inline get() = vec3(y(), y(), y())
val Vector3fc.xxx: Vector3fc
  inline get() = vec3(x(), x(), x())
val Vector3fc.yxx: Vector3fc
  inline get() = vec3(y(), x(), x())
val Vector3fc.zxx: Vector3fc
  inline get() = vec3(z(), x(), x())
val Vector3fc.xyx: Vector3fc
  inline get() = vec3(x(), y(), x())
val Vector3fc.yyx: Vector3fc
  inline get() = vec3(y(), y(), x())
val Vector3fc.zyx: Vector3fc
  inline get() = vec3(z(), y(), x())
val Vector3fc.xzx: Vector3fc
  inline get() = vec3(x(), z(), x())
val Vector3fc.yzx: Vector3fc
  inline get() = vec3(y(), z(), x())
val Vector3fc.zzx: Vector3fc
  inline get() = vec3(z(), z(), x())
val Vector3fc.xxy: Vector3fc
  inline get() = vec3(x(), x(), y())
val Vector3fc.yxy: Vector3fc
  inline get() = vec3(y(), x(), y())
val Vector3fc.zxy: Vector3fc
  inline get() = vec3(z(), x(), y())
val Vector3fc.xyy: Vector3fc
  inline get() = vec3(x(), y(), y())
val Vector3fc.yyy: Vector3fc
  inline get() = vec3(y(), y(), y())
val Vector3fc.zyy: Vector3fc
  inline get() = vec3(z(), y(), y())
val Vector3fc.xzy: Vector3fc
  inline get() = vec3(x(), z(), y())
val Vector3fc.yzy: Vector3fc
  inline get() = vec3(y(), z(), y())
val Vector3fc.zzy: Vector3fc
  inline get() = vec3(z(), z(), y())
val Vector3fc.xxz: Vector3fc
  inline get() = vec3(x(), x(), z())
val Vector3fc.yxz: Vector3fc
  inline get() = vec3(y(), x(), z())
val Vector3fc.zxz: Vector3fc
  inline get() = vec3(z(), x(), z())
val Vector3fc.xyz: Vector3fc
  inline get() = vec3(x(), y(), z())
val Vector3fc.yyz: Vector3fc
  inline get() = vec3(y(), y(), z())
val Vector3fc.zyz: Vector3fc
  inline get() = vec3(z(), y(), z())
val Vector3fc.xzz: Vector3fc
  inline get() = vec3(x(), z(), z())
val Vector3fc.yzz: Vector3fc
  inline get() = vec3(y(), z(), z())
val Vector3fc.zzz: Vector3fc
  inline get() = vec3(z(), z(), z())
val Vector4fc.xxx: Vector3fc
  inline get() = vec3(x(), x(), x())
val Vector4fc.yxx: Vector3fc
  inline get() = vec3(y(), x(), x())
val Vector4fc.zxx: Vector3fc
  inline get() = vec3(z(), x(), x())
val Vector4fc.wxx: Vector3fc
  inline get() = vec3(w(), x(), x())
val Vector4fc.xyx: Vector3fc
  inline get() = vec3(x(), y(), x())
val Vector4fc.yyx: Vector3fc
  inline get() = vec3(y(), y(), x())
val Vector4fc.zyx: Vector3fc
  inline get() = vec3(z(), y(), x())
val Vector4fc.wyx: Vector3fc
  inline get() = vec3(w(), y(), x())
val Vector4fc.xzx: Vector3fc
  inline get() = vec3(x(), z(), x())
val Vector4fc.yzx: Vector3fc
  inline get() = vec3(y(), z(), x())
val Vector4fc.zzx: Vector3fc
  inline get() = vec3(z(), z(), x())
val Vector4fc.wzx: Vector3fc
  inline get() = vec3(w(), z(), x())
val Vector4fc.xwx: Vector3fc
  inline get() = vec3(x(), w(), x())
val Vector4fc.ywx: Vector3fc
  inline get() = vec3(y(), w(), x())
val Vector4fc.zwx: Vector3fc
  inline get() = vec3(z(), w(), x())
val Vector4fc.wwx: Vector3fc
  inline get() = vec3(w(), w(), x())
val Vector4fc.xxy: Vector3fc
  inline get() = vec3(x(), x(), y())
val Vector4fc.yxy: Vector3fc
  inline get() = vec3(y(), x(), y())
val Vector4fc.zxy: Vector3fc
  inline get() = vec3(z(), x(), y())
val Vector4fc.wxy: Vector3fc
  inline get() = vec3(w(), x(), y())
val Vector4fc.xyy: Vector3fc
  inline get() = vec3(x(), y(), y())
val Vector4fc.yyy: Vector3fc
  inline get() = vec3(y(), y(), y())
val Vector4fc.zyy: Vector3fc
  inline get() = vec3(z(), y(), y())
val Vector4fc.wyy: Vector3fc
  inline get() = vec3(w(), y(), y())
val Vector4fc.xzy: Vector3fc
  inline get() = vec3(x(), z(), y())
val Vector4fc.yzy: Vector3fc
  inline get() = vec3(y(), z(), y())
val Vector4fc.zzy: Vector3fc
  inline get() = vec3(z(), z(), y())
val Vector4fc.wzy: Vector3fc
  inline get() = vec3(w(), z(), y())
val Vector4fc.xwy: Vector3fc
  inline get() = vec3(x(), w(), y())
val Vector4fc.ywy: Vector3fc
  inline get() = vec3(y(), w(), y())
val Vector4fc.zwy: Vector3fc
  inline get() = vec3(z(), w(), y())
val Vector4fc.wwy: Vector3fc
  inline get() = vec3(w(), w(), y())
val Vector4fc.xxz: Vector3fc
  inline get() = vec3(x(), x(), z())
val Vector4fc.yxz: Vector3fc
  inline get() = vec3(y(), x(), z())
val Vector4fc.zxz: Vector3fc
  inline get() = vec3(z(), x(), z())
val Vector4fc.wxz: Vector3fc
  inline get() = vec3(w(), x(), z())
val Vector4fc.xyz: Vector3fc
  inline get() = vec3(x(), y(), z())
val Vector4fc.yyz: Vector3fc
  inline get() = vec3(y(), y(), z())
val Vector4fc.zyz: Vector3fc
  inline get() = vec3(z(), y(), z())
val Vector4fc.wyz: Vector3fc
  inline get() = vec3(w(), y(), z())
val Vector4fc.xzz: Vector3fc
  inline get() = vec3(x(), z(), z())
val Vector4fc.yzz: Vector3fc
  inline get() = vec3(y(), z(), z())
val Vector4fc.zzz: Vector3fc
  inline get() = vec3(z(), z(), z())
val Vector4fc.wzz: Vector3fc
  inline get() = vec3(w(), z(), z())
val Vector4fc.xwz: Vector3fc
  inline get() = vec3(x(), w(), z())
val Vector4fc.ywz: Vector3fc
  inline get() = vec3(y(), w(), z())
val Vector4fc.zwz: Vector3fc
  inline get() = vec3(z(), w(), z())
val Vector4fc.wwz: Vector3fc
  inline get() = vec3(w(), w(), z())
val Vector4fc.xxw: Vector3fc
  inline get() = vec3(x(), x(), w())
val Vector4fc.yxw: Vector3fc
  inline get() = vec3(y(), x(), w())
val Vector4fc.zxw: Vector3fc
  inline get() = vec3(z(), x(), w())
val Vector4fc.wxw: Vector3fc
  inline get() = vec3(w(), x(), w())
val Vector4fc.xyw: Vector3fc
  inline get() = vec3(x(), y(), w())
val Vector4fc.yyw: Vector3fc
  inline get() = vec3(y(), y(), w())
val Vector4fc.zyw: Vector3fc
  inline get() = vec3(z(), y(), w())
val Vector4fc.wyw: Vector3fc
  inline get() = vec3(w(), y(), w())
val Vector4fc.xzw: Vector3fc
  inline get() = vec3(x(), z(), w())
val Vector4fc.yzw: Vector3fc
  inline get() = vec3(y(), z(), w())
val Vector4fc.zzw: Vector3fc
  inline get() = vec3(z(), z(), w())
val Vector4fc.wzw: Vector3fc
  inline get() = vec3(w(), z(), w())
val Vector4fc.xww: Vector3fc
  inline get() = vec3(x(), w(), w())
val Vector4fc.yww: Vector3fc
  inline get() = vec3(y(), w(), w())
val Vector4fc.zww: Vector3fc
  inline get() = vec3(z(), w(), w())
val Vector4fc.www: Vector3fc
  inline get() = vec3(w(), w(), w())
val Float.xxxx: Vector4fc
  inline get() = vec4(this, this, this, this)
val Vector2fc.xxxx: Vector4fc
  inline get() = vec4(x(), x(), x(), x())
val Vector2fc.yxxx: Vector4fc
  inline get() = vec4(y(), x(), x(), x())
val Vector2fc.xyxx: Vector4fc
  inline get() = vec4(x(), y(), x(), x())
val Vector2fc.yyxx: Vector4fc
  inline get() = vec4(y(), y(), x(), x())
val Vector2fc.xxyx: Vector4fc
  inline get() = vec4(x(), x(), y(), x())
val Vector2fc.yxyx: Vector4fc
  inline get() = vec4(y(), x(), y(), x())
val Vector2fc.xyyx: Vector4fc
  inline get() = vec4(x(), y(), y(), x())
val Vector2fc.yyyx: Vector4fc
  inline get() = vec4(y(), y(), y(), x())
val Vector2fc.xxxy: Vector4fc
  inline get() = vec4(x(), x(), x(), y())
val Vector2fc.yxxy: Vector4fc
  inline get() = vec4(y(), x(), x(), y())
val Vector2fc.xyxy: Vector4fc
  inline get() = vec4(x(), y(), x(), y())
val Vector2fc.yyxy: Vector4fc
  inline get() = vec4(y(), y(), x(), y())
val Vector2fc.xxyy: Vector4fc
  inline get() = vec4(x(), x(), y(), y())
val Vector2fc.yxyy: Vector4fc
  inline get() = vec4(y(), x(), y(), y())
val Vector2fc.xyyy: Vector4fc
  inline get() = vec4(x(), y(), y(), y())
val Vector2fc.yyyy: Vector4fc
  inline get() = vec4(y(), y(), y(), y())
val Vector3fc.xxxx: Vector4fc
  inline get() = vec4(x(), x(), x(), x())
val Vector3fc.yxxx: Vector4fc
  inline get() = vec4(y(), x(), x(), x())
val Vector3fc.zxxx: Vector4fc
  inline get() = vec4(z(), x(), x(), x())
val Vector3fc.xyxx: Vector4fc
  inline get() = vec4(x(), y(), x(), x())
val Vector3fc.yyxx: Vector4fc
  inline get() = vec4(y(), y(), x(), x())
val Vector3fc.zyxx: Vector4fc
  inline get() = vec4(z(), y(), x(), x())
val Vector3fc.xzxx: Vector4fc
  inline get() = vec4(x(), z(), x(), x())
val Vector3fc.yzxx: Vector4fc
  inline get() = vec4(y(), z(), x(), x())
val Vector3fc.zzxx: Vector4fc
  inline get() = vec4(z(), z(), x(), x())
val Vector3fc.xxyx: Vector4fc
  inline get() = vec4(x(), x(), y(), x())
val Vector3fc.yxyx: Vector4fc
  inline get() = vec4(y(), x(), y(), x())
val Vector3fc.zxyx: Vector4fc
  inline get() = vec4(z(), x(), y(), x())
val Vector3fc.xyyx: Vector4fc
  inline get() = vec4(x(), y(), y(), x())
val Vector3fc.yyyx: Vector4fc
  inline get() = vec4(y(), y(), y(), x())
val Vector3fc.zyyx: Vector4fc
  inline get() = vec4(z(), y(), y(), x())
val Vector3fc.xzyx: Vector4fc
  inline get() = vec4(x(), z(), y(), x())
val Vector3fc.yzyx: Vector4fc
  inline get() = vec4(y(), z(), y(), x())
val Vector3fc.zzyx: Vector4fc
  inline get() = vec4(z(), z(), y(), x())
val Vector3fc.xxzx: Vector4fc
  inline get() = vec4(x(), x(), z(), x())
val Vector3fc.yxzx: Vector4fc
  inline get() = vec4(y(), x(), z(), x())
val Vector3fc.zxzx: Vector4fc
  inline get() = vec4(z(), x(), z(), x())
val Vector3fc.xyzx: Vector4fc
  inline get() = vec4(x(), y(), z(), x())
val Vector3fc.yyzx: Vector4fc
  inline get() = vec4(y(), y(), z(), x())
val Vector3fc.zyzx: Vector4fc
  inline get() = vec4(z(), y(), z(), x())
val Vector3fc.xzzx: Vector4fc
  inline get() = vec4(x(), z(), z(), x())
val Vector3fc.yzzx: Vector4fc
  inline get() = vec4(y(), z(), z(), x())
val Vector3fc.zzzx: Vector4fc
  inline get() = vec4(z(), z(), z(), x())
val Vector3fc.xxxy: Vector4fc
  inline get() = vec4(x(), x(), x(), y())
val Vector3fc.yxxy: Vector4fc
  inline get() = vec4(y(), x(), x(), y())
val Vector3fc.zxxy: Vector4fc
  inline get() = vec4(z(), x(), x(), y())
val Vector3fc.xyxy: Vector4fc
  inline get() = vec4(x(), y(), x(), y())
val Vector3fc.yyxy: Vector4fc
  inline get() = vec4(y(), y(), x(), y())
val Vector3fc.zyxy: Vector4fc
  inline get() = vec4(z(), y(), x(), y())
val Vector3fc.xzxy: Vector4fc
  inline get() = vec4(x(), z(), x(), y())
val Vector3fc.yzxy: Vector4fc
  inline get() = vec4(y(), z(), x(), y())
val Vector3fc.zzxy: Vector4fc
  inline get() = vec4(z(), z(), x(), y())
val Vector3fc.xxyy: Vector4fc
  inline get() = vec4(x(), x(), y(), y())
val Vector3fc.yxyy: Vector4fc
  inline get() = vec4(y(), x(), y(), y())
val Vector3fc.zxyy: Vector4fc
  inline get() = vec4(z(), x(), y(), y())
val Vector3fc.xyyy: Vector4fc
  inline get() = vec4(x(), y(), y(), y())
val Vector3fc.yyyy: Vector4fc
  inline get() = vec4(y(), y(), y(), y())
val Vector3fc.zyyy: Vector4fc
  inline get() = vec4(z(), y(), y(), y())
val Vector3fc.xzyy: Vector4fc
  inline get() = vec4(x(), z(), y(), y())
val Vector3fc.yzyy: Vector4fc
  inline get() = vec4(y(), z(), y(), y())
val Vector3fc.zzyy: Vector4fc
  inline get() = vec4(z(), z(), y(), y())
val Vector3fc.xxzy: Vector4fc
  inline get() = vec4(x(), x(), z(), y())
val Vector3fc.yxzy: Vector4fc
  inline get() = vec4(y(), x(), z(), y())
val Vector3fc.zxzy: Vector4fc
  inline get() = vec4(z(), x(), z(), y())
val Vector3fc.xyzy: Vector4fc
  inline get() = vec4(x(), y(), z(), y())
val Vector3fc.yyzy: Vector4fc
  inline get() = vec4(y(), y(), z(), y())
val Vector3fc.zyzy: Vector4fc
  inline get() = vec4(z(), y(), z(), y())
val Vector3fc.xzzy: Vector4fc
  inline get() = vec4(x(), z(), z(), y())
val Vector3fc.yzzy: Vector4fc
  inline get() = vec4(y(), z(), z(), y())
val Vector3fc.zzzy: Vector4fc
  inline get() = vec4(z(), z(), z(), y())
val Vector3fc.xxxz: Vector4fc
  inline get() = vec4(x(), x(), x(), z())
val Vector3fc.yxxz: Vector4fc
  inline get() = vec4(y(), x(), x(), z())
val Vector3fc.zxxz: Vector4fc
  inline get() = vec4(z(), x(), x(), z())
val Vector3fc.xyxz: Vector4fc
  inline get() = vec4(x(), y(), x(), z())
val Vector3fc.yyxz: Vector4fc
  inline get() = vec4(y(), y(), x(), z())
val Vector3fc.zyxz: Vector4fc
  inline get() = vec4(z(), y(), x(), z())
val Vector3fc.xzxz: Vector4fc
  inline get() = vec4(x(), z(), x(), z())
val Vector3fc.yzxz: Vector4fc
  inline get() = vec4(y(), z(), x(), z())
val Vector3fc.zzxz: Vector4fc
  inline get() = vec4(z(), z(), x(), z())
val Vector3fc.xxyz: Vector4fc
  inline get() = vec4(x(), x(), y(), z())
val Vector3fc.yxyz: Vector4fc
  inline get() = vec4(y(), x(), y(), z())
val Vector3fc.zxyz: Vector4fc
  inline get() = vec4(z(), x(), y(), z())
val Vector3fc.xyyz: Vector4fc
  inline get() = vec4(x(), y(), y(), z())
val Vector3fc.yyyz: Vector4fc
  inline get() = vec4(y(), y(), y(), z())
val Vector3fc.zyyz: Vector4fc
  inline get() = vec4(z(), y(), y(), z())
val Vector3fc.xzyz: Vector4fc
  inline get() = vec4(x(), z(), y(), z())
val Vector3fc.yzyz: Vector4fc
  inline get() = vec4(y(), z(), y(), z())
val Vector3fc.zzyz: Vector4fc
  inline get() = vec4(z(), z(), y(), z())
val Vector3fc.xxzz: Vector4fc
  inline get() = vec4(x(), x(), z(), z())
val Vector3fc.yxzz: Vector4fc
  inline get() = vec4(y(), x(), z(), z())
val Vector3fc.zxzz: Vector4fc
  inline get() = vec4(z(), x(), z(), z())
val Vector3fc.xyzz: Vector4fc
  inline get() = vec4(x(), y(), z(), z())
val Vector3fc.yyzz: Vector4fc
  inline get() = vec4(y(), y(), z(), z())
val Vector3fc.zyzz: Vector4fc
  inline get() = vec4(z(), y(), z(), z())
val Vector3fc.xzzz: Vector4fc
  inline get() = vec4(x(), z(), z(), z())
val Vector3fc.yzzz: Vector4fc
  inline get() = vec4(y(), z(), z(), z())
val Vector3fc.zzzz: Vector4fc
  inline get() = vec4(z(), z(), z(), z())
val Vector4fc.xxxx: Vector4fc
  inline get() = vec4(x(), x(), x(), x())
val Vector4fc.yxxx: Vector4fc
  inline get() = vec4(y(), x(), x(), x())
val Vector4fc.zxxx: Vector4fc
  inline get() = vec4(z(), x(), x(), x())
val Vector4fc.wxxx: Vector4fc
  inline get() = vec4(w(), x(), x(), x())
val Vector4fc.xyxx: Vector4fc
  inline get() = vec4(x(), y(), x(), x())
val Vector4fc.yyxx: Vector4fc
  inline get() = vec4(y(), y(), x(), x())
val Vector4fc.zyxx: Vector4fc
  inline get() = vec4(z(), y(), x(), x())
val Vector4fc.wyxx: Vector4fc
  inline get() = vec4(w(), y(), x(), x())
val Vector4fc.xzxx: Vector4fc
  inline get() = vec4(x(), z(), x(), x())
val Vector4fc.yzxx: Vector4fc
  inline get() = vec4(y(), z(), x(), x())
val Vector4fc.zzxx: Vector4fc
  inline get() = vec4(z(), z(), x(), x())
val Vector4fc.wzxx: Vector4fc
  inline get() = vec4(w(), z(), x(), x())
val Vector4fc.xwxx: Vector4fc
  inline get() = vec4(x(), w(), x(), x())
val Vector4fc.ywxx: Vector4fc
  inline get() = vec4(y(), w(), x(), x())
val Vector4fc.zwxx: Vector4fc
  inline get() = vec4(z(), w(), x(), x())
val Vector4fc.wwxx: Vector4fc
  inline get() = vec4(w(), w(), x(), x())
val Vector4fc.xxyx: Vector4fc
  inline get() = vec4(x(), x(), y(), x())
val Vector4fc.yxyx: Vector4fc
  inline get() = vec4(y(), x(), y(), x())
val Vector4fc.zxyx: Vector4fc
  inline get() = vec4(z(), x(), y(), x())
val Vector4fc.wxyx: Vector4fc
  inline get() = vec4(w(), x(), y(), x())
val Vector4fc.xyyx: Vector4fc
  inline get() = vec4(x(), y(), y(), x())
val Vector4fc.yyyx: Vector4fc
  inline get() = vec4(y(), y(), y(), x())
val Vector4fc.zyyx: Vector4fc
  inline get() = vec4(z(), y(), y(), x())
val Vector4fc.wyyx: Vector4fc
  inline get() = vec4(w(), y(), y(), x())
val Vector4fc.xzyx: Vector4fc
  inline get() = vec4(x(), z(), y(), x())
val Vector4fc.yzyx: Vector4fc
  inline get() = vec4(y(), z(), y(), x())
val Vector4fc.zzyx: Vector4fc
  inline get() = vec4(z(), z(), y(), x())
val Vector4fc.wzyx: Vector4fc
  inline get() = vec4(w(), z(), y(), x())
val Vector4fc.xwyx: Vector4fc
  inline get() = vec4(x(), w(), y(), x())
val Vector4fc.ywyx: Vector4fc
  inline get() = vec4(y(), w(), y(), x())
val Vector4fc.zwyx: Vector4fc
  inline get() = vec4(z(), w(), y(), x())
val Vector4fc.wwyx: Vector4fc
  inline get() = vec4(w(), w(), y(), x())
val Vector4fc.xxzx: Vector4fc
  inline get() = vec4(x(), x(), z(), x())
val Vector4fc.yxzx: Vector4fc
  inline get() = vec4(y(), x(), z(), x())
val Vector4fc.zxzx: Vector4fc
  inline get() = vec4(z(), x(), z(), x())
val Vector4fc.wxzx: Vector4fc
  inline get() = vec4(w(), x(), z(), x())
val Vector4fc.xyzx: Vector4fc
  inline get() = vec4(x(), y(), z(), x())
val Vector4fc.yyzx: Vector4fc
  inline get() = vec4(y(), y(), z(), x())
val Vector4fc.zyzx: Vector4fc
  inline get() = vec4(z(), y(), z(), x())
val Vector4fc.wyzx: Vector4fc
  inline get() = vec4(w(), y(), z(), x())
val Vector4fc.xzzx: Vector4fc
  inline get() = vec4(x(), z(), z(), x())
val Vector4fc.yzzx: Vector4fc
  inline get() = vec4(y(), z(), z(), x())
val Vector4fc.zzzx: Vector4fc
  inline get() = vec4(z(), z(), z(), x())
val Vector4fc.wzzx: Vector4fc
  inline get() = vec4(w(), z(), z(), x())
val Vector4fc.xwzx: Vector4fc
  inline get() = vec4(x(), w(), z(), x())
val Vector4fc.ywzx: Vector4fc
  inline get() = vec4(y(), w(), z(), x())
val Vector4fc.zwzx: Vector4fc
  inline get() = vec4(z(), w(), z(), x())
val Vector4fc.wwzx: Vector4fc
  inline get() = vec4(w(), w(), z(), x())
val Vector4fc.xxwx: Vector4fc
  inline get() = vec4(x(), x(), w(), x())
val Vector4fc.yxwx: Vector4fc
  inline get() = vec4(y(), x(), w(), x())
val Vector4fc.zxwx: Vector4fc
  inline get() = vec4(z(), x(), w(), x())
val Vector4fc.wxwx: Vector4fc
  inline get() = vec4(w(), x(), w(), x())
val Vector4fc.xywx: Vector4fc
  inline get() = vec4(x(), y(), w(), x())
val Vector4fc.yywx: Vector4fc
  inline get() = vec4(y(), y(), w(), x())
val Vector4fc.zywx: Vector4fc
  inline get() = vec4(z(), y(), w(), x())
val Vector4fc.wywx: Vector4fc
  inline get() = vec4(w(), y(), w(), x())
val Vector4fc.xzwx: Vector4fc
  inline get() = vec4(x(), z(), w(), x())
val Vector4fc.yzwx: Vector4fc
  inline get() = vec4(y(), z(), w(), x())
val Vector4fc.zzwx: Vector4fc
  inline get() = vec4(z(), z(), w(), x())
val Vector4fc.wzwx: Vector4fc
  inline get() = vec4(w(), z(), w(), x())
val Vector4fc.xwwx: Vector4fc
  inline get() = vec4(x(), w(), w(), x())
val Vector4fc.ywwx: Vector4fc
  inline get() = vec4(y(), w(), w(), x())
val Vector4fc.zwwx: Vector4fc
  inline get() = vec4(z(), w(), w(), x())
val Vector4fc.wwwx: Vector4fc
  inline get() = vec4(w(), w(), w(), x())
val Vector4fc.xxxy: Vector4fc
  inline get() = vec4(x(), x(), x(), y())
val Vector4fc.yxxy: Vector4fc
  inline get() = vec4(y(), x(), x(), y())
val Vector4fc.zxxy: Vector4fc
  inline get() = vec4(z(), x(), x(), y())
val Vector4fc.wxxy: Vector4fc
  inline get() = vec4(w(), x(), x(), y())
val Vector4fc.xyxy: Vector4fc
  inline get() = vec4(x(), y(), x(), y())
val Vector4fc.yyxy: Vector4fc
  inline get() = vec4(y(), y(), x(), y())
val Vector4fc.zyxy: Vector4fc
  inline get() = vec4(z(), y(), x(), y())
val Vector4fc.wyxy: Vector4fc
  inline get() = vec4(w(), y(), x(), y())
val Vector4fc.xzxy: Vector4fc
  inline get() = vec4(x(), z(), x(), y())
val Vector4fc.yzxy: Vector4fc
  inline get() = vec4(y(), z(), x(), y())
val Vector4fc.zzxy: Vector4fc
  inline get() = vec4(z(), z(), x(), y())
val Vector4fc.wzxy: Vector4fc
  inline get() = vec4(w(), z(), x(), y())
val Vector4fc.xwxy: Vector4fc
  inline get() = vec4(x(), w(), x(), y())
val Vector4fc.ywxy: Vector4fc
  inline get() = vec4(y(), w(), x(), y())
val Vector4fc.zwxy: Vector4fc
  inline get() = vec4(z(), w(), x(), y())
val Vector4fc.wwxy: Vector4fc
  inline get() = vec4(w(), w(), x(), y())
val Vector4fc.xxyy: Vector4fc
  inline get() = vec4(x(), x(), y(), y())
val Vector4fc.yxyy: Vector4fc
  inline get() = vec4(y(), x(), y(), y())
val Vector4fc.zxyy: Vector4fc
  inline get() = vec4(z(), x(), y(), y())
val Vector4fc.wxyy: Vector4fc
  inline get() = vec4(w(), x(), y(), y())
val Vector4fc.xyyy: Vector4fc
  inline get() = vec4(x(), y(), y(), y())
val Vector4fc.yyyy: Vector4fc
  inline get() = vec4(y(), y(), y(), y())
val Vector4fc.zyyy: Vector4fc
  inline get() = vec4(z(), y(), y(), y())
val Vector4fc.wyyy: Vector4fc
  inline get() = vec4(w(), y(), y(), y())
val Vector4fc.xzyy: Vector4fc
  inline get() = vec4(x(), z(), y(), y())
val Vector4fc.yzyy: Vector4fc
  inline get() = vec4(y(), z(), y(), y())
val Vector4fc.zzyy: Vector4fc
  inline get() = vec4(z(), z(), y(), y())
val Vector4fc.wzyy: Vector4fc
  inline get() = vec4(w(), z(), y(), y())
val Vector4fc.xwyy: Vector4fc
  inline get() = vec4(x(), w(), y(), y())
val Vector4fc.ywyy: Vector4fc
  inline get() = vec4(y(), w(), y(), y())
val Vector4fc.zwyy: Vector4fc
  inline get() = vec4(z(), w(), y(), y())
val Vector4fc.wwyy: Vector4fc
  inline get() = vec4(w(), w(), y(), y())
val Vector4fc.xxzy: Vector4fc
  inline get() = vec4(x(), x(), z(), y())
val Vector4fc.yxzy: Vector4fc
  inline get() = vec4(y(), x(), z(), y())
val Vector4fc.zxzy: Vector4fc
  inline get() = vec4(z(), x(), z(), y())
val Vector4fc.wxzy: Vector4fc
  inline get() = vec4(w(), x(), z(), y())
val Vector4fc.xyzy: Vector4fc
  inline get() = vec4(x(), y(), z(), y())
val Vector4fc.yyzy: Vector4fc
  inline get() = vec4(y(), y(), z(), y())
val Vector4fc.zyzy: Vector4fc
  inline get() = vec4(z(), y(), z(), y())
val Vector4fc.wyzy: Vector4fc
  inline get() = vec4(w(), y(), z(), y())
val Vector4fc.xzzy: Vector4fc
  inline get() = vec4(x(), z(), z(), y())
val Vector4fc.yzzy: Vector4fc
  inline get() = vec4(y(), z(), z(), y())
val Vector4fc.zzzy: Vector4fc
  inline get() = vec4(z(), z(), z(), y())
val Vector4fc.wzzy: Vector4fc
  inline get() = vec4(w(), z(), z(), y())
val Vector4fc.xwzy: Vector4fc
  inline get() = vec4(x(), w(), z(), y())
val Vector4fc.ywzy: Vector4fc
  inline get() = vec4(y(), w(), z(), y())
val Vector4fc.zwzy: Vector4fc
  inline get() = vec4(z(), w(), z(), y())
val Vector4fc.wwzy: Vector4fc
  inline get() = vec4(w(), w(), z(), y())
val Vector4fc.xxwy: Vector4fc
  inline get() = vec4(x(), x(), w(), y())
val Vector4fc.yxwy: Vector4fc
  inline get() = vec4(y(), x(), w(), y())
val Vector4fc.zxwy: Vector4fc
  inline get() = vec4(z(), x(), w(), y())
val Vector4fc.wxwy: Vector4fc
  inline get() = vec4(w(), x(), w(), y())
val Vector4fc.xywy: Vector4fc
  inline get() = vec4(x(), y(), w(), y())
val Vector4fc.yywy: Vector4fc
  inline get() = vec4(y(), y(), w(), y())
val Vector4fc.zywy: Vector4fc
  inline get() = vec4(z(), y(), w(), y())
val Vector4fc.wywy: Vector4fc
  inline get() = vec4(w(), y(), w(), y())
val Vector4fc.xzwy: Vector4fc
  inline get() = vec4(x(), z(), w(), y())
val Vector4fc.yzwy: Vector4fc
  inline get() = vec4(y(), z(), w(), y())
val Vector4fc.zzwy: Vector4fc
  inline get() = vec4(z(), z(), w(), y())
val Vector4fc.wzwy: Vector4fc
  inline get() = vec4(w(), z(), w(), y())
val Vector4fc.xwwy: Vector4fc
  inline get() = vec4(x(), w(), w(), y())
val Vector4fc.ywwy: Vector4fc
  inline get() = vec4(y(), w(), w(), y())
val Vector4fc.zwwy: Vector4fc
  inline get() = vec4(z(), w(), w(), y())
val Vector4fc.wwwy: Vector4fc
  inline get() = vec4(w(), w(), w(), y())
val Vector4fc.xxxz: Vector4fc
  inline get() = vec4(x(), x(), x(), z())
val Vector4fc.yxxz: Vector4fc
  inline get() = vec4(y(), x(), x(), z())
val Vector4fc.zxxz: Vector4fc
  inline get() = vec4(z(), x(), x(), z())
val Vector4fc.wxxz: Vector4fc
  inline get() = vec4(w(), x(), x(), z())
val Vector4fc.xyxz: Vector4fc
  inline get() = vec4(x(), y(), x(), z())
val Vector4fc.yyxz: Vector4fc
  inline get() = vec4(y(), y(), x(), z())
val Vector4fc.zyxz: Vector4fc
  inline get() = vec4(z(), y(), x(), z())
val Vector4fc.wyxz: Vector4fc
  inline get() = vec4(w(), y(), x(), z())
val Vector4fc.xzxz: Vector4fc
  inline get() = vec4(x(), z(), x(), z())
val Vector4fc.yzxz: Vector4fc
  inline get() = vec4(y(), z(), x(), z())
val Vector4fc.zzxz: Vector4fc
  inline get() = vec4(z(), z(), x(), z())
val Vector4fc.wzxz: Vector4fc
  inline get() = vec4(w(), z(), x(), z())
val Vector4fc.xwxz: Vector4fc
  inline get() = vec4(x(), w(), x(), z())
val Vector4fc.ywxz: Vector4fc
  inline get() = vec4(y(), w(), x(), z())
val Vector4fc.zwxz: Vector4fc
  inline get() = vec4(z(), w(), x(), z())
val Vector4fc.wwxz: Vector4fc
  inline get() = vec4(w(), w(), x(), z())
val Vector4fc.xxyz: Vector4fc
  inline get() = vec4(x(), x(), y(), z())
val Vector4fc.yxyz: Vector4fc
  inline get() = vec4(y(), x(), y(), z())
val Vector4fc.zxyz: Vector4fc
  inline get() = vec4(z(), x(), y(), z())
val Vector4fc.wxyz: Vector4fc
  inline get() = vec4(w(), x(), y(), z())
val Vector4fc.xyyz: Vector4fc
  inline get() = vec4(x(), y(), y(), z())
val Vector4fc.yyyz: Vector4fc
  inline get() = vec4(y(), y(), y(), z())
val Vector4fc.zyyz: Vector4fc
  inline get() = vec4(z(), y(), y(), z())
val Vector4fc.wyyz: Vector4fc
  inline get() = vec4(w(), y(), y(), z())
val Vector4fc.xzyz: Vector4fc
  inline get() = vec4(x(), z(), y(), z())
val Vector4fc.yzyz: Vector4fc
  inline get() = vec4(y(), z(), y(), z())
val Vector4fc.zzyz: Vector4fc
  inline get() = vec4(z(), z(), y(), z())
val Vector4fc.wzyz: Vector4fc
  inline get() = vec4(w(), z(), y(), z())
val Vector4fc.xwyz: Vector4fc
  inline get() = vec4(x(), w(), y(), z())
val Vector4fc.ywyz: Vector4fc
  inline get() = vec4(y(), w(), y(), z())
val Vector4fc.zwyz: Vector4fc
  inline get() = vec4(z(), w(), y(), z())
val Vector4fc.wwyz: Vector4fc
  inline get() = vec4(w(), w(), y(), z())
val Vector4fc.xxzz: Vector4fc
  inline get() = vec4(x(), x(), z(), z())
val Vector4fc.yxzz: Vector4fc
  inline get() = vec4(y(), x(), z(), z())
val Vector4fc.zxzz: Vector4fc
  inline get() = vec4(z(), x(), z(), z())
val Vector4fc.wxzz: Vector4fc
  inline get() = vec4(w(), x(), z(), z())
val Vector4fc.xyzz: Vector4fc
  inline get() = vec4(x(), y(), z(), z())
val Vector4fc.yyzz: Vector4fc
  inline get() = vec4(y(), y(), z(), z())
val Vector4fc.zyzz: Vector4fc
  inline get() = vec4(z(), y(), z(), z())
val Vector4fc.wyzz: Vector4fc
  inline get() = vec4(w(), y(), z(), z())
val Vector4fc.xzzz: Vector4fc
  inline get() = vec4(x(), z(), z(), z())
val Vector4fc.yzzz: Vector4fc
  inline get() = vec4(y(), z(), z(), z())
val Vector4fc.zzzz: Vector4fc
  inline get() = vec4(z(), z(), z(), z())
val Vector4fc.wzzz: Vector4fc
  inline get() = vec4(w(), z(), z(), z())
val Vector4fc.xwzz: Vector4fc
  inline get() = vec4(x(), w(), z(), z())
val Vector4fc.ywzz: Vector4fc
  inline get() = vec4(y(), w(), z(), z())
val Vector4fc.zwzz: Vector4fc
  inline get() = vec4(z(), w(), z(), z())
val Vector4fc.wwzz: Vector4fc
  inline get() = vec4(w(), w(), z(), z())
val Vector4fc.xxwz: Vector4fc
  inline get() = vec4(x(), x(), w(), z())
val Vector4fc.yxwz: Vector4fc
  inline get() = vec4(y(), x(), w(), z())
val Vector4fc.zxwz: Vector4fc
  inline get() = vec4(z(), x(), w(), z())
val Vector4fc.wxwz: Vector4fc
  inline get() = vec4(w(), x(), w(), z())
val Vector4fc.xywz: Vector4fc
  inline get() = vec4(x(), y(), w(), z())
val Vector4fc.yywz: Vector4fc
  inline get() = vec4(y(), y(), w(), z())
val Vector4fc.zywz: Vector4fc
  inline get() = vec4(z(), y(), w(), z())
val Vector4fc.wywz: Vector4fc
  inline get() = vec4(w(), y(), w(), z())
val Vector4fc.xzwz: Vector4fc
  inline get() = vec4(x(), z(), w(), z())
val Vector4fc.yzwz: Vector4fc
  inline get() = vec4(y(), z(), w(), z())
val Vector4fc.zzwz: Vector4fc
  inline get() = vec4(z(), z(), w(), z())
val Vector4fc.wzwz: Vector4fc
  inline get() = vec4(w(), z(), w(), z())
val Vector4fc.xwwz: Vector4fc
  inline get() = vec4(x(), w(), w(), z())
val Vector4fc.ywwz: Vector4fc
  inline get() = vec4(y(), w(), w(), z())
val Vector4fc.zwwz: Vector4fc
  inline get() = vec4(z(), w(), w(), z())
val Vector4fc.wwwz: Vector4fc
  inline get() = vec4(w(), w(), w(), z())
val Vector4fc.xxxw: Vector4fc
  inline get() = vec4(x(), x(), x(), w())
val Vector4fc.yxxw: Vector4fc
  inline get() = vec4(y(), x(), x(), w())
val Vector4fc.zxxw: Vector4fc
  inline get() = vec4(z(), x(), x(), w())
val Vector4fc.wxxw: Vector4fc
  inline get() = vec4(w(), x(), x(), w())
val Vector4fc.xyxw: Vector4fc
  inline get() = vec4(x(), y(), x(), w())
val Vector4fc.yyxw: Vector4fc
  inline get() = vec4(y(), y(), x(), w())
val Vector4fc.zyxw: Vector4fc
  inline get() = vec4(z(), y(), x(), w())
val Vector4fc.wyxw: Vector4fc
  inline get() = vec4(w(), y(), x(), w())
val Vector4fc.xzxw: Vector4fc
  inline get() = vec4(x(), z(), x(), w())
val Vector4fc.yzxw: Vector4fc
  inline get() = vec4(y(), z(), x(), w())
val Vector4fc.zzxw: Vector4fc
  inline get() = vec4(z(), z(), x(), w())
val Vector4fc.wzxw: Vector4fc
  inline get() = vec4(w(), z(), x(), w())
val Vector4fc.xwxw: Vector4fc
  inline get() = vec4(x(), w(), x(), w())
val Vector4fc.ywxw: Vector4fc
  inline get() = vec4(y(), w(), x(), w())
val Vector4fc.zwxw: Vector4fc
  inline get() = vec4(z(), w(), x(), w())
val Vector4fc.wwxw: Vector4fc
  inline get() = vec4(w(), w(), x(), w())
val Vector4fc.xxyw: Vector4fc
  inline get() = vec4(x(), x(), y(), w())
val Vector4fc.yxyw: Vector4fc
  inline get() = vec4(y(), x(), y(), w())
val Vector4fc.zxyw: Vector4fc
  inline get() = vec4(z(), x(), y(), w())
val Vector4fc.wxyw: Vector4fc
  inline get() = vec4(w(), x(), y(), w())
val Vector4fc.xyyw: Vector4fc
  inline get() = vec4(x(), y(), y(), w())
val Vector4fc.yyyw: Vector4fc
  inline get() = vec4(y(), y(), y(), w())
val Vector4fc.zyyw: Vector4fc
  inline get() = vec4(z(), y(), y(), w())
val Vector4fc.wyyw: Vector4fc
  inline get() = vec4(w(), y(), y(), w())
val Vector4fc.xzyw: Vector4fc
  inline get() = vec4(x(), z(), y(), w())
val Vector4fc.yzyw: Vector4fc
  inline get() = vec4(y(), z(), y(), w())
val Vector4fc.zzyw: Vector4fc
  inline get() = vec4(z(), z(), y(), w())
val Vector4fc.wzyw: Vector4fc
  inline get() = vec4(w(), z(), y(), w())
val Vector4fc.xwyw: Vector4fc
  inline get() = vec4(x(), w(), y(), w())
val Vector4fc.ywyw: Vector4fc
  inline get() = vec4(y(), w(), y(), w())
val Vector4fc.zwyw: Vector4fc
  inline get() = vec4(z(), w(), y(), w())
val Vector4fc.wwyw: Vector4fc
  inline get() = vec4(w(), w(), y(), w())
val Vector4fc.xxzw: Vector4fc
  inline get() = vec4(x(), x(), z(), w())
val Vector4fc.yxzw: Vector4fc
  inline get() = vec4(y(), x(), z(), w())
val Vector4fc.zxzw: Vector4fc
  inline get() = vec4(z(), x(), z(), w())
val Vector4fc.wxzw: Vector4fc
  inline get() = vec4(w(), x(), z(), w())
val Vector4fc.xyzw: Vector4fc
  inline get() = vec4(x(), y(), z(), w())
val Vector4fc.yyzw: Vector4fc
  inline get() = vec4(y(), y(), z(), w())
val Vector4fc.zyzw: Vector4fc
  inline get() = vec4(z(), y(), z(), w())
val Vector4fc.wyzw: Vector4fc
  inline get() = vec4(w(), y(), z(), w())
val Vector4fc.xzzw: Vector4fc
  inline get() = vec4(x(), z(), z(), w())
val Vector4fc.yzzw: Vector4fc
  inline get() = vec4(y(), z(), z(), w())
val Vector4fc.zzzw: Vector4fc
  inline get() = vec4(z(), z(), z(), w())
val Vector4fc.wzzw: Vector4fc
  inline get() = vec4(w(), z(), z(), w())
val Vector4fc.xwzw: Vector4fc
  inline get() = vec4(x(), w(), z(), w())
val Vector4fc.ywzw: Vector4fc
  inline get() = vec4(y(), w(), z(), w())
val Vector4fc.zwzw: Vector4fc
  inline get() = vec4(z(), w(), z(), w())
val Vector4fc.wwzw: Vector4fc
  inline get() = vec4(w(), w(), z(), w())
val Vector4fc.xxww: Vector4fc
  inline get() = vec4(x(), x(), w(), w())
val Vector4fc.yxww: Vector4fc
  inline get() = vec4(y(), x(), w(), w())
val Vector4fc.zxww: Vector4fc
  inline get() = vec4(z(), x(), w(), w())
val Vector4fc.wxww: Vector4fc
  inline get() = vec4(w(), x(), w(), w())
val Vector4fc.xyww: Vector4fc
  inline get() = vec4(x(), y(), w(), w())
val Vector4fc.yyww: Vector4fc
  inline get() = vec4(y(), y(), w(), w())
val Vector4fc.zyww: Vector4fc
  inline get() = vec4(z(), y(), w(), w())
val Vector4fc.wyww: Vector4fc
  inline get() = vec4(w(), y(), w(), w())
val Vector4fc.xzww: Vector4fc
  inline get() = vec4(x(), z(), w(), w())
val Vector4fc.yzww: Vector4fc
  inline get() = vec4(y(), z(), w(), w())
val Vector4fc.zzww: Vector4fc
  inline get() = vec4(z(), z(), w(), w())
val Vector4fc.wzww: Vector4fc
  inline get() = vec4(w(), z(), w(), w())
val Vector4fc.xwww: Vector4fc
  inline get() = vec4(x(), w(), w(), w())
val Vector4fc.ywww: Vector4fc
  inline get() = vec4(y(), w(), w(), w())
val Vector4fc.zwww: Vector4fc
  inline get() = vec4(z(), w(), w(), w())
val Vector4fc.wwww: Vector4fc
  inline get() = vec4(w(), w(), w(), w())
