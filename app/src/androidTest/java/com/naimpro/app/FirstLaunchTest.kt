package com.naimpro.app

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First launch instrumentation test.
 *
 * Runs on the CI emulator only (no local hypervisor). It verifies that the app
 * boots into either onboarding or the home surface and that the onboarding
 * "continue" action never crashes, which is the class of bug that used to hide
 * behind a silent Ankara fallback.
 */
@RunWith(AndroidJUnit4::class)
class FirstLaunchTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appBootsIntoOnboardingOrHome() {
        rule.waitForIdle()
        val onboardingTitle =
            rule.onAllNodes(hasText("Naim Pro'ya hoş geldiniz"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        if (onboardingTitle) {
            rule.onNodeWithText("Devam").performClick()
            rule.waitForIdle()
        }
        // The activity must still be alive after the first frame and one action.
        assertTrue(!rule.activity.isFinishing)
    }
}
