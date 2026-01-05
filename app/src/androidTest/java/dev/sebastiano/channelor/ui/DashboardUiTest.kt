package dev.sebastiano.channelor.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme
import org.junit.Rule
import org.junit.Test

class DashboardUiTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun clickingChannelCard_opensBottomSheetWithDetails() {
        val channel =
            ZigbeeChannelCongestion(
                channelNumber = 11,
                centerFrequency = 2405,
                congestionScore = 0.0,
                isZllRecommended = true,
                pros = listOf("Test Pro"),
                cons = listOf("Test Con"),
            )
        val state =
            DashboardState(
                isScanning = false,
                zigbeeCongestion = listOf(channel),
                wifiScanResults = emptyList(),
                recommendedChannels = listOf(channel),
                top3Channels = setOf(11),
            )

        composeTestRule.setContent {
            ChannelorTheme { DashboardContent(state = state, onScanClick = {}) }
        }

        // Click on the channel card
        composeTestRule.onNodeWithText("CH 11").performClick()

        // Verify bottom sheet title
        composeTestRule.onNodeWithText("Channel 11 Details").assertIsDisplayed()

        // Verify pros and cons
        composeTestRule.onNodeWithText("Test Pro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Con").assertIsDisplayed()
    }
}
