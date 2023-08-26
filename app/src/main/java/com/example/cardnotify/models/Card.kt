package com.example.cardnotify.models

data class Card(
    val id: Number,
    val photo_url:String,
    val title:String,
    val description:String,
    val gender:String,
    val premium_card:Boolean,
    val lock:Boolean
)
