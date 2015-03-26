import java.awt.Image
import java.awt.datatransfer.{DataFlavor, Transferable}

class ImageTransferable(i: Image) extends Transferable
{
	def getTransferDataFlavors = Array[DataFlavor](DataFlavor.imageFlavor)
	def isDataFlavorSupported(flavor: DataFlavor) = flavor eq DataFlavor.imageFlavor
	def getTransferData(flavor: DataFlavor) = i
}