/*
 * Chronal: Metronome app for Android
 * Copyright (C) 2026  cognitivity
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.cognitivity.chronal.ui.settings

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.cognitivity.chronal.ChronalApp.Companion.context
import dev.cognitivity.chronal.ui.settings.layout.SettingsLayout
import dev.cognitivity.chronal.ui.settings.screens.SettingsHomeScreen
import dev.cognitivity.chronal.ui.settings.screens.SettingsListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageMain(
    expanded: Boolean,
    padding: PaddingValues
) {
    val navController = rememberNavController()

    SettingsScaffold(
        expanded = expanded,
        padding = padding
    ) {
        SettingsNavHost(navController, expanded)
    }
}

@Composable
fun SettingsScaffold(
    expanded: Boolean,
    padding: PaddingValues,
    content: @Composable () -> Unit
) {
    if (expanded) {
        SettingsPageExpanded {
            content()
        }
    } else {
        SettingsPageCompact(padding) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsNavHost(
    navController: NavHostController,
    expanded: Boolean
) {
    val spec: FiniteAnimationSpec<IntOffset> = MotionScheme.expressive().slowEffectsSpec()

    NavHost(
        navController = navController,
        startDestination = "root",
        enterTransition = {
            slideIn(initialOffset = { fullSize -> IntOffset(fullSize.width, 0) }, animationSpec = spec)
        },
        exitTransition = {
            slideOut(targetOffset = { fullSize -> IntOffset(0,0) }, animationSpec = spec)
        },
        popEnterTransition = {
            slideIn(initialOffset = { fullSize -> IntOffset(0,0) }, animationSpec = spec)
        },
        popExitTransition = {
            slideOut(targetOffset = { fullSize -> IntOffset(fullSize.width, 0) }, animationSpec = spec)
        }
    ) {
        composable("root") {
            SettingsHomeScreen(navController, expanded)
        }

        composable("category/{id}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("id")
            val category = categoryId?.let { SettingsLayout.categoryOf(it) }
            if (category != null) {
                SettingsListScreen(
                    title = context.getString(category.title),
                    items = category.items,
                    navController = navController,
                    expanded = expanded
                )
            }
        }

        composable("page/{id}") { backStackEntry ->
            val pageId = backStackEntry.arguments?.getString("id")
            val page = pageId?.let { SettingsLayout.pageOf(it) }
            if (page != null) {
                SettingsListScreen(
                    title = context.getString(page.title),
                    items = page.items,
                    navController = navController,
                    expanded = expanded
                )
            }
        }
    }
}