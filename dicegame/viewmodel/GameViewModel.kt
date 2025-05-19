package com.example.dicegame.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.dicegame.strategy.ComputerStrategy
import kotlin.random.Random

class GameViewModel : ViewModel() {

    var humanScore by mutableStateOf(0)
        private set
    var computerScore by mutableStateOf(0)
        private set
    var humanCurrentRoll by mutableStateOf(listOf<Int>())
        private set
    var computerCurrentRoll by mutableStateOf(listOf<Int>())
        private set
    var humanRollsUsed by mutableStateOf(0)
        private set
    var computerRollsUsed by mutableStateOf(0)
        private set
    var humanWins by mutableStateOf(0)
        private set
    var computerWins by mutableStateOf(0)
        private set
    var humanSelectedDice by mutableStateOf(setOf<Int>())
        private set
    var gameOver by mutableStateOf(false)
        private set
    var winnerMessage by mutableStateOf("")
        private set
    var targetScore by mutableStateOf(101)
        private set
    var isTieBreaker by mutableStateOf(false)
        private set
    var isComputerTurn by mutableStateOf(false)
        private set

    // Computer strategy
    private val computerStrategy = ComputerStrategy()

    // Game initialization
    fun startNewGame(target: Int = 101) {
        humanScore = 0
        computerScore = 0
        humanCurrentRoll = ArrayList<Int>().toList()
        computerCurrentRoll = ArrayList<Int>().toList()
        humanRollsUsed = 0
        computerRollsUsed = 0
        humanSelectedDice = HashSet<Int>().toSet()
        gameOver = false
        winnerMessage = ""
        targetScore = target
        isTieBreaker = false
        isComputerTurn = false
    }

    // Roll dice function
    fun rollDice(isReroll: Boolean = false) {
        if (gameOver) {
            return
        }

        if (!isReroll) {
            // First roll
            humanRollsUsed = 1
            computerRollsUsed = 1

            // Humans time to roll
            val humanRolls = ArrayList<Int>()
            for (i in 0 until 5) {
                val dieValue = Random.nextInt(1, 7)
                humanRolls.add(dieValue)
            }
            humanCurrentRoll = humanRolls

            // Computers time to roll
            val computerRolls = ArrayList<Int>()
            var i = 0
            while (i < 5) {
                computerRolls.add(Random.nextInt(1, 7))
                i++
            }
            computerCurrentRoll = computerRolls


            humanSelectedDice = HashSet<Int>().toSet()
        } else if (humanRollsUsed < 3) {

            humanRollsUsed += 1

            // Keep selected dice, reroll others
            val newRolls = ArrayList<Int>()
            for (i in humanCurrentRoll.indices) {
                if (humanSelectedDice.contains(i)) {
                    newRolls.add(humanCurrentRoll[i])
                } else {
                    newRolls.add(Random.nextInt(1, 7))
                }
            }
            humanCurrentRoll = newRolls

            // Computer strategy
            if (computerRollsUsed < 3) {
                val shouldReroll = computerStrategy.shouldReroll(
                    computerCurrentRoll,
                    computerRollsUsed,
                    computerScore,
                    humanScore,
                    targetScore
                )

                if (shouldReroll) {
                    computerRollsUsed += 1

                    val keepIndices = computerStrategy.diceToKeep(
                        computerCurrentRoll,
                        computerRollsUsed,
                        computerScore,
                        humanScore,
                        targetScore
                    )


                    val newComputerRolls = ArrayList<Int>()
                    for (i in computerCurrentRoll.indices) {
                        if (keepIndices.contains(i)) {
                            newComputerRolls.add(computerCurrentRoll[i])
                        } else {
                            val newValue = Random.nextInt(1, 7)
                            newComputerRolls.add(newValue)
                        }
                    }
                    computerCurrentRoll = newComputerRolls
                }
            }
        }


        if (humanRollsUsed >= 3) {
            performScoring()
        }
    }


    fun toggleDiceSelection(index: Int) {
        if (humanRollsUsed > 0 && humanRollsUsed < 3 && !gameOver) {

            val newSelection = HashSet(humanSelectedDice)

            // Toggle the selection
            if (newSelection.contains(index)) {
                newSelection.remove(index)
            } else {
                newSelection.add(index)
            }

            humanSelectedDice = newSelection
        }
    }

