package com.example.epubreader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    onDismissRequest: () -> Unit,
    onTimeGoalSet: () -> Unit,
    bookDataViewModel: BookDataViewModel,
    timeGoalViewModel: TimeGoalViewModel,
    modifier: Modifier = Modifier
) {
    var hourValue by remember { mutableStateOf("") }
    var minuteValue by remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = { onDismissRequest() },
    ){
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(6.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                Text(
                    text = "Set Time Goal",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 20.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.onBackground)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Read",
                        textAlign = TextAlign.Start,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, bottom = 12.dp),
                        color = Color.Black
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TimeSelector(
                            hourValue = hourValue,
                            onHourValueChange = { newValue ->
                                val numeric = newValue.filter { it.isDigit() }
                                val intValue = numeric.toIntOrNull() ?: 0
                                if (numeric.isEmpty() || (intValue in 0..12) && numeric.length <= 2) {
                                    if (intValue in 0..9)
                                        hourValue = "0$numeric"
                                    hourValue = numeric
                                }
                                                },
                            minuteValue = minuteValue,
                            onMinuteValueChange = { newValue ->
                                val numeric = newValue.filter { it.isDigit() }
                                val intValue = numeric.toIntOrNull() ?: 0
                                if (numeric.isEmpty() || (intValue in 0..60) && numeric.length <= 2) {
                                    minuteValue = numeric
                                } }
                        )
                    }
                    Text(
                        text = "per day",
                        textAlign = TextAlign.End,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black
                    )
                }
                Text(
                    text = "This will reset your timer (if any)",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    color = colorResource(id = R.color.LightRed),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onDismissRequest() }
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    TextButton(
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            if (hourValue != "" || minuteValue != "") {
                                timeGoalViewModel.updateBookTimeGoal(
                                    bookDataViewModel.selectedBook.value?.uri!!,
                                    (hourValue.toInt() * 60 + minuteValue.toInt())
                                )
                                onTimeGoalSet()
                                onDismissRequest()
                            }
                        }
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    hourValue: String,
    onHourValueChange: (String) -> Unit,
    minuteValue: String,
    onMinuteValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val hourFocusRequester = remember { FocusRequester() }
    val minuteFocusRequester = remember { FocusRequester() }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = hourValue,
                onValueChange = onHourValueChange,
                placeholder = {Text(text = "00")},
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        minuteFocusRequester.requestFocus() }
                ),
                modifier = Modifier.focusRequester(hourFocusRequester),
            )
            Text(
                text = "Hour",
                fontSize = 12.sp

            )
        }
        Column(
            modifier = Modifier.height(50.dp).padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(5.dp))
                    .size(4.dp)
            )
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(5.dp))
                    .size(4.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = minuteValue,
                onValueChange = onMinuteValueChange,
                placeholder = {Text(text = "00")},
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.focusRequester(minuteFocusRequester),

                )
            Text(
                text = "Minutes",
                fontSize = 12.sp
            )
        }
        Spacer(
            modifier = Modifier.weight(1f)
        )
    }
    LaunchedEffect(Unit) {
        hourFocusRequester.requestFocus()
        delay(100)
        keyboardController?.show()
    }
}

