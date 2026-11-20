package com.naimpro.app.ui.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, radius and sizing tokens.
 *
 * Every screen must use these values instead of raw numbers so the whole app
 * keeps one rhythm. They are plain constants (no theme lookup) which keeps them
 * cheap and usable from widgets and previews.
 */
object NaimSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp

    /** Horizontal page gutter. */
    val gutter: Dp = 16.dp

    /** Minimum touch target required by the accessibility gate. */
    val touchTarget: Dp = 48.dp
}

object NaimRadius {
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 28.dp
    val pill: Dp = 999.dp
}
