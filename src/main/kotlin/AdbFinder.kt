import com.intellij.openapi.project.Project
import org.jetbrains.android.sdk.AndroidSdkUtils

object AdbFinder {

    fun isAdbInstalled() = AndroidSdkUtils.isAndroidSdkAvailable()

    fun adbPath(project: Project): String {
        var adbPath = ""
        val adbFile = AndroidSdkUtils.getAdb(project)
        if (adbFile != null) {
            adbPath = adbFile.absolutePath
        }
        return adbPath
    }
}