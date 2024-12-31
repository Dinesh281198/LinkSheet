package fe.linksheet.module.resolver

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import fe.linksheet.extension.android.queryResolveInfosByIntent
import fe.linksheet.extension.android.toDisplayActivityInfos
import fe.linksheet.extension.android.toPackageKeyedMap

class BrowserResolver(val packageManager: PackageManager) {
    companion object {
        private val httpSchemeUri: Uri = Uri.fromParts("http", "", "")
        private val httpsSchemeUri: Uri = Uri.fromParts("https", "", "")

        private val baseBrowserIntent = Intent()
            .setAction(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)

        val httpBrowserIntent = Intent(baseBrowserIntent).setData(httpSchemeUri)
        val httpsBrowserIntent = Intent(baseBrowserIntent).setData(httpsSchemeUri)

        fun isSchemeTypicallySupportedByBrowsers(intent: Intent): Boolean {
            return httpSchemeUri.scheme == intent.scheme || httpsSchemeUri.scheme == intent.scheme
        }
    }

    fun queryDisplayActivityInfoBrowsers(sorted: Boolean): List<DisplayActivityInfo> {
        return queryBrowsers().toDisplayActivityInfos(packageManager, sorted)
    }

    fun queryBrowsers(): Map<String, ResolveInfo> {
        // Some apps (looking at you Coinbase) declare their app as a browser (via CATEGORY_BROWSABLE and ACTION_VIEW) but ONLY for HTTPS schemes.
        // Previously, LinkSheet assumed (rightfully so) that an app, which is declared as a BROWSER, could handle HTTP (alongside HTTPS and other schemes).
        // Since this appears to NOT be the case, we have to send two intent queries (one for HTTP and one for HTTPS), then de-dupe them using a map
        return queryBrowsers(httpBrowserIntent) + queryBrowsers(httpsBrowserIntent)
    }

    private fun queryBrowsers(intent: Intent): Map<String, ResolveInfo> {
        return packageManager.queryResolveInfosByIntent(intent, true).toPackageKeyedMap()
    }
}


