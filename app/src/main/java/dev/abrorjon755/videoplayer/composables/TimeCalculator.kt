package dev.abrorjon755.videoplayer.composables

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TimeCalculator(
    seconds: Int,
    isBig: Boolean? = null,
    modifier: Modifier = Modifier,
) {
    var seconds2 = seconds
    if (seconds < 0) {
        seconds2 *= -1
    }
    Text(
        "${if (seconds < 0) "-" else if (isBig == false) "+" else ""}${minute(seconds2)}:${
            second(seconds2).toString().padStart(2, '0')
        }",
        style = if (isBig == true) MaterialTheme.typography.displayLarge else if (isBig == false) MaterialTheme.typography.displaySmall else MaterialTheme.typography.bodyLarge,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

fun minute(milliseconds: Int): Int {
    return (milliseconds / 1000 / 60)
}

fun second(milliseconds: Int): Int {
    val minute = minute(milliseconds)
    return (milliseconds / 1000 - minute * 60)
}