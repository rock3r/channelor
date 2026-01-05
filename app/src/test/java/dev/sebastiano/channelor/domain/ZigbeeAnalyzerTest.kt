package dev.sebastiano.channelor.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZigbeeAnalyzerTest {

    private val analyzer = ZigbeeAnalyzer()

    @Test
    fun `analyzeCongestion returns 16 channels`() {
        val results = analyzer.analyzeCongestion(emptyList())
        assertEquals(16, results.size)
        assertEquals(11, results.first().channelNumber)
        assertEquals(26, results.last().channelNumber)
    }

    @Test
    fun `calculateCongestion with no wifi return zero scores`() {
        val results = analyzer.analyzeCongestion(emptyList())
        results.forEach { assertEquals(0.0, it.congestionScore, 0.001) }
    }

    @Test
    fun `calculateCongestion with overlapping wifi returns positive scores`() {
        // Wi-Fi Channel 1 is at 2412 MHz
        // Zigbee Channel 11 is at 2405 MHz
        // They overlap (dist = 7 MHz < 11 MHz)
        val wifi = listOf(WifiNetwork("Net1", 2412, -40))
        val results = analyzer.analyzeCongestion(wifi)

        val ch11 = results.find { it.channelNumber == 11 }!!
        assertTrue("CH11 should have congestion score > 0", ch11.congestionScore > 0)

        // Zigbee Channel 26 is at 2480 MHz
        // Far from Wi-Fi Channel 1 (dist = 68 MHz)
        val ch26 = results.find { it.channelNumber == 26 }!!
        assertEquals(0.0, ch26.congestionScore, 0.001)
    }

    @Test
    fun `ZLL recommended channels are correctly marked`() {
        val results = analyzer.analyzeCongestion(emptyList())
        val recommended = results.filter { it.isZllRecommended }.map { it.channelNumber }
        assertEquals(listOf(11, 15, 20, 25), recommended)
    }

    @Test
    fun `pros and cons are correctly populated for specific channels`() {
        val results = analyzer.analyzeCongestion(emptyList())

        val ch11 = results.find { it.channelNumber == 11 }!!
        assertTrue(ch11.cons.contains("Usually occupied by Wi-Fi (Channel 1)"))
        assertTrue(ch11.pros.contains("Zigbee Light Link (ZLL) recommended channel"))

        val ch26 = results.find { it.channelNumber == 26 }!!
        assertTrue(ch26.pros.contains("Little to no Wi-Fi interference"))
        assertTrue(ch26.cons.any { it.contains("Lower transmission power") })

        val ch15 = results.find { it.channelNumber == 15 }!!
        assertTrue(ch15.pros.contains("Zigbee Light Link (ZLL) recommended channel"))
        assertTrue(ch15.cons.isEmpty())

        val ch12 = results.find { it.channelNumber == 12 }!!
        assertTrue(ch12.pros.isEmpty())
        assertTrue(ch12.cons.contains("Not a standard ZLL channel"))
    }
}
