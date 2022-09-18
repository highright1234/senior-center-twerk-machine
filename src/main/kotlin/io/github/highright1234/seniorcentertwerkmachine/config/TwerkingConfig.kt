package io.github.highright1234.seniorcentertwerkmachine.config

import io.github.monun.tap.config.Config
import io.github.monun.tap.config.ConfigSupport
import java.io.File

@Suppress("MagicNumber")
object TwerkingConfig {
    fun load(file: File) = ConfigSupport.compute(this, file)

    private val pitchIntRange : IntRange get() {
        val numbers = pitchRange.split("..").map { it.toInt() }
        return numbers[0]..numbers[1]
    }

    val pitchUpRange get() = pitchIntRange step pitchUpStep
    val pitchDownRange get() = pitchIntRange step pitchDownStep

    @Config
    var pitchRange = "-20..10"

    @Config
    var pitchUpStep = 5

    @Config
    var pitchDownStep = 10

    @Config
    var delayBetweenPositionChanging = 100L

    @Config
    var deltaY = -0.4

    @Config
    var cacheRemovingDelay = 60000L
}