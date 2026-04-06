package com.solarsensear.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.solarsensear.ui.screens.ar.ARScreen
import com.solarsensear.ui.screens.ar.SetupSheet
import com.solarsensear.ui.screens.calculator.CalculatorScreen
import com.solarsensear.ui.screens.home.HomeScreen
import com.solarsensear.ui.screens.login.LoginScreen
import com.solarsensear.ui.screens.onboarding.OnboardingScreen
import com.solarsensear.ui.screens.profile.ProfileScreen
import com.solarsensear.ui.screens.report.ReportScreen
import com.solarsensear.ui.screens.reports.ReportsListScreen
import com.solarsensear.ui.screens.splash.SplashScreen
import com.solarsensear.ui.screens.vendors.VendorsScreen

@Composable
fun AppNavGraph(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScaffold(
                rootNavController = navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }

        composable(
            route = Screen.Report.route,
            arguments = listOf(navArgument("reportIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val reportIndex = backStackEntry.arguments?.getInt("reportIndex") ?: 0
            ReportScreen(
                reportIndex = reportIndex,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AR.route,
            arguments = listOf(
                navArgument("panelCount") { type = NavType.IntType },
                navArgument("roofType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val panelCount = backStackEntry.arguments?.getInt("panelCount") ?: 12
            val roofType = backStackEntry.arguments?.getString("roofType") ?: "flat"

            // In a real implementation, these would come from the setup flow saved state
            ARScreen(
                panelCount = panelCount,
                roofType = roofType,
                locationName = "Nagpur",
                monthlyBillInr = 3000.0,
                onBack = { navController.popBackStack() },
                onCapture = { _ ->
                    navController.navigate(Screen.Report.createRoute(0)) {
                        popUpTo(Screen.Main.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Vendors.route) {
            VendorsScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainScaffold(
    rootNavController: NavHostController,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            composable(BottomNavItem.Home.route) {
                var showSetupSheet by remember { mutableStateOf(false) }

                HomeScreen(
                    onStartScan = { showSetupSheet = true },
                    onReportClick = { index ->
                        rootNavController.navigate(Screen.Report.createRoute(index))
                    }
                )

                if (showSetupSheet) {
                    SetupSheet(
                        onDismiss = { showSetupSheet = false },
                        onStartScan = { panelCount, roofType, location, bill ->
                            showSetupSheet = false
                            rootNavController.navigate(Screen.AR.createRoute(panelCount, roofType))
                        }
                    )
                }
            }

            composable(BottomNavItem.Calculator.route) {
                CalculatorScreen()
            }

            composable(BottomNavItem.Reports.route) {
                ReportsListScreen(
                    onReportClick = { index ->
                        rootNavController.navigate(Screen.Report.createRoute(index))
                    }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
