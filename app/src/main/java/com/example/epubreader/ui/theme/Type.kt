package com.example.epubreader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.epubreader.R

val myFontFamily = FontFamily(
    Font(R.font.mplusrounded1c_regular, FontWeight.Normal),
    Font(R.font.mplusrounded1c_bold, FontWeight.Bold)
)
// Set of Material typography styles to start with
val customTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = myFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = myFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = myFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)