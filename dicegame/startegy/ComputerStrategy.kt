package com.example.dicegame.strategy

import kotlin.random.Random

class ComputerStrategy {

    fun shouldReroll(
        currentRoll: List<Int>,
        rollsUsed: Int,
        computerScore: Int,
        humanScore: Int,
        targetScore: Int
    ): Boolean {
        // Prevent rerolling if maximum rolls reached
        if (rollsUsed >= 3) return false

        val currentSum = currentRoll.sum()
        val scoreDifference = humanScore - computerScore
        val remainingToTarget = targetScore - computerScore

        return when {
            // More aggressive when behind
            scoreDifference > remainingToTarget / 2 -> currentSum < 15

            // Conservative when close to winning
            remainingToTarget <= 20 -> currentSum < 16


            rollsUsed == 1 -> currentSum < 12
            rollsUsed == 2 -> currentSum < 15

            else -> false
        }
    }

    fun diceToKeep(
        currentRoll: List<Int>,
        rollsUsed: Int,
        computerScore: Int,
        humanScore: Int,
        targetScore: Int
    ): Set<Int> {
        val keepIndices = mutableSetOf<Int>()
        val scoreDifference = humanScore - computerScore
        val remainingToTarget = targetScore - computerScore

        currentRoll.forEachIndexed { index, value ->
            when {
                // Always keep 6s
                value == 6 -> keepIndices.add(index)

                // Contextual strategy for 5s
                value == 5 -> {
                    val keepProbability = when {
                        scoreDifference > remainingToTarget / 2 -> 0.8
                        remainingToTarget <= 20 -> 0.6
                        else -> 0.7
                    }
                    if (Random.nextDouble() < keepProbability) keepIndices.add(index)
                }

                // Moderate strategy for 4s
                value == 4 -> {
                    val keepProbability = when {
                        scoreDifference > remainingToTarget / 2 -> 0.4
                        else -> 0.2
                    }
                    if (Random.nextDouble() < keepProbability) keepIndices.add(index)
                }
            }
        }

        return keepIndices
    }
}
/* Strategy Documentation

Objective:
- Create a moderate-complexity strategy for the computer player
- Implement basic decision-making logic for dice rerolling

Strategy Principles:
1. Reroll Decision Criteria:
   - Aggressive rerolling when far from target score
   - Conservative approach when close to winning
   - Consider current roll sum for reroll decisions

2. Dice Keeping Strategy:
   - Prioritize keeping high-value dice (5s and 6s)
   - Occasional keeping of 4s with randomness
   - Simple probabilistic approach

Advantages:
- Straightforward and easy to understand
- Provides some strategic variability
- Adapts slightly to game state

Limitations:
- Not highly sophisticated
- Limited contextual awareness
- Relies on basic probabilistic decisions

Expected Performance:
- Better than completely random strategy
- Provides moderate challenge to human player
- Demonstrates basic strategic thinking
*/