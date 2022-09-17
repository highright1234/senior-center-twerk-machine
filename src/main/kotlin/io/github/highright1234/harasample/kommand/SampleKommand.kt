package io.github.highright1234.harasample.kommand

import io.github.highright1234.harasample.suspendExecutes
import io.github.monun.kommand.PluginKommand

object SampleKommand {
    fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("sample") {
            requires { isPlayer }
            suspendExecutes {
                sender.sendMessage("Hello, World!")
            }
        }
    }
}