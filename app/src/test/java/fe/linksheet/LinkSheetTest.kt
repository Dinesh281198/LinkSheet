package fe.linksheet

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

interface LinkSheetTest : KoinTest {
    fun getContext(): Context {
        return ApplicationProvider.getApplicationContext<Context>()
    }

    @After
    fun teardown() {
        stopKoin()
    }
}
