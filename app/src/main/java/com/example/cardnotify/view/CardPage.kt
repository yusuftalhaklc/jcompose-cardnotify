package com.example.cardnotify.view

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.view.RoundedCorner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.cardnotify.MainActivity
import com.example.cardnotify.R
import com.example.cardnotify.models.Card
import com.example.cardnotify.models.UserData
import com.example.cardnotify.notification.NotificationData
import com.example.cardnotify.notification.PushNotification
import com.example.cardnotify.notification.RetrofitInstance
import com.example.cardnotify.ui.theme.ChangeBarColors
import com.example.cardnotify.ui.theme.bgColor
import com.example.cardnotify.ui.theme.confirmButton
import com.example.cardnotify.ui.theme.dialogBg
import com.example.cardnotify.ui.theme.notifyBg
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CardPage(
    userData: UserData?,
    onSignOut: () -> Unit
){
    ChangeBarColors(statusBarColor = Color.White, navigationBarColor = Color.White)

    val db = Firebase.firestore

    val usersCollection = db.collection("users")
    val cardsCollection = db.collection("card")
    var context = LocalContext.current

    var user by remember {
        mutableStateOf<UserData?>(null)
    }

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showPrimaryQuestionDialog by remember { mutableStateOf(false) }
    var showSecondaryQuestionDialog by remember { mutableStateOf(false) }
    var showLastDialog by remember { mutableStateOf(true) }

    // val cardList = mutableListOf<Card>()
    val cardList by remember { mutableStateOf(mutableListOf<Card>()) }


    usersCollection.whereEqualTo("uuid", userData?.uuid)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                // Kullanıcı yoksa ekle
            } else {
                // Kullanıcı zaten varsa mevcut kullanıcı bilgilerini döndür
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceResult ->
                    Log.d("FIREBASE TOKENxxxxxxx", instanceResult.token)
                    val userDocumentId = result.documents.firstOrNull()?.id // Eklenen belgenin ID'sini al

                    // Eklenen belgenin ID'sini kullanarak token güncelleme işlemi
                    if (userDocumentId != null) {
                        usersCollection.document(userDocumentId).update("token", instanceResult.token)
                            .addOnSuccessListener {
                                Log.d("CARDNOTIFY USER update", "başarılı")
                            }
                            .addOnFailureListener {
                                if (userData != null) {
                                    Log.d("CARDNOTIFY USER update", "başarısız" + it.message + userData.uuid)
                                }
                            }
                    }
                }
            }
        }
        .addOnFailureListener { exception ->
            Log.d("CARDNOTIFY USER", "sorgu hatası")
        }



    cardsCollection.get()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result
                for (document in documents) {
                    val data = document.data
                    val card = Card(
                        id = data["id"] as Number,
                        photo_url = data["photo_url"] as String,
                        title = data["title"] as String,
                        description = data["description"] as? String ?: "",
                        gender = data["gender"] as String,
                        premium_card = data["premium_card"] as Boolean,
                        lock = data["lock"] as Boolean
                    )
                    if (!cardList.any { it.id == card.id }) {
                        cardList.add(card)
                    }

                }

            } else {
                Log.d("CARD NOTIFTY CARD", "Veri çekme işlemi başarısız oldu", task.exception)
            }
        }

    CreateAndSaveUser(usersCollection, userData) { success, userFromCallback ->
        if (success) {
            if (userFromCallback != null) {
                user = userFromCallback
                // Kullanıcı verileri güncellendiğinde yapılacak işlemler
            } else {
                // İşlem başarılı oldu ama user objesi null
                // Bu durumu ele alabilirsiniz veya bir hata durumu olabilir
            }
        } else {
            // İşlem başarısız oldu
            // Hata durumunu ele alabilir veya kullanıcıya bilgi verebilirsiniz
        }
    }
    
    if (user != null && cardList.isNotEmpty()) {
        if (user!!.matchingUser!!.isEmpty() || user!!.gender!!.isEmpty()){
            if (showLastDialog){
                showPrimaryQuestionDialog = true
            }

        }

        Column (modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp) ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                if(userData?.profilePictureUrl != null) {
                    AsyncImage(
                        model = userData.profilePictureUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                }

                if(userData?.displayName != null) {
                    Text(
                        text = userData.displayName,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,

                        )

                }

                if(user?.premium == true) {
                    Spacer(modifier = Modifier.padding(4.dp))
                    Image(painter =  painterResource(id = R.drawable.premium),
                        contentDescription ="Premium User",
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // Tıklama animasyonunu kapatmak için null olarak ayarlayın
                            ) {
                                Toast
                                    .makeText(
                                        context,
                                        "Premium User",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }

                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(modifier = Modifier.size(30.dp), onClick = { showSignOutDialog = true  } ) {
                    Icon(painter =  painterResource(id = R.drawable.baseline_exit_to_app_24), contentDescription ="Sign out")
                }

            }

            LazyRow( modifier = Modifier.padding(top = 80.dp, bottom = 130.dp)
                ,content = {
                    if (cardList.isNotEmpty()) {
                        val sortedCardList = cardList.sortedBy { it.id.toDouble() }
                        items(sortedCardList) {
                            val isPremiumUser = user?.premium == true
                            val isMaleUser = user?.gender == "Male"
                            val isFemaleUser = user?.gender == "Female"

                            if (it.premium_card) {
                                if (isPremiumUser) {
                                    Card(cardModel = it, senderUser = user!!)
                                }
                            } else {
                                val isLockedCardForPremiumUser = isPremiumUser && it.lock
                                if (!isLockedCardForPremiumUser) {
                                    if ((it.gender == "Male" && isMaleUser)) {
                                        Card(cardModel = it, senderUser = user!!)
                                    } else if ((it.gender == "Female" && isFemaleUser)) {
                                        Card(cardModel = it, senderUser = user!!)
                                    }
                                    else if ((it.gender == "unisex")) {
                                        Card(cardModel = it, senderUser = user!!)
                                    }
                                }


                            }
                        }
                    }

                })
        }
    } else {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
            val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(200.dp)
            )
        }

    }

    //sign out dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Confirmation") },
            text = { Text("Do you want to sign out?") },
            confirmButton = {
                TextButton(onClick = {onSignOut()}) {
                    Text("Sign out".uppercase())
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel".uppercase())
                }
            },
        )
    }

    //gender and matching_user dialog
    val radioOptions = listOf("Male", "Female")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    if (showPrimaryQuestionDialog){



        AlertDialog(
            onDismissRequest = { },
            title = { Text("Gender") },
            text = {

                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    radioOptions.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = {
                                        onOptionSelected(text)
                                    }
                                )
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) }
                            )
                            Text(
                                text = text,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }



            },
            confirmButton = {
                Button(
                    onClick = {


                        showPrimaryQuestionDialog = false
                        showSecondaryQuestionDialog = true

                    }
                ) {
                    Text("Confirm")
                }
            },

        )
    }

    if (showSecondaryQuestionDialog){
        var matchingUser by remember { mutableStateOf("") }
        var errorUserNotFound by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Matching User") },
            text = {
                Column {
                    OutlinedTextField(
                        value = matchingUser,
                        onValueChange = { matchingUser = it },
                        label = { Text(text = "User mail") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }


            },
            confirmButton = {
                Button(
                    onClick = {
                        val matchingUserEmailQuery = usersCollection.whereEqualTo("email", matchingUser).get()
                        matchingUserEmailQuery.addOnCompleteListener { emailQueryTask ->
                            if (emailQueryTask.isSuccessful) {
                                val emailQuerySnap = emailQueryTask.result
                                if (!emailQuerySnap.isEmpty) {
                                    // Kullanıcı bulundu, diğer işlemleri yapabilirsiniz
                                    userData?.uuid?.let { uuid ->
                                        val query = usersCollection.whereEqualTo("uuid", uuid).get()
                                        query.addOnSuccessListener { querySnap ->
                                            for (document in querySnap) {
                                                usersCollection.document(document.id).update(
                                                    "gender", selectedOption,
                                                    "matchingUser", matchingUser
                                                )
                                                    .addOnSuccessListener {
                                                        Log.d("CARDNOTIFY USER INFORMATION", "başarılı")
                                                        Toast.makeText(
                                                            context,
                                                            "Information Saved!",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        showSecondaryQuestionDialog = false
                                                        showPrimaryQuestionDialog = false
                                                        showLastDialog = false
                                                    }
                                                    .addOnFailureListener {
                                                        Log.d("CARDNOTIFY USER INFORMATION", "başarısız")
                                                    }
                                            }
                                        }
                                    }
                                } else {
                                    matchingUser = ""
                                    Toast.makeText(
                                        context,
                                        "User Not Found",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                // Sorgu başarısız oldu, ilgili işlemleri yapabilirsiniz
                            }
                        }



                    }
                ) {
                    Text("Confirm")
                }
            },

            )

    }

}

fun CreateAndSaveUser(usersCollection: CollectionReference, userData: UserData?, onComplete: (Boolean, UserData?) -> Unit) {
    // Kullanıcı var mı yok mu kontrolü
    if (userData == null || userData.uuid == null) {
        onComplete(false, null)
        return
    }

    usersCollection.whereEqualTo("uuid", userData.uuid)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                // Kullanıcı yoksa ekle
                usersCollection.add(userData)
                    .addOnSuccessListener {
                        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceResult ->
                            Log.d("FIREBASE TOKENxxxxxxx", instanceResult.token)
                            val userDocumentId = it.id // Eklenen belgenin ID'sini al

                            // Eklenen belgenin ID'sini kullanarak token güncelleme işlemi
                            usersCollection.document(userDocumentId).update("token", instanceResult.token)
                                .addOnSuccessListener {
                                    Log.d("CARDNOTIFY USER update", "başarılı")
                                    onComplete(true, userData)
                                }
                                .addOnFailureListener {
                                    Log.d("CARDNOTIFY USER update", "başarısız" + it.message + userData.uuid)
                                    onComplete(false, null)
                                }
                        }
                    }
                    .addOnFailureListener {
                        Log.d("CARDNOTIFY USER", "başarısız")
                        onComplete(false, null)
                    }
            } else {
                // Kullanıcı zaten varsa mevcut kullanıcı bilgilerini döndür
                val userDocument = result.documents.firstOrNull()
                val existingUser = userDocument?.toObject(UserData::class.java)
                onComplete(true, existingUser)
            }
        }
        .addOnFailureListener { exception ->
            Log.d("CARDNOTIFY USER", "sorgu hatası")
            onComplete(false, null)
        }
}


