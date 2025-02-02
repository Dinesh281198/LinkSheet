package fe.linksheet.experiment.engine.resolver.followredirects

import fe.linksheet.experiment.engine.resolver.configureHeaders
import fe.linksheet.extension.ktor.isHtml
import fe.linksheet.extension.ktor.urlString
import fe.linksheet.module.resolver.urlresolver.redirect.RedirectResolveRequest.Companion.parseRefreshHeader
import fe.std.result.*
import io.ktor.client.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.saket.unfurl.extension.HtmlMetadataUnfurlerExtension


class FollowRedirectsLocalSource(private val client: HttpClient) : FollowRedirectsSource {

    override suspend fun resolve(urlString: String): IResult<FollowRedirectsResult> {
        val headResult = tryCatch {
            client.head(urlString = urlString) { configureHeaders() }
        }
        if (headResult.isFailure()) {
            return +headResult
        }

        val headResponse = headResult.value
        val headRefreshHeaderUrl = headResponse.handleRefreshHeader()
        if (headRefreshHeaderUrl != null) {
            return FollowRedirectsResult.RefreshHeader(headRefreshHeaderUrl).success
        }

        if (headResponse.status.value !in 400..499) {
            return FollowRedirectsResult.LocationHeader(headResponse.urlString()).success
        }

        val getResult = tryCatch {
            client.get(urlString = urlString) { configureHeaders() }
        }
        if (getResult.isFailure()) {
            return +getResult
        }

        val getResponse = getResult.value
        val getRefreshHeaderUrl = getResponse.handleRefreshHeader()
        if (getRefreshHeaderUrl != null) {
            return FollowRedirectsResult.RefreshHeader(getRefreshHeaderUrl).success
        }

        val htmlText = if (getResponse.isHtml()) getResponse.bodyAsText() else null
        return FollowRedirectsResult.GetRequest(getResponse.urlString(), htmlText).success
    }

    private fun HttpResponse.handleRefreshHeader(): String? {
        val refreshHeader = headers["refresh"] ?: return null
        val parsedHeader = parseRefreshHeader(refreshHeader) ?: return null

        return parsedHeader
            .takeIf { it.first == 0 }
            ?.takeIf { parseUrl(it.second) != null }
            ?.second
    }
}
