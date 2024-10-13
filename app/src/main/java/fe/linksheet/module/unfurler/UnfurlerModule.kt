package fe.linksheet.module.unfurler

import me.saket.unfurl.Unfurler
import org.koin.dsl.module

val UnfurlerModule = module {
    single<Unfurler> { Unfurler(httpClient = get()) }
}
