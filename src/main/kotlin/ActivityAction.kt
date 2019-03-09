import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import java.util.*


class ActivityAction : AnAction() {

    companion object {

        private const val `notification title` = "kwick Activity"
        private const val errorNoActivity = "Could not find activity."

        private val NOTIFICATION_GROUP = NotificationGroup.balloonGroup(`notification title`)

        private fun currentActivityCommand(adb: String) = """
           $adb shell dumpsys window windows | grep -E 'mFocusedApp' | cut -d ' ' -f 7
        """.trimIndent()

    }


    private val adbPath by lazy {
        if (AdbFinder.isAdbInstalled()) {
            AdbFinder.adbPath(event.project!!)
        } else ""
    }

    private lateinit var event: AnActionEvent

    override fun actionPerformed(e: AnActionEvent) {
        event = e
        runWithAdb()
    }

    private fun runWithAdb() {

        if (adbPath.isNotBlank()) {

            val r = Runnable {
                currentActivityCommand(adbPath).exect {

                    when (it.type) {

                        NotificationType.INFORMATION -> popup(it.message, it.type)

                        NotificationType.WARNING -> popup(errorNoActivity, it.type)

                        NotificationType.ERROR -> popup("Error running adb command : ${it.message}", it.type)
                    }
                }
            }
            r.run()

        } else {
            popup("Could not find adb.", NotificationType.WARNING)
        }
    }

    fun String.exect(block: (Result) -> Unit) {
        val sb = StringBuilder()
        try {

            val p = Runtime.getRuntime().exec(this)
            p.waitFor()

            val sc = Scanner(p.inputStream)
            while (sc.hasNext()) sb.append(sc.nextLine())

            if (sb.toString().trim().isNotBlank()) {
                block.invoke(Result(sb.toString().trim(), NotificationType.INFORMATION))
            } else {
                block.invoke(Result("Empty adb Activity return.", NotificationType.WARNING))
            }

        } catch (e: Exception) {
            block.invoke(Result("${e.message}", NotificationType.ERROR))
        }
    }


    data class Result(val message: String, val type: NotificationType)


    fun popup(message: String, type: NotificationType) {


        ApplicationManager.getApplication().invokeLater {
            val notification = NOTIFICATION_GROUP.createNotification(`notification title`, message, type, null)
            Notifications.Bus.notify(notification)
        }
    }
}