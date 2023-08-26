package com.example.cardnotify.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ChangeBarColors(
    statusBarColor: Color,
    navigationBarColor:Color,
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(
        color = statusBarColor
    )
    systemUiController.setNavigationBarColor(
        color = navigationBarColor
    )
}