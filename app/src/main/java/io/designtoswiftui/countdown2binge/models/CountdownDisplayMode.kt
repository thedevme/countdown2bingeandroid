package io.designtoswiftui.countdown2binge.models

/**
 * Display mode for countdown on the Timeline hero card.
 *
 * Days mode: Shows how many calendar days until the season finale airs
 *   - Example: "12 DAYS" until the final episode
 *
 * Episodes mode: Shows how many episodes remain until the finale
 *   - Example: "3 EPS" left in the season
 */
enum class CountdownDisplayMode {
    DAYS,
    EPISODES
}
