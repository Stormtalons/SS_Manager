import java.awt.datatransfer.DataFlavor
import java.awt.event.{MouseMotionAdapter, MouseAdapter, MouseEvent, KeyEvent}
import java.awt.image.BufferedImage
import java.awt._
import java.nio.file.{Paths, Files}
import javax.imageio.ImageIO
import javax.swing.{JOptionPane, JPanel, SpringLayout, JFrame}

import scala.collection.mutable.ArrayBuffer

class SSFrame(a: BufferedImage) extends JFrame with Tools
{
	var active = true
	var resize = false
	var suspend = false
	val boxes = new ArrayBuffer[DrawnBox]
	var original = a
	var current = a
	var click: Point = null
	val c = getContentPane
	val imgPane = new JPanel {
		override def paint(g: Graphics)
		{
			g.drawImage(current, 0, 0, null)
			g.setColor(Color.RED)
			boxes.foreach(b => {g.drawRect(b.x, b.y, b.w, b.h);g.drawRect(b.x + 1, b.y + 1, b.w - 2, b.h - 2)})
		}}
	imgPane.setBackground(Color.green)
	val sl = new SpringLayout
	sl.putConstraint(SpringLayout.NORTH, imgPane, 2, SpringLayout.NORTH, c)
	sl.putConstraint(SpringLayout.WEST, imgPane, 2, SpringLayout.WEST, c)
	sl.putConstraint(SpringLayout.EAST, imgPane, -2, SpringLayout.EAST, c)
	sl.putConstraint(SpringLayout.SOUTH, imgPane, -2, SpringLayout.SOUTH, c)
	c.setLayout(sl)
	c.add(imgPane)
	addKeyListener((e: KeyEvent) =>
		if (e.getKeyCode == KeyEvent.VK_V && e.isControlDown)
		{
			val t = Toolkit.getDefaultToolkit.getSystemClipboard.getContents(null)
			if (t.isDataFlavorSupported(DataFlavor.imageFlavor))
			{
					current = t.getTransferData(DataFlavor.imageFlavor).asInstanceOf[BufferedImage]
					setSize(new Dimension(current.getWidth + 4, current.getHeight + 4))
					repaint()
			}^^
		})

	addMouseListener(((e: MouseEvent) =>
		if (e.getButton == MouseEvent.BUTTON3)
		{
			setVisible(false)
			(Main.ssfs -= this).x
		}
		else if (e.getButton == MouseEvent.BUTTON1)
		{
			if (inResizeBlock(e))
			{
				moveTo(getX + getWidth, getY + getHeight, true)
				click = new Point(getWidth, getHeight)
			} else click = e.getPoint
			if (e.isControlDown && e.isAltDown) saveImage
			else if (e.isAltDown) boxes.clear
			else if (e.isShiftDown) Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new ImageTransferable(getScreen), null)
			repaint()
		},

		(e: MouseEvent) =>
		if (e.getButton == MouseEvent.BUTTON1 && resize)
		{
			val imw = current.getWidth.toDouble
			val imh = current.getHeight.toDouble
			var xdif = e.getX - click.getX
			var ydif = e.getY - click.getY
			if (xdif == 0 && ydif == 0) current = original
			else
			{
				if (Math.abs(xdif) < Math.abs(ydif)) ydif = xdif / (imw / imh)
				else xdif = ydif * (imw / imh)
				val temp = new BufferedImage((imw + xdif).toInt, (imh + ydif).toInt, BufferedImage.TYPE_INT_RGB)
				val g = temp.createGraphics
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
				g.drawImage(original, 0, 0, temp.getWidth, temp.getHeight, 0, 0, original.getWidth, original.getHeight, null)
				g.dispose
				current = temp
			}
			setSize(current.getWidth + 4, current.getHeight + 4)
			repaint()
		}): MouseAdapter)

	addMouseMotionListener(((e: MouseEvent) => setResizing(inResizeBlock(e)),
		(e: MouseEvent) =>
		if (e.isControlDown)
		{
			if (click != null)
			{
				boxes += new DrawnBox(click, e.getPoint)
				click = null
			} else boxes(boxes.size - 1).setSize(e.getPoint)
			repaint()
		}
		else if (click != null)
		{
			if (resize) setSize(e.getX, e.getY)
			else setLocation(e.getXOnScreen - click.x, e.getYOnScreen - click.y)
		}): MouseMotionAdapter)


	private def getScreen =
	{
		val tr = new BufferedImage(current.getWidth, current.getHeight, BufferedImage.TYPE_INT_RGB)
		imgPane.paint(tr.getGraphics)
		tr
	}

	def saveImage: Unit = (1 to 100).foreach(i =>
	{
		val path = Paths.get(s"${System.getenv("USERPROFILE")}\\Desktop\\$i.png")
		if (!Files.exists(path))
		{
			ImageIO.write(getScreen, "png", Files.newOutputStream(path))^^JOptionPane.showMessageDialog(null, s"Error writing file: '$path'.\nPlease try again")
			return
		}
	})
	private def setResizing(b: Boolean) = {resize = b;setCursor(new Cursor(if (resize) Cursor.SE_RESIZE_CURSOR else Cursor.DEFAULT_CURSOR))}
	private def inResizeBlock(e: MouseEvent) =
	{
		val xmax = getX + getWidth
		val ymax = getY + getHeight
		e.getXOnScreen >= xmax - 10 && e.getXOnScreen <= xmax && e.getYOnScreen >= ymax - 10 && e.getYOnScreen <= ymax
	}
	private def moveTo(x: Int, y: Int, s: Boolean)
	{
		if (s) suspend = true
		Main.robot.mouseMove(x, y).^^
		suspend = false
	}

	override def paint(g: Graphics)
	{
		g.setColor(Color.BLACK)
		g.drawRect(0, 0, getWidth - 1, getHeight - 1)
		g.setColor(Color.WHITE)
		g.drawRect(1, 1, getWidth - 3, getHeight - 3)
		imgPane.repaint()
	}

	setSize(new Dimension(current.getWidth + 4, current.getHeight + 4))
	val r = GraphicsEnvironment.getLocalGraphicsEnvironment.getScreenDevices()(Main.monitor).getDefaultConfiguration.getBounds
	setLocation(r.x, r.y)
	setUndecorated(true)
	setVisible(true)
	setAlwaysOnTop(true)
	setAlwaysOnTop(false)
}