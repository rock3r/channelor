package dev.sebastiano.channelor.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import dev.sebastiano.channelor.ui.theme.ChannelorTheme
import org.junit.Rule
import org.junit.Test

class ChannelCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun channelCard_displaysChannelNumber() {
    val channel =
      ZigbeeChannelCongestion(
        channelNumber = 11,
        centerFrequency = 2405,
        congestionScore = 0.0,
        isZllRecommended = true,
      )

    composeTestRule.setContent { ChannelorTheme { ChannelCard(channel) } }

    composeTestRule.onNodeWithText("CH 11").assertExists()
    composeTestRule.onNodeWithText("2405 MHz").assertExists()
    composeTestRule.onNodeWithText("ZLL Recommended").assertExists()
  }

  @Test
  fun channelCard_displaysAnnotation() {
    val channel =
      ZigbeeChannelCongestion(
        channelNumber = 26,
        centerFrequency = 2480,
        congestionScore = 0.0,
        annotation = "Problematic",
      )

    composeTestRule.setContent { ChannelorTheme { ChannelCard(channel) } }

    composeTestRule.onNodeWithText("CH 26").assertExists()
    composeTestRule.onNodeWithText("Problematic").assertExists()
  }
}
