import java.awt._
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.{RandomAccessFile, FilenameFilter, File}
import java.nio.file.{Files, Paths}
import javax.imageio.ImageIO
import javax.swing.{JOptionPane, ImageIcon}

import scala.collection.mutable.ArrayBuffer

object Main extends App with Tools
{
	val ini = new File("SSManager.ini")
	var ssFolder: File = null
	var monitor = 0
	val ssfs = new ArrayBuffer[SSFrame]
	val ignoredFiles = new ArrayBuffer[String]
	val robot = new Robot
	var suspended = false
	val pop = new PopupMenu
	val ico = if (new File("/eye.png").exists) new TrayIcon(new ImageIcon(Toolkit.getDefaultToolkit.getImage("/eye.png")).getImage, "", pop)
	else new TrayIcon(new ImageIcon(Toolkit.getDefaultToolkit.getImage(getClass.getResource("/eye.png"))).getImage, "", pop)
	pop.add("Suspend", {suspended = !suspended;pop.getItem(0).setLabel(if (suspended) "Resume" else "Suspend")}%)
	pop.add("Open INI", (if (ini.exists) Desktop.getDesktop.open(ini)^^)%)
	pop.add("Reload INI", loadIni%)
	pop.add("Clear problem file list", ignoredFiles.clear%)
	pop.add("Exit", quit%)
	ico.addMouseListener((e: MouseEvent) => if (e.getButton == MouseEvent.BUTTON1) ssfs.foreach(ssf => ssf.setVisible(!ssfs(ssfs.length - 1).isVisible)), null)
	SystemTray.getSystemTray.add(ico)

	loadIni

	while (true)
		if (!suspended)
		{
			if (ssFolder == null) quit
			if (!ssFolder.exists) quit
			ssFolder.listFiles(new FilenameFilter
			{
				def accept(dir: File, name: String) = name.toLowerCase.endsWith(".jpg") && !ignoredFiles.contains(name)
			}).foreach(file =>
				while (
					try
					{
						val raf = new RandomAccessFile(file.getAbsolutePath, "rw")
						val fl = raf.getChannel.tryLock
						if (fl != null)
						{
							raf.close
							val im = tryGet[BufferedImage](ImageIO.read(file), null)
							if (im != null) ssfs += new SSFrame(im)
							var deleteAttempts = 0
							do if (file.delete)
								deleteAttempts = -1
							else
							{
								deleteAttempts += 1
								Thread.sleep(10)
							} while (deleteAttempts != -1 && deleteAttempts < 5)
							if (deleteAttempts > 0)
								JOptionPane.showMessageDialog(null, s"File ${ignoredFiles += file.getName; file.getPath} could not be deleted.\nPlease delete this file manually, and purge the ignored file index.")
						}
						false
					}
					catch {case _ => true}
				)
					Thread.sleep(50)
			)

			Thread.sleep(50)
		}
		else
			Thread.sleep(2000)

	def loadIni =
	{
		if (!ini.exists) ini.createNewFile
		Files.readAllLines(Paths.get(ini.getPath)).toArray(Array[String]()).map(_.split("=")).foreach(s =>
			(s(0) match
			{
			case label if label.equalsIgnoreCase("monitor") => (monitor = Integer.parseInt(s(1)))^^
			case label if label.equalsIgnoreCase("folder") =>
				ssFolder = new File(s(1))
				if (!ssFolder.exists)
				{
					if (JOptionPane.showConfirmDialog(null, s"Directory: '${s(1)}' does not exist. Would you like to create it?") == JOptionPane.OK_OPTION) ssFolder.mkdirs
					else ssFolder = null
				} else if (!ssFolder.isDirectory)
				{
					JOptionPane.showConfirmDialog(null, s"Designated screenshot directory: '$ssFolder' is not a folder. Please fix settings to continue.")
					ssFolder = null
				}
			})^^
			{
				ssFolder = null
			})
		if (ssFolder == null) JOptionPane.showMessageDialog(null, "No valid screenshot folder defined in SSViewer.ini. Please add a 'folder=' record.")
	}
	def quit = {SystemTray.getSystemTray.remove(ico);System.exit(0)}
}
