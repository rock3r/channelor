package dev.sebastiano.channelor.ui

import app.cash.turbine.test
import dev.sebastiano.channelor.data.WifiRepository
import dev.sebastiano.channelor.domain.ZigbeeAnalyzer
import dev.sebastiano.channelor.domain.ZigbeeChannelCongestion
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val wifiRepository = mockk<WifiRepository>()
    private val zigbeeAnalyzer = mockk<ZigbeeAnalyzer>()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { wifiRepository.getWifiScanResults() } returns flowOf(emptyList())
        coEvery { wifiRepository.triggerScan() } returns true // or false, doesn't matter much
        viewModel = MainViewModel(wifiRepository, zigbeeAnalyzer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `recommendedChannels should always include best ZLL channel in last position`() = runTest {
        val congestionScores =
            listOf(
                ZigbeeChannelCongestion(11, 2405, 0.5, isZllRecommended = true), // ZLL
                ZigbeeChannelCongestion(12, 2410, 0.1, isZllRecommended = false),
                ZigbeeChannelCongestion(13, 2415, 0.2, isZllRecommended = false),
                ZigbeeChannelCongestion(14, 2420, 0.3, isZllRecommended = false),
                ZigbeeChannelCongestion(15, 2425, 0.6, isZllRecommended = true), // ZLL
            )

        every { zigbeeAnalyzer.analyzeCongestion(any()) } returns congestionScores

        viewModel.recommendedChannels.test {
            assertEquals(emptyList<ZigbeeChannelCongestion>(), awaitItem())

            viewModel.onPermissionResult(true)

            val result = awaitItem()
            assertEquals(5, result.size)
            assertEquals(12, result[0].channelNumber)
            assertEquals(13, result[1].channelNumber)
            assertEquals(14, result[2].channelNumber)
            assertEquals(15, result[3].channelNumber)
            assertEquals(11, result[4].channelNumber)
        }
    }

    @Test
    fun `recommendedChannels should pick best ZLL even if it is the absolute best`() = runTest {
        val congestionScores =
            listOf(
                ZigbeeChannelCongestion(
                    11,
                    2405,
                    0.05,
                    isZllRecommended = true,
                ), // ZLL, absolute best
                ZigbeeChannelCongestion(12, 2410, 0.1, isZllRecommended = false),
                ZigbeeChannelCongestion(13, 2415, 0.2, isZllRecommended = false),
            )

        every { zigbeeAnalyzer.analyzeCongestion(any()) } returns congestionScores

        viewModel.recommendedChannels.test {
            assertEquals(emptyList<ZigbeeChannelCongestion>(), awaitItem())

            viewModel.onPermissionResult(true)

            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals(12, result[0].channelNumber)
            assertEquals(13, result[1].channelNumber)
            assertEquals(11, result[2].channelNumber)
        }
    }
}
