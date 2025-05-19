package com.example.dicegame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dicegame.R
import com.example.dicegame.viewmodel.GameViewModel

@Composable
fun GameScreen(
    onNavigateBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val targetInputDialogOpen = remember { mutableStateOf(true) }
    var targetScoreInput by remember { mutableStateOf("101") }

    if (targetInputDialogOpen.value) {
        TargetScoreDialog(
            currentTarget = targetScoreInput,
            onTargetChange = { targetScoreInput = it },
            onConfirm = {
                targetInputDialogOpen.value = false
                viewModel.startNewGame(targetScoreInput.toIntOrNull() ?: 101)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top section with scores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Win count
            Text(
                text = "H:${viewModel.humanWins}/C:${viewModel.computerWins}",
                style = MaterialTheme.typography.titleMedium
            )

            // Current scores
            Text(
                text = "Score: ${viewModel.humanScore} - ${viewModel.computerScore}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Human player part
        Text(
            text = "Your Dice (Rolls used: ${viewModel.humanRollsUsed}/3)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DiceRow(
            diceValues = viewModel.humanCurrentRoll,
            selectedDice = viewModel.humanSelectedDice,
            onDiceClick = { viewModel.toggleDiceSelection(it) },
            enabled = viewModel.humanRollsUsed in 1..2 && !viewModel.gameOver
        )

        // Computer player part
        Text(
            text = "Computer's Dice (Rolls used: ${viewModel.computerRollsUsed}/3)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        DiceRow(
            diceValues = viewModel.computerCurrentRoll,
            selectedDice = emptySet(),
            onDiceClick = { },
            enabled = false
        )

        Spacer(modifier = Modifier.weight(1f))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (viewModel.isTieBreaker) {
                        viewModel.rollTieBreaker()
                    } else {
                        viewModel.rollDice(isReroll = viewModel.humanRollsUsed > 0)
                    }
                },
                enabled = (!viewModel.gameOver &&
                        (viewModel.humanRollsUsed == 0 ||
                                (viewModel.humanRollsUsed < 3 && !viewModel.isTieBreaker)))
            ) {
                Text("Throw")
            }

            Button(
                onClick = { viewModel.scoreRoll() },
                enabled = (!viewModel.gameOver &&
                        viewModel.humanRollsUsed > 0 &&
                        viewModel.humanRollsUsed < 3 &&
                        !viewModel.isTieBreaker)
            ) {
                Text("Score")
            }
        }
    }


    if (viewModel.gameOver) {
        GameOverDialog(
            message = viewModel.winnerMessage,
            isWin = viewModel.winnerMessage == "You win!",
            onDismiss = onNavigateBack
        )
    }
}

@Composable
fun DiceRow(
    diceValues: List<Int>,
    selectedDice: Set<Int>,
    onDiceClick: (Int) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        diceValues.forEachIndexed { index, value ->
            DiceImage(
                value = value,
                isSelected = index in selectedDice,
                onClick = { onDiceClick(index) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun DiceImage(
    value: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val diceResource = when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        6 -> R.drawable.dice_6
        else -> R.drawable.dice_1
    }

    Box(
        modifier = Modifier
            .size(60.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color.Blue else Color.Transparent
            )
            .alpha(if (enabled) 1f else 0.7f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceResource),
            contentDescription = "Dice $value",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}

@Composable
fun TargetScoreDialog(
    currentTarget: String,
    onTargetChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { /* Cannot dismiss */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Target Score",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = currentTarget,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) onTargetChange(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Target Score") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Start Game")
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    message: String,
    isWin: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { /* Cannot dismiss */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Over see you next time",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isWin) Color.Green else Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = "Press Back to return to the main menu",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Return to Menu")
                }
            }
        }
    }
}