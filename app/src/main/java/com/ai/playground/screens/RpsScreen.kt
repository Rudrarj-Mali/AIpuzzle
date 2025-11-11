package com.ai.playground.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ai.playground.logic.getRpsResult // <-- Import local logic
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RpsScreen() {
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf("adaptive") }
    var userMove by remember { mutableStateOf("") }
    var aiMove by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Make your move!") }

    val moves = listOf("rock" to "🪨 Rock", "paper" to "📄 Paper", "scissors" to "✂ Scissors")
    val modes = listOf("random", "probability", "adaptive")
    val modeNames = mapOf(
        "random" to "Random",
        "probability" to "Probability",
        "adaptive" to "Adaptive"
    )

    fun play(move: String) {
        // No coroutine needed, this is instant!
        val response = getRpsResult(move)
        userMove = response.user
        aiMove = response.ai
        result = when (response.winner) {
            "user" -> "You win! 🎉"
            "ai" -> "AI wins! 🤖"
            else -> "It's a draw! 😐"
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rock-Paper-Scissors") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game result
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("You: ${userMove.ifEmpty { "-" }}", style = MaterialTheme.typography.titleMedium)
                    Text("AI: ${aiMove.ifEmpty { "-" }}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(result, style = MaterialTheme.typography.headlineSmall)
                }
            }

            // Mode selection
            Text("AI Mode:", style = MaterialTheme.typography.titleMedium)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEach { modeOption ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (mode == modeOption),
                                onClick = { mode = modeOption }
                            )
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = (mode == modeOption),
                            onClick = { mode = modeOption }
                        )
                        Text(
                            text = modeNames[modeOption] ?: modeOption,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Move buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Your move:", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moves.forEach { (move, display) ->
                        Button(onClick = { play(move) }) {
                            Text(display)
                        }
                    }
                }
            }
        }
    }
}