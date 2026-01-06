package dev.sebastiano.channelor.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import dev.sebastiano.channelor.R
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme
import org.junit.Rule
import org.junit.Test

class DashboardUiTest {

        @get:Rule val composeTestRule = createComposeRule()

        @Test
        @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
        fun clickingChannelCard_opensBottomSheetWithDetails() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                val proString = context.getString(R.string.pro_zll_recommended)
                // We'll use a string that we know exists for cons as well, e.g., wifi interference
                val conString = context.getString(R.string.con_wifi_1_interference)

                val channel =
                        ZigbeeChannelCongestion(
                                channelNumber = 11,
                                centerFrequency = 2405,
                                congestionScore = 0.0,
                                isZllRecommended = true,
                                pros = listOf(R.string.pro_zll_recommended),
                                cons = listOf(R.string.con_wifi_1_interference),
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
                        val windowSizeClass =
                                WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp))

                        val sheetState = rememberModalBottomSheetState()
                        val scrollState = rememberScrollState()
                        // We need to hoist the state to test the interaction
                        var selectedChannel by remember {
                                mutableStateOf<ZigbeeChannelCongestion?>(null)
                        }

                        ChannelorTheme {
                                DashboardContent(
                                        state = state,
                                        onScanClick = {},
                                        onChannelClick = { selectedChannel = it },
                                        selectedChannel = selectedChannel,
                                        onChannelDismiss = { selectedChannel = null },
                                        sheetState = sheetState,
                                        scrollState = scrollState,
                                        windowSizeClass = windowSizeClass
                                )
                        }
                }

                // Click on the channel card
                // Note: ChannelCard displays "CH 11"
                composeTestRule.onNodeWithText("CH 11").performClick()

                // Verify bottom sheet title "Channel 11 Details"
                // We need to resolve the localized string for title format
                // stringResource(R.string.channel_details_title, channel.channelNumber)
                val titleString = context.getString(R.string.channel_details_title, 11)

                composeTestRule.onNodeWithText(titleString).assertIsDisplayed()

                // Verify pros and cons
                composeTestRule.onNodeWithText(proString).assertIsDisplayed()
                composeTestRule.onNodeWithText(conString).assertIsDisplayed()
        }
}
