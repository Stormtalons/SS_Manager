import java.awt.MenuItem
import java.awt.event._

trait Tools
{
	implicit def anyToCode[T <: Any](_code: => T) = new Code(_code)
	implicit def codeToActionListener(_code: => Unit) = new ActionListener {def actionPerformed(e: ActionEvent) = _code}
	implicit def codeToMouseAdapter(_code: (MouseEvent => Unit, MouseEvent => Unit)) = new MouseAdapter
	{
		override def mousePressed(e: MouseEvent) = if (_code._1 != null) _code._1(e)
		override def mouseReleased(e: MouseEvent) = if (_code._2 != null) _code._2(e)
	}
	implicit def codeToMouseMotionAdapter(_code: (MouseEvent => Unit, MouseEvent => Unit)) = new MouseMotionAdapter
	{
		override def mouseMoved(e: MouseEvent) = if (_code._1 != null) _code._1(e)
		override def mouseDragged(e: MouseEvent) = if (_code._2 != null) _code._2(e)
	}
	implicit def codeToKeyAdapter(_code: KeyEvent => Unit) = new KeyAdapter {override def keyPressed(e: KeyEvent) = _code}
	implicit def codeToMenuItem(_params: (String, () => Unit)) =
	{
		val temp = new MenuItem(_params._1)
		temp.addActionListener(_params._2())
		temp
	}

	def tryGet[T <: Any](_code: => T, _dft: => T = null) = try _code catch {case e: Exception => _dft}
}
