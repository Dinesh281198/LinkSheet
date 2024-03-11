package fe.linksheet.module.resolver.urlresolver.base

import android.net.Uri
import fe.linksheet.extension.koin.injectLogger
import fe.linksheet.extension.unwrapOrNull
import fe.linksheet.module.database.entity.resolver.ResolverEntity
import fe.linksheet.module.log.internal.LoggerDelegate
import fe.linksheet.module.redactor.HashProcessor
import fe.linksheet.module.repository.resolver.ResolverRepository
import fe.linksheet.module.resolver.urlresolver.ResolveResultType
import fe.linksheet.util.Darknet
import fe.linksheet.util.HostUtil
import fe.linksheet.util.compat.CompatUriHost
import fe.linksheet.util.compat.compatHost
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass


typealias ResolvePredicate = (Uri) -> Boolean

abstract class UrlResolver<T : ResolverEntity<T>, R : Any>(
    clazz: KClass<R>,
    private val redirectResolver: ResolveRequest,
    private val resolverRepository: ResolverRepository<T>,
) : KoinComponent {
    private val logger by injectLogger(clazz)

    suspend fun resolve(
        uri: Uri,
        localCache: Boolean,
        builtInCache: Boolean,
        resolvePredicate: ResolvePredicate? = null,
        externalService: Boolean,
        connectTimeout: Int,
        canAccessInternet: Boolean,
        allowDarknets: Boolean,
    ): Result<ResolveResultType>? {
        if (resolvePredicate?.invoke(uri) == false) {
            return null
        }

        val darknet = Darknet.getOrNull(uri)
        val uriString = uri.toString()
        val uriLogContext = logger.createContext(uriString, HashProcessor.UrlProcessor)

        if (!allowDarknets && darknet != null) {
            logger.info(uriLogContext) { "$it is a darknet url, but darknets are not allowed, skipping" }
            return null
        }

        if (localCache) {
            val cached = resolverRepository.getForInputUrl(uriString)?.url
            if (cached != null) {
                logger.info(cached, HashProcessor.UrlProcessor) { "From local cache: $it" }
                return ResolveResultType.Resolved.LocalCache(cached).success()
            }
        }

        if (builtInCache) {
            val cached = resolverRepository.getBuiltInCachedForUrl(uriString)
            if (cached != null) {
                logger.info(cached, HashProcessor.UrlProcessor) { "From built-in cache: $it" }
                return ResolveResultType.Resolved.BuiltInCache(cached).success()
            }
        }

        if (!canAccessInternet) {
            return ResolveResultType.NoInternetConnection.success()
        }

        val resolveResult = resolve(uriString, uriLogContext, externalService, uri.compatHost, darknet, connectTimeout)

        if (localCache) {
            val url = resolveResult.unwrapOrNull<ResolveResultType, ResolveResultType.Resolved>()?.url
            if (url != null) {
                resolverRepository.insert(uriString, url)
            }
        }

        return resolveResult
    }

    private fun canResolveExternally(
        logContext: LoggerDelegate.RedactedParameter,
        host: CompatUriHost?,
        darknet: Darknet?,
    ): Boolean {
        if (darknet != null) {
            logger.info(logContext) { "$it is a darknet url, but external services are enabled, skipping" }
            return false
        }

        if (HostUtil.isAccessiblePublicly(host) == false) {
            logger.info(logContext) { "$it is not publicly accessible, falling back to local resolving" }
            return false
        }

        return true
    }

    private fun resolve(
        uriString: String,
        logContext: LoggerDelegate.RedactedParameter,
        externalService: Boolean,
        host: CompatUriHost?,
        darknet: Darknet?,
        timeout: Int,
    ): Result<ResolveResultType> {
        if (externalService && canResolveExternally(logContext, host, darknet)) {
            logger.info(logContext) { "Attempting to resolve $it via external service.." }
            val result = redirectResolver.resolveRemote(uriString, timeout, resolverRepository.remoteResolveUrlField)
            if (result.isFailure) logger.error("External resolve failed", result.exceptionOrNull())

            return result
        }

        logger.info(logContext) { "Using local service for $it" }
        val result = redirectResolver.resolveLocal(uriString, timeout)
        if (result.isFailure) logger.error("Local service resolve failed", result.exceptionOrNull())

        return result
    }
}
