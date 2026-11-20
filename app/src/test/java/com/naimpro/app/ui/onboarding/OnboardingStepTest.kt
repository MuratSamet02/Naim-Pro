package com.naimpro.app.ui.onboarding

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OnboardingStepTest {
    @Test
    @DisplayName("Flow starts at the welcome step")
    fun start() {
        assertEquals(OnboardingStep.WELCOME, OnboardingStep.first())
    }

    @Test
    @DisplayName("Forward navigation walks every step and then stops")
    fun forward() {
        var step: OnboardingStep? = OnboardingStep.first()
        val visited = mutableListOf<OnboardingStep>()
        while (step != null) {
            visited += step
            step = step.next()
        }
        assertEquals(OnboardingStep.entries.toList(), visited)
    }

    @Test
    @DisplayName("Backward navigation cannot leave the flow")
    fun backward() {
        assertNull(OnboardingStep.WELCOME.previous())
        assertEquals(OnboardingStep.WELCOME, OnboardingStep.LOCATION.previous())
    }

    @Test
    @DisplayName("Only the last step finishes the flow")
    fun last() {
        assertEquals(1, OnboardingStep.entries.count { it.isLast })
        assertTrue(OnboardingStep.METHOD.isLast)
    }

    @Test
    @DisplayName("Location is the only step that blocks continue without input")
    fun requiredInput() {
        assertFalse(canContinue(OnboardingStep.LOCATION, hasLocation = false))
        assertTrue(canContinue(OnboardingStep.LOCATION, hasLocation = true))
        OnboardingStep.entries.filter { it != OnboardingStep.LOCATION }.forEach {
            assertTrue(canContinue(it, hasLocation = false), "$it should be skippable")
        }
    }
}
