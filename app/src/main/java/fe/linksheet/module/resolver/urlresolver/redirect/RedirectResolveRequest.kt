package fe.linksheet.module.resolver.urlresolver.redirect

import fe.httpkt.Request
import fe.linksheet.LinkSheetAppConfig
import fe.linksheet.extension.koin.single
import fe.linksheet.module.log.impl.hasher.HashProcessor
import fe.linksheet.module.log.impl.Logger
import fe.linksheet.module.resolver.urlresolver.CachedRequest
import fe.linksheet.module.resolver.urlresolver.base.ResolveRequest
import org.koin.dsl.module
import java.io.IOException

val redirectResolveRequestModule = module {
    single<RedirectResolveRequest, Request, CachedRequest> { _, request, cachedRequest ->
        RedirectResolveRequest(
            "${LinkSheetAppConfig.supabaseHost()}/redirector",
            LinkSheetAppConfig.supabaseApiKey(),
            request, cachedRequest, singletonLogger
        )
    }
}

class RedirectResolveRequest(
    apiUrl: String,
    token: String,
    request: Request,
    private val urlResolverCache: CachedRequest,
    logger: Logger
) : ResolveRequest(apiUrl, token, request, logger, "redirect") {
    @Throws(IOException::class)
    override fun resolveLocal(url: String, timeout: Int): String {
        val con = urlResolverCache.head(url, timeout, true)
        logger.debug({ "ResolveLocal $it" }, url, HashProcessor.StringProcessor)

        val response = if (con.responseCode in 400..499) {
            urlResolverCache.get(url, timeout, true)
        } else con

        return response.url.toString()
    }
}
