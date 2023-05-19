package fe.linksheet.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fe.linksheet.*
import fe.linksheet.composable.main.MainRoute
import fe.linksheet.composable.settings.SettingsRoute
import fe.linksheet.composable.settings.SettingsViewModel
import fe.linksheet.composable.settings.about.AboutSettingsRoute
import fe.linksheet.composable.settings.about.CreditsSettingsRoute
import fe.linksheet.composable.settings.apps.AppsSettingsRoute
import fe.linksheet.composable.settings.browser.inapp.InAppBrowserSettingsRoute
import fe.linksheet.composable.settings.bottomsheet.BottomSheetSettingsRoute
import fe.linksheet.composable.settings.browser.mode.PreferredBrowserSettingsRoute
import fe.linksheet.composable.settings.apps.link.AppsWhichCanOpenLinksSettingsRoute
import fe.linksheet.composable.settings.apps.preferred.PreferredAppSettingsRoute
import fe.linksheet.composable.settings.browser.BrowserSettingsRoute
import fe.linksheet.composable.settings.link.LibRedirectServiceSettingsRoute
import fe.linksheet.composable.settings.link.LibRedirectSettingsRoute
import fe.linksheet.composable.settings.link.LinksSettingsRoute
import fe.linksheet.composable.settings.theme.ThemeSettingsRoute
import fe.linksheet.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val settingsViewModel = koinViewModel<SettingsViewModel>()

            AppTheme(theme = settingsViewModel.theme.value) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(modifier = Modifier.height(5.dp))

                            NavHost(
                                navController = navController,
                                startDestination = mainRoute,
                            ) {
                                composable(route = mainRoute) {
                                    MainRoute(navController = navController)
                                }

                                val onBackPressed: () -> Unit = { navController.popBackStack() }
                                composable(route = settingsRoute) {
                                    SettingsRoute(
                                        navController = navController,
                                        onBackPressed = onBackPressed
                                    )
                                }

                                composable(route = appsSettingsRoute) {
                                    AppsSettingsRoute(
                                        navController = navController,
                                        onBackPressed = onBackPressed
                                    )
                                }

                                composable(route = browserSettingsRoute) {
                                    BrowserSettingsRoute(
                                        navController = navController,
                                        onBackPressed = onBackPressed
                                    )
                                }

                                composable(route = bottomSheetSettingsRoute) {
                                    BottomSheetSettingsRoute(
                                        onBackPressed = onBackPressed,
                                    )
                                }

                                composable(route = linksSettingsRoute) {
                                    LinksSettingsRoute(
                                        onBackPressed = onBackPressed,
                                        navController = navController,
                                    )
                                }

                                composable(route = libRedirectSettingsRoute) {
                                    LibRedirectSettingsRoute(
                                        onBackPressed = onBackPressed,
                                        navController = navController,
                                    )
                                }

                                composable(route = libRedirectServiceSettingsRoute) {
                                    it.arguments?.getString("service")?.let { service ->
                                        LibRedirectServiceSettingsRoute(
                                            onBackPressed = onBackPressed,
                                            serviceKey = service,
                                            viewModel = settingsViewModel
                                        )
                                    }
                                }

                                composable(route = themeSettingsRoute) {
                                    ThemeSettingsRoute(
                                        onBackPressed = onBackPressed,
                                        viewModel = settingsViewModel
                                    )
                                }

                                composable(route = aboutSettingsRoute) {
                                    AboutSettingsRoute(
                                        navController = navController,
                                        onBackPressed = onBackPressed
                                    )
                                }

                                composable(route = creditsSettingsRoute) {
                                    CreditsSettingsRoute(onBackPressed = onBackPressed)
                                }

                                composable(route = preferredBrowserSettingsRoute) {
                                    PreferredBrowserSettingsRoute(
                                        onBackPressed = onBackPressed,
                                        settingsViewModel = settingsViewModel
                                    )
                                }

                                composable(route = inAppBrowserSettingsRoute) {
                                    InAppBrowserSettingsRoute(
                                        onBackPressed = onBackPressed,
                                        settingsViewModel = settingsViewModel
                                    )
                                }

                                composable(route = preferredAppsSettingsRoute) {
                                    PreferredAppSettingsRoute(
                                        onBackPressed = onBackPressed,
                                        settingsViewModel = settingsViewModel
                                    )
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    composable(route = appsWhichCanOpenLinksSettingsRoute) {
                                        AppsWhichCanOpenLinksSettingsRoute(
                                            onBackPressed = onBackPressed,
                                            settingsViewModel = settingsViewModel
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}