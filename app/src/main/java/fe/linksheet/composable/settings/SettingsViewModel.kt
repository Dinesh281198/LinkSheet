package fe.linksheet.composable.settings

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasomaniac.openwith.data.LinkSheetDatabase
import com.tasomaniac.openwith.preferred.PreferredResolver.resolve
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import fe.linksheet.BuildConfig
import fe.linksheet.extension.toDisplayActivityInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class SettingsViewModel : ViewModel(), KoinComponent {
    private val database by inject<LinkSheetDatabase>()

    val preferredApps = mutableStateListOf<Pair<String, DisplayActivityInfo?>>()
    val whichAppsCanHandleLinks = mutableStateListOf<DisplayActivityInfo>()

    fun loadPreferredApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            preferredApps.clear()
            preferredApps.addAll(database.preferredAppDao().allPreferredApps().map {
                it.host to it.resolve(context)
            })
        }
    }

    fun deletePreferredApp(host: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferredApps.removeIf { it.first == host }
            database.preferredAppDao().deleteHost(host)
        }
    }

    fun openDefaultBrowserSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }

    fun openOpenByDefaultSettings(context: Context, packageName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                Uri.parse("package:$packageName")
            )
            context.startActivity(intent)
            return true
        }

        return false
    }

    fun checkDefaultBrowser(context: Context): Boolean {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
        val resolveInfo =
            context.packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo?.activityInfo?.packageName == BuildConfig.APPLICATION_ID
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun loadAppsWhichCanHandleLinks(context: Context) {
        val manager = context.getSystemService(DomainVerificationManager::class.java)
        viewModelScope.launch(Dispatchers.IO) {
            whichAppsCanHandleLinks.clear()
            whichAppsCanHandleLinks.addAll(
                context.packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES)
                    .asSequence()
                    .mapNotNull { packageInfo ->
                        context.packageManager.queryIntentActivities(
                            Intent().setPackage(packageInfo.packageName), PackageManager.MATCH_ALL
                        ).firstOrNull() ?: return@mapNotNull null
                    }
                    .filter { resolveInfo ->
                        val state =
                            manager.getDomainVerificationUserState(resolveInfo.activityInfo.packageName)
                        state != null && state.isLinkHandlingAllowed && state.hostToStateMap.isNotEmpty() && state.hostToStateMap.any { it.value == DomainVerificationUserState.DOMAIN_STATE_VERIFIED }
                    }
                    .filter { it.activityInfo.packageName != BuildConfig.APPLICATION_ID }
                    .map { it.toDisplayActivityInfo(context) }
                    .sortedBy { it.displayLabel }
                    .toList()
            )
        }
    }

//    companion object {
//        const val PRIVATE_FLAG_HAS_DOMAIN_URLS = (1 shl 4)
//    }
//
//    @SuppressLint("DiscouragedPrivateApi")
//    private fun doesAppHandleLinks(context: Context, packageName: String): Boolean? {
//        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
//
//        val field = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            val fields =
//                HiddenApiBypass.getInstanceFields(ApplicationInfo::class.java) as List<Field>
//            fields.find { it.name == "privateFlags" }?.get(packageInfo.applicationInfo)
//        } else {
//            ApplicationInfo::class.java.getDeclaredField("privateFlags").apply {
//                this.isAccessible = true
//            }.get(packageInfo.applicationInfo)
//        }
//
//        return field?.let {
//            // https://android.googlesource.com/platform/frameworks/base/+/android-8.0.0_r4/cmds/pm/src/com/android/commands/pm/Pm.java#898
//            it.toString().toInt() and PRIVATE_FLAG_HAS_DOMAIN_URLS != 0
//        }
//    }
}