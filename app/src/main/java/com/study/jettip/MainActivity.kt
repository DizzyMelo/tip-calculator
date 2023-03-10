package com.study.jettip

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.Money
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.study.jettip.components.RoundIconButton
import com.study.jettip.components.InputField
import com.study.jettip.ui.theme.JetTipTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                val totalTipAmount = remember {
                    mutableStateOf(0f)
                }
                // A surface container using the 'background' color from the theme
                Column {
                    CreateTotalPerPersonBox(totalPerPerson = totalTipAmount.value)
                    CreateOptionsBox(totalTipAmount = totalTipAmount)
                }
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    JetTipTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            color = MaterialTheme.colors.background
        ) {
            content()
        }
    }
}

@Composable
fun CreateTotalPerPersonBox(totalPerPerson: Float = 10f) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(horizontal = 10.dp)
        .clip(shape = RoundedCornerShape(12.dp))
        .background(color = Color(0xFFE9D7F7)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Per Person",
                style = MaterialTheme.typography.h5
            )
            Text(
                text = "$${"%.2f".format(totalPerPerson)}",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

//@Preview
@Composable
fun CreateOptionsBox (totalTipAmount: MutableState<Float>) {
    BillForm(totalTipAmount = totalTipAmount) { bill ->
        Log.d("AMOUNT", "CreateOptionsBox: $bill")
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BillForm(modifier: Modifier = Modifier, totalTipAmount: MutableState<Float>, onValChange: (String) -> Unit = {}) {

    val bill = remember {
        mutableStateOf("")
    }

    val splitNumber = remember {
        mutableStateOf(1)
    }

    val tipValue = remember {
        mutableStateOf(0f)
    }

    val validState = remember(bill.value) {
        bill.value.trim().isNotEmpty()
    }

    var range by remember { mutableStateOf(0f) }
    val percentage = (range * 100).toInt()

    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier
            .padding(top = 20.dp)
            .border(
                border = BorderStroke(width = 1.dp, color = Color.LightGray),
                shape = CircleShape.copy(all = CornerSize(8.dp))
            ),
        elevation = 0.dp
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(10.dp)
        ) {
            Column {
                InputField(
                    valueState = bill,
                    labelId = "Enter Bill",
                    onAction = KeyboardActions {
                        if (!validState) return@KeyboardActions
                        onValChange(bill.value.trim())
                        keyboardController?.hide()
                    },
                    icon = Icons.Rounded.Money
                )
//                if (validState) {
                SplitController(splitNumber) {
                    if (splitNumber.value == 1 && it == -1) {
                        return@SplitController
                    }
                    splitNumber.value += it
                }
                DisplayTip(valueState = tipValue)
                PercentageSelector(range = range, percentage = percentage) {
                    range = it
                    val totalBill = bill.value.toFloat()
                    totalTipAmount.value = calculateTotalPerPerson(totalBill = totalBill, percentage = range, split = splitNumber.value)
                    tipValue.value = calculateTotalTip(totalBill = totalBill, percentage = range)
                }
//                }
            }
        }
    }
}

@Composable
fun SplitController(valueState: MutableState<Int>, updateValue: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Text(text = "Split")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(imageVector = Icons.Default.Remove) {
                updateValue(-1)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "${valueState.value}")
            Spacer(modifier = Modifier.width(10.dp))
            RoundIconButton(imageVector = Icons.Default.Add) {
                updateValue(1)
            }
        }
    }
}

@Composable
fun DisplayTip(valueState: MutableState<Float>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Tip")
        Text(text = "$${"%.2f".format(valueState.value)}")
    }
}

@Composable
fun PercentageSelector(range: Float, percentage: Int, onValChange: (Float) -> Unit) {
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(10.dp))
        Text("$percentage %")
        Spacer(modifier = Modifier.height(10.dp))
        Slider(
            value = range,
            onValueChange = {
            onValChange(it)
        }, steps = 5)
    }
}

fun calculateTotalPerPerson(totalBill: Float, percentage: Float, split: Int) = (totalBill * ( 1 + percentage )) / split
fun calculateTotalTip(totalBill: Float, percentage: Float) =  totalBill * percentage