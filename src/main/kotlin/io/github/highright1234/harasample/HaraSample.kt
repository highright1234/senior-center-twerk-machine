package io.github.highright1234.harasample

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import io.github.highright1234.harasample.kommand.SampleKommand
import io.github.highright1234.harasample.listener.SampleListener
import io.github.monun.kommand.KommandContext
import io.github.monun.kommand.KommandSource
import io.github.monun.kommand.kommand
import io.github.monun.kommand.node.LiteralNode

class HaraSample : SuspendingJavaPlugin() {
    companion object {
        lateinit var plugin: HaraSample
    }
    override suspend fun onEnableAsync() {
        plugin = this
        kommand {
            SampleKommand.register(this)
        }
        server.pluginManager.registerSuspendingEvents(SampleListener, this)
    }

    override suspend fun onDisableAsync() {

    }
}

fun LiteralNode.suspendExecutes(executes: suspend KommandSource.(KommandContext) -> Unit) {
    executes { kommandContext ->
        HaraSample.plugin.launch {
            executes.invoke(this@executes, kommandContext)
        }
    }
}