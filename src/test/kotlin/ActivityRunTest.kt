import org.junit.Test
import java.util.*

class ActivityRunTest {

    companion object {

    }

    @Test
    fun `exec test`() {

        val a = AdbFinder.isAdbInstalled()
        println("Printing")
        println(a)
    }
}