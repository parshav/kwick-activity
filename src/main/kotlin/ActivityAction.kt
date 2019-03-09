import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.awt.RelativePoint
import java.util.*


class ActivityAction : AnAction() {

    companion object {

        private const val errorNoActivity = "Could not find activity."

        private fun currentActivityCommand(adb: String) = """
           $adb shell dumpsys window windows | grep -E 'mFocusedApp' | cut -d ' ' -f 7
        """.trimIndent()
    }


    private val adbPath by lazy {
        if (AdbFinder.isAdbInstalled()) {
            AdbFinder.adbPath(event.project!!)
        } else ""
    }

    lateinit var event: AnActionEvent

    override fun actionPerformed(e: AnActionEvent) {
        event = e
        runWithAdb()
    }

    private fun runWithAdb() {

        if (adbPath.isNotBlank()) {

            currentActivityCommand(adbPath).exect {

                when (it.type) {

                    MessageType.INFO -> popup(it.message, it.type)

                    MessageType.WARNING -> popup(errorNoActivity, it.type)

                    MessageType.ERROR -> popup("Error running adb command : ${it.message}", it.type)
                }
            }

        } else {
            popup("Could not find adb.")
        }
    }

    fun String.exect(block: (Result) -> Unit) {
        val sb = StringBuilder()
        try {

            val p = Runtime.getRuntime().exec(this)
            val sc = Scanner(p.inputStream)
            while (sc.hasNext()) sb.append(sc.nextLine())

            if (sb.toString().trim().isNotBlank()) {
                block.invoke(Result(sb.toString().trim(), MessageType.INFO))
            } else {
                block.invoke(Result("Empty adb Activity return.", MessageType.WARNING))
            }

        } catch (e: Exception) {
            block.invoke(Result("${e.message}", MessageType.WARNING))
        }
    }


    data class Result(val message: String, val type: MessageType)

    private fun popup(text: String, type: MessageType = MessageType.INFO) {

        val statusBar = WindowManager.getInstance()
                .getStatusBar(DataKeys.PROJECT.getData(event.dataContext))

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight)
    }
}