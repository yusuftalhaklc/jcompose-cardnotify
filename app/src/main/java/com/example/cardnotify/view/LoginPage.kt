package com.example.cardnotify.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cardnotify.R
import com.example.cardnotify.models.SignInState
import com.example.cardnotify.ui.theme.ChangeBarColors
import com.example.cardnotify.ui.theme.bgColor
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoginPage(
    state: SignInState,
    onSignInClick: () -> Unit
){
    val context = LocalContext.current
    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(
                context,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    ChangeBarColors(statusBarColor = bgColor, navigationBarColor = bgColor)
    Column (modifier = Modifier
        .fillMaxSize()
        .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.login2),
                contentDescription = "Login",
                modifier = Modifier
                    .padding(40.dp)
                    .shadow(10.dp, shape = RoundedCornerShape(21.dp)).clickable {
                        onSignInClick()
                    }
            )
        }
    }

}