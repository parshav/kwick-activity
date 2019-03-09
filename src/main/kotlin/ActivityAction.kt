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
        private val currentActivityCommand = """
            adb shell dumpsys window windows | grep -E 'mFocusedApp' | cut -d ' ' -f 7
        """.trimIndent()
    }


    override fun actionPerformed(e: AnActionEvent) {
        runCommand(e)
    }


    private fun runCommand(event: AnActionEvent) {
        val sb = StringBuilder()
        try {
            val p = Runtime.getRuntime().exec(currentActivityCommand)
            val sc = Scanner(p.inputStream)
            while (sc.hasNext()) sb.append(sc.nextLine())

            if (sb.toString().trim().isNotBlank()) {
                popup(sb.toString().trim(), event = event)
            } else {
                popup("Could not find activity.", type = MessageType.WARNING, event = event)
            }

        } catch (e: Exception) {
            popup("Error running command.", type = MessageType.ERROR, event = event)
        }
    }

    private fun popup(text: String, type: MessageType = MessageType.INFO, event: AnActionEvent) {

        val statusBar = WindowManager.getInstance()
                .getStatusBar(DataKeys.PROJECT.getData(event.dataContext))

        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(5000)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.component), Balloon.Position.atRight)
    }
}