    // Score current roll
    fun scoreRoll() {
        if (humanRollsUsed <= 0 || gameOver) {
            return
        }

        // Complete computer turns
        while (computerRollsUsed < 3) {
            val shouldReroll = computerStrategy.shouldReroll(
                computerCurrentRoll,
                computerRollsUsed,
                computerScore,
                humanScore,
                targetScore
            )

            if (shouldReroll) {
                computerRollsUsed += 1

                val keepIndices = computerStrategy.diceToKeep(
                    computerCurrentRoll,
                    computerRollsUsed,
                    computerScore,
                    humanScore,
                    targetScore
                )

                // Create new computer rolls
                val newRolls = ArrayList<Int>()
                for (i in 0 until computerCurrentRoll.size) {
                    if (keepIndices.contains(i)) {
                        newRolls.add(computerCurrentRoll[i])
                    } else {
                        newRolls.add(Random.nextInt(1, 7))
                    }
                }
                computerCurrentRoll = newRolls
            } else {
                break
            }
        }

        performScoring()
    }

    // Calculating the scores and checking  game state
    private fun performScoring() {
        if (isTieBreaker) {

            var humanTotal = 0
            for (die in humanCurrentRoll) {
                humanTotal += die
            }

            var computerTotal = 0
            for (die in computerCurrentRoll) {
                computerTotal += die
            }

            // finding who is the winner
            if (humanTotal > computerTotal) {
                winnerMessage = "You win!"
                humanWins += 1
                gameOver = true
            } else if (computerTotal > humanTotal) {
                winnerMessage = "You lose"
                computerWins += 1
                gameOver = true
            } else {

                humanRollsUsed = 0
                computerRollsUsed = 0
            }
        } else {

            var humanRollTotal = 0
            for (die in humanCurrentRoll) {
                humanRollTotal += die
            }
            humanScore += humanRollTotal

            var computerRollTotal = 0
            for (die in computerCurrentRoll) {
                computerRollTotal += die
            }
            computerScore += computerRollTotal

            // Reset for next round
            humanRollsUsed = 0
            computerRollsUsed = 0
            humanSelectedDice = HashSet<Int>().toSet()


            if (humanScore >= targetScore || computerScore >= targetScore) {
                if (humanScore >= targetScore && computerScore >= targetScore) {
                    if (humanScore > computerScore) {
                        winnerMessage = "You win!"
                        humanWins += 1
                        gameOver = true
                    } else if (computerScore > humanScore) {
                        winnerMessage = "You lose"
                        computerWins += 1
                        gameOver = true
                    } else {
                        // Tie
                        isTieBreaker = true
                        humanRollsUsed = 0
                        computerRollsUsed = 0
                    }
                } else if (humanScore >= targetScore) {
                    winnerMessage = "You win!"
                    humanWins += 1
                    gameOver = true
                } else {
                    winnerMessage = "You lose"
                    computerWins += 1
                    gameOver = true
                }
            }
        }
    }

    // Handle tie breaker
    fun rollTieBreaker() {
        if (!isTieBreaker || humanRollsUsed != 0) {
            return
        }

        humanRollsUsed = 1
        computerRollsUsed = 1

        // Roll human dice
        val humanRolls = ArrayList<Int>()
        for (i in 0 until 5) {
            humanRolls.add(Random.nextInt(1, 7))
        }
        humanCurrentRoll = humanRolls


        val computerRolls = ArrayList<Int>()
        for (i in 0 until 5) {
            computerRolls.add(Random.nextInt(1, 7))
        }
        computerCurrentRoll = computerRolls


        var humanTotal = 0
        for (die in humanCurrentRoll) {
            humanTotal += die
        }

        var computerTotal = 0
        for (die in computerCurrentRoll) {
            computerTotal += die
        }


        if (humanTotal > computerTotal) {
            winnerMessage = "You win the dice game!"
            humanWins += 1
            gameOver = true
        } else if (computerTotal > humanTotal) {
            winnerMessage = "You lose the dice game"
            computerWins += 1
            gameOver = true
        } else {

            humanRollsUsed = 0
            computerRollsUsed = 0
        }
    }

    // Update target score
    fun updateTargetScore(score: Int) {
        if (score > 0) {
            targetScore = score
        }
    }
}

