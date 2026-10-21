package com.example.appspace

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Process
import androidx.annotation.Keep
import java.io.File
import java.io.FileOutputStream

/**
 * Shizuku UserService. It runs with the shell identity provided by Shizuku.
 * Uses a small manual Binder protocol so the project does not need an AIDL file.
 */
@Keep
class ShizukuInstallService : Binder() {

    init {
        attachInterface(null, DESCRIPTOR)
    }

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        when (code) {
            IBinder.INTERFACE_TRANSACTION -> {
                reply?.writeString(DESCRIPTOR)
                return true
            }
            TRANSACTION_INSTALL_APK -> {
                data.enforceInterface(DESCRIPTOR)
                val apkFd = data.readTypedObject(ParcelFileDescriptor.CREATOR)
                    ?: throw IllegalArgumentException("没有收到 APK 文件描述符")
                val result = installApk(apkFd)
                reply?.writeNoException()
                reply?.writeString(result)
                return true
            }
            TRANSACTION_SET_DEFAULT_INSTALLER -> {
                data.enforceInterface(DESCRIPTOR)
                val enabled = data.readInt() != 0
                val result = setDefaultInstaller(enabled)
                reply?.writeNoException()
                reply?.writeString(result)
                return true
            }
            TRANSACTION_DESTROY -> {
                // Shizuku UserService reserved destroy transaction.
                runCatching { data.enforceInterface(DESCRIPTOR) }
                reply?.writeNoException()
                Thread { System.exit(0) }.start()
                return true
            }
            else -> return super.onTransact(code, data, reply, flags)
        }
    }

    private fun installApk(apkFileDescriptor: ParcelFileDescriptor): String {
        var stagedApk: File? = null
        return try {
            val tempDirectory = File("/data/local/tmp")
            if (!tempDirectory.exists() || !tempDirectory.isDirectory) {
                return "ERROR|无法访问 /data/local/tmp"
            }

            val outputFile = File.createTempFile("appspace-install-", ".apk", tempDirectory)
            stagedApk = outputFile
            ParcelFileDescriptor.AutoCloseInputStream(apkFileDescriptor).use { input ->
                FileOutputStream(outputFile).use { output -> input.copyTo(output) }
            }

            val process = ProcessBuilder("/system/bin/pm", "install", "-r", outputFile.absolutePath)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            val exitCode = process.waitFor()

            if (exitCode == 0 && output.contains("Success", ignoreCase = true) &&
                !output.contains("Failure", ignoreCase = true)) {
                "SUCCESS|${output.ifBlank { "Success" }}"
            } else {
                "ERROR|${output.ifBlank { "pm install 失败，退出码 $exitCode" }}"
            }
        } catch (t: Throwable) {
            "ERROR|${t.message ?: t.javaClass.simpleName}"
        } finally {
            runCatching { stagedApk?.delete() }
        }
    }

    /**
     * Uses the shell identity provided by Shizuku to register MainActivity as a preferred APK handler.
     * Android/OEM restrictions can still reject this; the UI reports the actual result.
     */
    private fun setDefaultInstaller(enabled: Boolean): String {
        return try {
            val serviceManager = Class.forName("android.os.ServiceManager")
            val packageBinder = serviceManager.getMethod("getService", String::class.java)
                .invoke(null, "package") as? IBinder
                ?: return "ERROR|无法连接 Android PackageManager 服务"
            val stubClass = Class.forName("android.content.pm.IPackageManager\$Stub")
            val packageManager = stubClass.getMethod("asInterface", IBinder::class.java)
                .invoke(null, packageBinder)
                ?: return "ERROR|无法取得 IPackageManager 接口"
            val packageManagerInterface = Class.forName("android.content.pm.IPackageManager")
            val packageName = "com.example.appspace"
            val mainComponent = ComponentName(packageName, "$packageName.MainActivity")
            val userId = Process.myUid() / 100000

            val clearMethod = packageManagerInterface.methods.firstOrNull {
                it.name == "clearPackagePreferredActivities" && it.parameterTypes.size == 1
            } ?: return "ERROR|当前系统不支持清除默认 APK 处理器"

            if (!enabled) {
                clearMethod.invoke(packageManager, packageName)
                return "SUCCESS|已清除 AppSpace 的默认 APK 处理器优先级。若仍被系统锁定，请到系统默认应用设置中手动切换。"
            }

            val actions = listOf(Intent.ACTION_VIEW, Intent.ACTION_INSTALL_PACKAGE)
            var successfulActions = 0
            val failures = mutableListOf<String>()
            for (action in actions) {
                try {
                    val intent = Intent(action).apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        setDataAndType(Uri.parse("content://appspace.local/default.apk"), APK_MIME_TYPE)
                    }
                    val queryMethod = packageManagerInterface.methods.firstOrNull {
                        it.name == "queryIntentActivities" && it.parameterTypes.size == 4
                    } ?: throw IllegalStateException("当前系统找不到 queryIntentActivities API")
                    val flagValue: Any = if (queryMethod.parameterTypes[2] == java.lang.Long.TYPE) 0x00010000L else 0x00010000
                    val queryResult = queryMethod.invoke(packageManager, intent, APK_MIME_TYPE, flagValue, userId)
                    val resolvedList = queryResult?.javaClass?.methods?.firstOrNull {
                        it.name == "getList" && it.parameterTypes.isEmpty()
                    }?.invoke(queryResult) as? List<*> ?: emptyList<Any>()
                    val components = resolvedList.mapNotNull { item ->
                        if (item == null) return@mapNotNull null
                        val activityInfo = runCatching { item.javaClass.getField("activityInfo").get(item) }.getOrNull() ?: return@mapNotNull null
                        val resolvedPackage = runCatching { activityInfo.javaClass.getField("packageName").get(activityInfo) as? String }.getOrNull() ?: return@mapNotNull null
                        val resolvedName = runCatching { activityInfo.javaClass.getField("name").get(activityInfo) as? String }.getOrNull() ?: return@mapNotNull null
                        ComponentName(resolvedPackage, resolvedName)
                    }.distinct()

                    if (components.none { it == mainComponent }) {
                        throw IllegalStateException("MainActivity 尚未被系统识别为 APK 处理器；请先把 APK intent-filter 合并进 AndroidManifest.xml")
                    }

                    // Do not clear every preferred activity owned by competing installer packages.
                    // removeExisting=true below replaces the matching APK-handler preference without
                    // resetting unrelated defaults (for example browser or link handling).

                    val filter = IntentFilter().apply {
                        addAction(action)
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addDataType(APK_MIME_TYPE)
                    }
                    val match = 0x00600000 or 0x0000FFFF
                    val namesArray = components.toTypedArray()
                    val addMethod = packageManagerInterface.methods.filter {
                        it.name == "addPreferredActivity" && (it.parameterTypes.size == 5 || it.parameterTypes.size == 6)
                    }.maxByOrNull { it.parameterTypes.size }
                        ?: throw IllegalStateException("当前系统不支持设置默认 APK 处理器")
                    val args = if (addMethod.parameterTypes.size == 6) {
                        arrayOf(filter, match, namesArray, mainComponent, userId, true)
                    } else {
                        arrayOf(filter, match, namesArray, mainComponent, userId)
                    }
                    addMethod.invoke(packageManager, *args)
                    successfulActions++
                } catch (e: Throwable) {
                    failures += "$action: ${e.cause?.message ?: e.message ?: e.javaClass.simpleName}"
                }
            }
            if (successfulActions == actions.size) {
                "SUCCESS|已请求将 AppSpace 设为默认 APK 处理器。部分厂商系统仍可能阻止默认选择。"
            } else {
                "ERROR|默认安装器设置不完整（成功 $successfulActions/${actions.size}）；${failures.joinToString("；")}"
            }
        } catch (t: Throwable) {
            "ERROR|默认安装器设置失败：${t.cause?.message ?: t.message ?: t.javaClass.simpleName}"
        }
    }

    companion object {
        const val DESCRIPTOR = "com.example.appspace.ShizukuInstallService"
        const val TRANSACTION_INSTALL_APK = IBinder.FIRST_CALL_TRANSACTION
        const val TRANSACTION_SET_DEFAULT_INSTALLER = IBinder.FIRST_CALL_TRANSACTION + 1
        const val TRANSACTION_DESTROY = 16777114
        private const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}