@Composable
fun Card(cardModel: Card, senderUser:UserData){

    var showDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    var premiumFeatureTitle by remember { mutableStateOf("") }
    var premiumFeatureBody by remember { mutableStateOf("") }

    var notifyTitle by remember { mutableStateOf("") }
    var notifyBody by remember { mutableStateOf("") }


    var receiverUser by remember {
        mutableStateOf<UserData?>(null)
    }

    val db = Firebase.firestore
    val usersCollection = db.collection("users")

    if (showPremiumDialog) {
        Dialog(onDismissRequest = { showPremiumDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {

                Column (modifier = Modifier.padding(20.dp)) {

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Özelleştirilebilir Kart",
                        fontSize = 20.sp ,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = premiumFeatureTitle,
                        onValueChange = { newValue ->
                            if (newValue.length <= 65) {
                                premiumFeatureTitle = newValue
                            }
                        },
                        label = { Text(text = "Bildirim Başlığı") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    OutlinedTextField(
                        value = premiumFeatureBody,
                        onValueChange = { newValue ->
                            if (newValue.length <= 240) {
                                premiumFeatureBody = newValue
                            }
                        },
                        label = { Text(text = "Bildirim İçeriği") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.padding(12.dp))
                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 0.dp),
                        horizontalArrangement = Arrangement.End

                    ){
                        Button(
                            onClick = {

                                if(premiumFeatureTitle.isNotEmpty() && premiumFeatureBody.isNotEmpty()){
                                    showPremiumDialog = false

                                    val matchingUserEmailQuery = usersCollection
                                        .whereEqualTo("email", senderUser.matchingUser)
                                        .get()
                                    matchingUserEmailQuery.addOnSuccessListener { result ->

                                        if (!result.isEmpty) {
                                            val userDocument = result.documents.firstOrNull()
                                            receiverUser = userDocument?.toObject(UserData::class.java)
                                            receiverUser?.token?.let {
                                                notifyTitle = premiumFeatureTitle
                                                notifyBody = premiumFeatureBody

                                                val notify = PushNotification(
                                                    to = it,
                                                    notification = NotificationData(
                                                        title = notifyTitle,
                                                        body = notifyBody
                                                    ),
                                                )
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val response =
                                                            RetrofitInstance.api.postNotification(notify)
                                                        if (response.isSuccessful) {
                                                            showDialog = true
                                                        } else {

                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("TAG", e.toString())
                                                    }
                                                }

                                            }
                                        }
                                    }




                                }

                                      },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButton,
                                contentColor = Color.White)
                            , shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(text = "Tamam")
                        }
                    }

                }



            }
        }
    }


    val context = LocalContext.current

    Column {
        AsyncImage(
            model = cardModel.photo_url,
            contentDescription = cardModel.description,
            modifier = Modifier
                .padding(40.dp)
                .shadow(10.dp, shape = RoundedCornerShape(21.dp))
                .clickable {

                    if (cardModel.title
                            .lowercase()
                            .contains("kilitli")
                    ) {
                        Toast
                            .makeText(
                                context,
                                "Premium Feature",
                                Toast.LENGTH_LONG
                            )
                            .show()
                    } else {



                        if (cardModel.title.lowercase() == "özelleştirilebilir kart") {
                            showPremiumDialog = true
                        }
                        else{
                            val matchingUserEmailQuery = usersCollection
                                .whereEqualTo("email", senderUser.matchingUser)
                                .get()
                            matchingUserEmailQuery.addOnSuccessListener { result ->

                                if (!result.isEmpty) {
                                    val userDocument = result.documents.firstOrNull()
                                    receiverUser = userDocument?.toObject(UserData::class.java)
                                    receiverUser?.token?.let {
                                        notifyTitle = cardModel.title
                                        notifyBody = "${senderUser.displayName} size ${cardModel.title}'ı gönderdi."
                                        val notify = PushNotification(
                                            to = it,
                                            notification = NotificationData(
                                                title = notifyTitle,
                                                body = notifyBody
                                            ),
                                        )
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                showDialog = true
                                                val response =
                                                    RetrofitInstance.api.postNotification(notify)
                                                if (response.isSuccessful) {
                                                    showDialog = true
                                                } else {

                                                }
                                            } catch (e: Exception) {
                                                Log.e("TAG", e.toString())
                                            }
                                        }

                                    }
                                }
                            }
                        }



                    }
                },
            contentScale = ContentScale.Crop
        )

    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.background(Color.White),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notification Sent",
                        fontSize = 20.sp ,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    //Confirm anim


                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confirm))
                    val progress by animateLottieCompositionAsState(composition)
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(150.dp)
                    )


                    Spacer(modifier = Modifier.height(12.dp))
                    //STATUS BAR
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 24.dp)
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                            .height(25.dp)
                            .background(notifyBg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween

                    ) {
                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val currentTime = Date()
                        val formattedTime = format.format(currentTime)

                        Text(
                            text = formattedTime,
                            fontSize = 10.sp ,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Text(
                            text = "${receiverUser?.displayName}'s Phone",
                            fontSize = 7.sp ,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Row (verticalAlignment = Alignment.CenterVertically){
                            Image(
                                painter = painterResource(id = R.drawable.cellular),
                                contentDescription ="Cellular",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 4.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.wifi),
                                contentDescription ="wifi",
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(start = 4.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.battery),
                                contentDescription ="battery",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(start = 4.dp)
                            )
                            Spacer(modifier =Modifier.padding(end = 4.dp))
                        }

                    }
                    Column (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp)
                            .height(120.dp)
                            .background(Brush.verticalGradient(listOf(notifyBg, Color.White))),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ){

                        Column (
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, top = 14.dp)
                                .shadow(1.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White) ,
                            horizontalAlignment = Alignment.Start

                        ){
                            Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_stat_nicon),
                                    contentDescription = "Card Notify",
                                    tint= Color.Gray,
                                    modifier = Modifier.size(8.dp)
                                )
                                Text(
                                    text = "Card Notify   ●   şimdi",
                                    fontSize = 6.sp ,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Text(
                                text = notifyTitle,
                                fontSize = 7.sp ,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                            Text(
                                text = notifyBody,
                                fontSize = 6.sp ,
                                color = Color.Black,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(start = 6.dp, bottom = 6.dp, end = 6.dp),
                                style = TextStyle(lineHeight = 8.sp)
                            )


                        }

                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp),
                        horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                showDialog = false
                                premiumFeatureTitle = ""
                                premiumFeatureBody = ""
                                      },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmButton,
                                contentColor = Color.White)
                            , shape = RoundedCornerShape(5.dp)
                        ) {
                            Text(text = "Tamam")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }}
            }

        }
    }
