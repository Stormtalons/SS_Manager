class Code[T <: Any](code: => T) extends Tools
{
	def % = () => code
	def ^^ : Unit = ^^()
	def ^^(_itBroke: => Unit = null): Unit = try code catch {case e: Exception => _itBroke}
	def x = new Thread(new Runnable{def run = code}).start
}