package com.example.skillmesh.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skillmesh.SkillMeshApp
import com.example.skillmesh.theme.AccentPrimary
import com.example.skillmesh.ui.discover.DiscoverScreen
import com.example.skillmesh.ui.discover.DiscoverViewModel
import com.example.skillmesh.ui.home.HomeScreen
import com.example.skillmesh.ui.home.HomeViewModel
import com.example.skillmesh.ui.nearby.NearbyScreen
import com.example.skillmesh.ui.nearby.NearbyViewModel
import com.example.skillmesh.ui.profile.ProfileScreen
import com.example.skillmesh.ui.profile.ProfileViewModel
import com.example.skillmesh.ui.projects.ProjectsScreen
import com.example.skillmesh.ui.projects.ProjectsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Discover : Screen("discover", "Discover", Icons.Default.Search)
    object Nearby : Screen("nearby", "Radar", Icons.Default.Sensors)
    object Projects : Screen("projects", "Projects", Icons.Default.Work)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Discover,
    Screen.Nearby,
    Screen.Projects,
    Screen.Profile
)

@Composable
fun MainNavigation(app: SkillMeshApp) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Create ViewModels backed by singletons
    val homeViewModel = HomeViewModel(app.repository, app.aiEngine, app.nearbyManager)
    val discoverViewModel = DiscoverViewModel(app.repository, app.aiEngine)
    val nearbyViewModel = NearbyViewModel(app.nearbyManager, app.repository)
    val projectsViewModel = ProjectsViewModel(app.repository, app.aiEngine)
    val profileViewModel = ProfileViewModel(app.repository, app.aiEngine)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPrimary,
                            selectedTextColor = AccentPrimary,
                            indicatorColor = AccentPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDiscover = { navController.navigate(Screen.Discover.route) },
                    onNavigateToNearby = { navController.navigate(Screen.Nearby.route) },
                    onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Discover.route) {
                DiscoverScreen(viewModel = discoverViewModel)
            }
            composable(Screen.Nearby.route) {
                NearbyScreen(viewModel = nearbyViewModel)
            }
            composable(Screen.Projects.route) {
                ProjectsScreen(viewModel = projectsViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
