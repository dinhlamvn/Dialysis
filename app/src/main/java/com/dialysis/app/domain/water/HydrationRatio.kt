package com.dialysis.app.domain.water

/**
 * Hydration ratio sent as `weight_ratio` with each water intake.
 * Mirrors iOS `BeverageType.hydrationRatio`, which is currently 1.0 for every drink.
 * Change the values here (and only here) if iOS starts using per-drink ratios.
 */
object HydrationRatio {
    const val DEFAULT = 1.0

    @Suppress("UNUSED_PARAMETER")
    fun forDrink(drinkName: String): Double = DEFAULT
}
