package io.agora.meeting.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dalvik.system.DexFile
import io.agora.meeting.ui.BuildConfig
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.regex.Pattern
import kotlin.jvm.Throws

object ClassUtils {
    private val TAG = ClassUtils::class.java.simpleName
    private const val EXTRACTED_NAME_EXT = ".classes"
    private const val EXTRACTED_SUFFIX = ".zip"

    private val SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes"

    private const val PREFS_FILE = "multidex.version"
    private const val KEY_DEX_NUMBER = "dex.number"

    private const val VM_WITH_MULTIDEX_VERSION_MAJOR = 2
    private const val VM_WITH_MULTIDEX_VERSION_MINOR = 1

    private fun getMultiDexPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_FILE, if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) Context.MODE_PRIVATE else Context.MODE_PRIVATE or Context.MODE_MULTI_PROCESS)
    }

    /**
     * By specifying the package name, scan all ClassName contained under the package
     *
     * @param context
     * @param packageName
     * @return Collection of all classes
     */
    @Throws(PackageManager.NameNotFoundException::class, IOException::class, InterruptedException::class)
    fun getFileNameByPackageName(context: Context, packageName: String): Set<String>? {
        val classNames: MutableSet<String> = HashSet()
        val paths = getSourcePaths(context)
        val parserCtl = CountDownLatch(paths.size)
        for (path in paths) {
            DefaultPoolExecutor.getInstance().execute(Runnable {
                var dexfile: DexFile? = null
                try {
                    dexfile = if (path.endsWith(EXTRACTED_SUFFIX)) {
                        //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                        DexFile.loadDex(path, "$path.tmp", 0)
                    } else {
                        DexFile(path)
                    }
                    val dexEntries = dexfile!!.entries()
                    while (dexEntries.hasMoreElements()) {
                        val className = dexEntries.nextElement()
                        if (className.startsWith(packageName)) {
                            classNames.add(className)
                        }
                    }
                } catch (ignore: Throwable) {
                    Log.e("ARouter", "Scan map file in dex files made error.", ignore)
                } finally {
                    if (null != dexfile) {
                        try {
                            dexfile.close()
                        } catch (ignore: Throwable) {
                        }
                    }
                    parserCtl.countDown()
                }
            })
        }
        parserCtl.await()
        Log.d(TAG, "Filter " + classNames.size + " classes by packageName <" + packageName + ">")
        return classNames
    }

    /**
     * get all the dex path
     *
     * @param context the application context
     * @return all the dex path
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    @Throws(PackageManager.NameNotFoundException::class, IOException::class)
    fun getSourcePaths(context: Context): List<String> {
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
        val sourceApk = File(applicationInfo.sourceDir)
        val sourcePaths: MutableList<String> = ArrayList()
        sourcePaths.add(applicationInfo.sourceDir) //add the default apk path

        //the prefix of extracted file, ie: test.classes
        val extractedFilePrefix = sourceApk.name + EXTRACTED_NAME_EXT
        /** If MultiDex already supported by VM, we will not to load Classesx.zip from
         * Secondary Folder, because there is none. */
        if (!isVMMultidexCapable()) {
            //the total dex numbers
            val totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1)
            val dexDir = File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME)
            for (secondaryNumber in 2..totalDexNumber) {
                //for each dex file, ie: test.classes2.zip, test.classes3.zip...
                val fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX
                val extractedFile = File(dexDir, fileName)
                if (extractedFile.isFile) {
                    sourcePaths.add(extractedFile.absolutePath)
                    //we ignore the verify zip part
                } else {
                    throw IOException("Missing extracted secondary dex file '" + extractedFile.path + "'")
                }
            }
        }
        if (BuildConfig.DEBUG) { // Search instant run support only debuggable
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo)!!)
        }
        return sourcePaths
    }

    /**
     * Get instant run dex path, used to catch the branch usingApkSplits=false.
     */
    private fun tryLoadInstantRunDexFile(applicationInfo: ApplicationInfo): List<String>? {
        val instantRunSourcePaths: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
            // add the split apk, normally for InstantRun, and newest version.
            instantRunSourcePaths.addAll(Arrays.asList(*applicationInfo.splitSourceDirs))
            Log.d(TAG, "Found InstantRun support")
        } else {
            try {
                // This man is reflection from Google instant run sdk, he will tell me where the dex files go.
                val pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths")
                val getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String::class.java)
                val instantRunDexPath = getDexFileDirectory.invoke(null, applicationInfo.packageName) as String
                val instantRunFilePath = File(instantRunDexPath)
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory) {
                    val dexFile = instantRunFilePath.listFiles()
                    for (file in dexFile) {
                        if (null != file && file.exists() && file.isFile && file.name.endsWith(".dex")) {
                            instantRunSourcePaths.add(file.absolutePath)
                        }
                    }
                    Log.d(TAG, "Found InstantRun support")
                }
            } catch (e: Exception) {
                Log.e(TAG, "InstantRun support error, " + e.message)
            }
        }
        return instantRunSourcePaths
    }

    /**
     * Identifies if the current VM has a native support for multidex, meaning there is no need for
     * additional installation by this library.
     *
     * @return true if the VM handles multidex
     */
    private fun isVMMultidexCapable(): Boolean {
        var isMultidexCapable = false
        var vmName: String? = null
        try {
            if (isYunOS()) {    // YunOS need special judgment
                vmName = "'YunOS'"
                isMultidexCapable = Integer.valueOf(System.getProperty("ro.build.version.sdk")) >= 21
            } else {    // Native Android system
                vmName = "'Android'"
                val versionString = System.getProperty("java.vm.version")
                if (versionString != null) {
                    val matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString)
                    if (matcher.matches()) {
                        try {
                            val major = matcher.group(1).toInt()
                            val minor = matcher.group(2).toInt()
                            isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR
                                    || (major == VM_WITH_MULTIDEX_VERSION_MAJOR
                                    && minor >= VM_WITH_MULTIDEX_VERSION_MINOR))
                        } catch (ignore: NumberFormatException) {
                            // let isMultidexCapable be false
                        }
                    }
                }
            }
        } catch (ignore: Exception) {
        }
        Log.i(TAG, "VM with name " + vmName + if (isMultidexCapable) " has multidex support" else " does not have multidex support")
        return isMultidexCapable
    }

    /**
     * Determine whether the system is a YunOS system
     */
    private fun isYunOS(): Boolean {
        return try {
            val version = System.getProperty("ro.yunos.version")
            val vmName = System.getProperty("java.vm.name")
            (vmName != null && vmName.toLowerCase().contains("lemur")
                    || version != null && version.trim { it <= ' ' }.length > 0)
        } catch (ignore: Exception) {
            false
        }
    }
}