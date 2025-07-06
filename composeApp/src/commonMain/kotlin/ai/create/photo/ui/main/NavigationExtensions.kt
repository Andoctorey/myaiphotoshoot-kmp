package ai.create.photo.ui.main

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

fun NavHostController.navigateSingleTopTo(route: String) = navigate(route) {
    // Pop up to the start destination of the graph to
    // avoid building up a large stack of destinations
    // on the back stack as users select items
    popUpTo(graph.findStartDestination().route!!) {
        saveState = true
    }
    // Avoid multiple copies of the same destination when
    // re-selecting the same item
    launchSingleTop = true
    // Restore state when re-selecting a previously selected item
    restoreState = true
} 