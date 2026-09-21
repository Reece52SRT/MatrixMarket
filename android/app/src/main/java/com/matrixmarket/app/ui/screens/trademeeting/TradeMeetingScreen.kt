package com.matrixmarket.app.ui.screens.trademeeting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matrixmarket.app.R
import com.matrixmarket.app.data.remote.dto.MyTradeMeetingResponse
import com.matrixmarket.app.ui.theme.LavenderLight
import com.matrixmarket.app.ui.theme.MeetingTeal
import com.matrixmarket.app.util.AppContainer
import com.matrixmarket.app.util.Resource
import com.matrixmarket.app.util.ViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeMeetingScreen(offerId: Int) {
    if (offerId <= 0) {
        MyMeetingsListScreen()
        return
    }

    val viewModel: TradeMeetingViewModel = viewModel(
        factory = ViewModelFactory { TradeMeetingViewModel(AppContainer.tradeRepository) }
    )
    val scheduleState by viewModel.scheduleState.collectAsState()
    val confirmState by viewModel.confirmState.collectAsState()

    var selectedSpot by remember { mutableStateOf("") }
    var confirmationCodeInput by remember { mutableStateOf("") }
    val safeSpots = listOf("Student Center North", "Library Foyer", "Campus Security Lobby")

    // Date & Time Picker State
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(14, 0)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.trade_meeting), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Schedule a safe, verified campus meeting spot and time.", color = Color.Gray)

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.choose_safe_spot), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            safeSpots.forEach { spot ->
                Surface(
                    color = if (selectedSpot == spot) MeetingTeal.copy(alpha = 0.15f) else Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clickable { selectedSpot = spot }
                    ) {
                        Text(spot)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Select Date & Time", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            // Date & Time Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                }

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
            }

            Spacer(Modifier.height(20.dp))

            when (scheduleState) {
                is Resource.Success -> {
                    val meeting = (scheduleState as Resource.Success).data
                    Surface(color = Color(0xFFFFF3D6), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Meeting Confirmed", fontWeight = FontWeight.Bold)
                            Text("Location: ${meeting.campusSpot}")
                            Text("Time: ${meeting.meetingTime}")
                            Text("Code: ${meeting.confirmationCode}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("Confirm Exchange (enter the code shown above once you meet)", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmationCodeInput,
                        onValueChange = { if (it.length <= 6) confirmationCodeInput = it.filter(Char::isDigit) },
                        label = { Text("6-digit code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.confirmTrade(meeting.meetingId, confirmationCodeInput) },
                        enabled = confirmState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MeetingTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.confirm_trade), color = Color.White)
                    }

                    if (confirmState is Resource.Success) {
                        Spacer(Modifier.height(10.dp))
                        Text((confirmState as Resource.Success).data, color = Color(0xFF2ECC71))
                    }
                    if (confirmState is Resource.Error) {
                        Spacer(Modifier.height(10.dp))
                        Text((confirmState as Resource.Error).message, color = Color.Red)
                    }
                }
                is Resource.Error -> {
                    Text((scheduleState as Resource.Error).message, color = Color.Red)
                }
                else -> {
                    Button(
                        onClick = {
                            val meetingDateTime = LocalDateTime.of(selectedDate, selectedTime)
                            val meetingTimeIso = meetingDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            viewModel.scheduleMeeting(offerId, selectedSpot, meetingTimeIso)
                        },
                        enabled = selectedSpot.isNotBlank() && scheduleState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MeetingTeal),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (scheduleState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Confirm Meeting", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Material3 DatePickerDialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Material3 TimePickerDialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }
}

// The "Meet" bottom-nav tab: lists every meeting the logged-in user is part of,
// whether as the buyer or the seller, so a seller can now see the meeting spot,
// time, and confirmation code once a buyer has scheduled it.
@Composable
private fun MyMeetingsListScreen() {
    val viewModel: TradeMeetingViewModel = viewModel(
        factory = ViewModelFactory { TradeMeetingViewModel(AppContainer.tradeRepository) }
    )
    val meetingsState by viewModel.myMeetingsState.collectAsState()
    val confirmState by viewModel.confirmState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyMeetings() }

    Surface(color = LavenderLight, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(stringResource(R.string.trade_meetings), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Every meeting you're scheduled for, as buyer or seller.", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))

            when (val state = meetingsState) {
                is Resource.Loading, Resource.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MeetingTeal)
                    }
                }
                is Resource.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Couldn't load your meetings.")
                            Text(state.message, fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadMyMeetings() },
                                colors = ButtonDefaults.buttonColors(containerColor = MeetingTeal)
                            ) { Text("Retry", color = Color.White) }
                        }
                    }
                }
                is Resource.Success -> {
                    if (state.data.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.no_active_trade_meetings), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.no_meetings_desc),
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.data, key = { it.meetingId }) { meeting ->
                                MeetingCard(
                                    meeting = meeting,
                                    confirmState = confirmState,
                                    onConfirm = { code -> viewModel.confirmTrade(meeting.meetingId, code) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingCard(
    meeting: MyTradeMeetingResponse,
    confirmState: Resource<String>,
    onConfirm: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var codeInput by remember { mutableStateOf("") }

    Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp).clickable { expanded = !expanded }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MeetingTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        meeting.role, // "Buyer" or "Seller"
                        color = MeetingTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(meeting.listingTitle, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                if (meeting.isConfirmed) {
                    Text("Confirmed", color = Color(0xFF2ECC71), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("With ${meeting.otherPartyName} · R ${"%.2f".format(meeting.price)}", fontSize = 13.sp, color = Color.Gray)
            Text("${meeting.campusSpot} · ${meeting.meetingTime}", fontSize = 13.sp, color = Color.Gray)

            if (expanded) {
                Spacer(Modifier.height(10.dp))
                Surface(color = Color(0xFFFFF3D6), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Confirmation Code", fontSize = 12.sp, color = Color.Gray)
                        Text(meeting.confirmationCode, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                if (!meeting.isConfirmed) {
                    Spacer(Modifier.height(10.dp))
                    Text("Enter the code once you've met to confirm the exchange:", fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { if (it.length <= 6) codeInput = it.filter(Char::isDigit) },
                        label = { Text("6-digit code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onConfirm(codeInput) },
                        enabled = confirmState !is Resource.Loading,
                        colors = ButtonDefaults.buttonColors(containerColor = MeetingTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.confirm_trade), color = Color.White)
                    }
                    if (confirmState is Resource.Error) {
                        Spacer(Modifier.height(6.dp))
                        Text(confirmState.message, color = Color.Red, fontSize = 12.sp)
                    }
                    if (confirmState is Resource.Success) {
                        Spacer(Modifier.height(6.dp))
                        Text(confirmState.data, color = Color(0xFF2ECC71), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}