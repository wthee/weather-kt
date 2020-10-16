package com.weather.data.model

import java.io.Serializable

data class SunMoonData(
    val date: String,
    val sunRise: String,
    val sunSet: String,
    val moonRise: String,
    val moonSet: String,
    val moonPhase: String
) : Serializable