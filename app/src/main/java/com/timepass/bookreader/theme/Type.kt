package com.timepass.bookreader.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.timepass.bookreader.R

val myFontFamily = FontFamily(
    Font(R.font.jura_variable_font_wght),
)

val customTypography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(fontFamily = myFontFamily),
        titleLarge = titleLarge.copy(fontFamily = myFontFamily),
        labelSmall = labelSmall.copy(fontFamily = myFontFamily),
    )
}