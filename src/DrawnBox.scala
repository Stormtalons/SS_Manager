import java.awt.Point
import scala.math.{min, max}

class DrawnBox(_original: Point)
{
	def this(_original: Point, _new: Point)
	{
		this(_original)
		setSize(_new)
	}

	val original = _original
	var x = 0
	var y = 0
	var w = 0
	var h = 0
	setSize(original)

	def setSize(_new: Point)
	{
		x = min(original.x, _new.x)
		y = min(original.y, _new.y)
		w = max(original.x, _new.x) - min(original.x, _new.x)
		h = max(original.y, _new.y) - min(original.y, _new.y)
	}
}