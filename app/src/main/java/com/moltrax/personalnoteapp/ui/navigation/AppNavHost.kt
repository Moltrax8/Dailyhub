package com.moltrax.personalnoteapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moltrax.personalnoteapp.ui.screen.aicoach.AiCoachScreen
import com.moltrax.personalnoteapp.ui.screen.auth.LoginScreen
import com.moltrax.personalnoteapp.ui.screen.focus.FocusScreen
import com.moltrax.personalnoteapp.ui.screen.food.FoodScannerScreen
import com.moltrax.personalnoteapp.ui.screen.home.HomeScreen
import com.moltrax.personalnoteapp.ui.screen.settings.SettingsScreen
import com.moltrax.personalnoteapp.ui.screen.task.TaskDetailScreen
import com.moltrax.personalnoteapp.ui.screen.vault.VaultScreen
import com.moltrax.personalnoteapp.ui.screen.workout.LiveWorkoutScreen
import com.moltrax.personalnoteapp.ui.screen.workout.WorkoutDetailScreen
import com.moltrax.personalnoteapp.ui.screen.workout.WorkoutScreen

@Composable
fun AppNavHost(startDestination: Any = Login) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = startDestination) {
        composable<Login>         { LoginScreen(nav) }
        composable<Home>          { HomeScreen(nav) }
        composable<TaskDetail>    { entry -> TaskDetailScreen(nav, entry.toRoute<TaskDetail>().taskId) }
        composable<Settings>      { SettingsScreen(nav) }
        composable<FocusTimer>    { entry -> FocusScreen(nav, entry.toRoute<FocusTimer>().taskId) }
        composable<Vault>         { VaultScreen(nav) }
        composable<WorkoutList>   { WorkoutScreen(nav) }
        composable<WorkoutDetail> { entry -> WorkoutDetailScreen(nav, entry.toRoute<WorkoutDetail>().groupId) }
        composable<LiveWorkout>   { entry ->
            val r = entry.toRoute<LiveWorkout>()
            LiveWorkoutScreen(nav, r.workoutId, r.groupId)
        }
        composable<AiCoach>       { entry -> AiCoachScreen(nav, entry.toRoute<AiCoach>().sessionId) }
        composable<FoodScanner>   { FoodScannerScreen(nav) }
    }
}
