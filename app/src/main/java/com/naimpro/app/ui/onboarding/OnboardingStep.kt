package com.naimpro.app.ui.onboarding

/**
 * Ordered first-run steps.
 *
 * The flow is modelled as plain data so the "can the user continue?" rules are
 * covered by fast JVM tests instead of an emulator run.
 */
enum class OnboardingStep {
    WELCOME,
    LOCATION,
    NOTIFICATIONS,
    METHOD,
    ;

    val isLast: Boolean get() = this == METHOD

    /** Whether the user may leave this step without providing input. */
    val isSkippable: Boolean get() = this == NOTIFICATIONS || this == METHOD

    fun next(): OnboardingStep? = entries.getOrNull(ordinal + 1)

    fun previous(): OnboardingStep? = entries.getOrNull(ordinal - 1)

    companion object {
        fun first(): OnboardingStep = WELCOME
    }
}

/** Whether the continue button is enabled on [step]. */
fun canContinue(
    step: OnboardingStep,
    hasLocation: Boolean,
): Boolean =
    when (step) {
        OnboardingStep.LOCATION -> hasLocation
        else -> true
    }
