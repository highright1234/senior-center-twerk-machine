package io.github.highright1234.seniorcentertwerkmachine

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import io.github.highright1234.seniorcentertwerkmachine.config.NpcDataConfig
import io.github.highright1234.seniorcentertwerkmachine.config.TwerkingConfig
import io.github.highright1234.seniorcentertwerkmachine.kommand.NpcKommand
import io.github.highright1234.seniorcentertwerkmachine.listener.InteractListener
import io.github.highright1234.seniorcentertwerkmachine.listener.JoinQuitListener
import io.github.highright1234.seniorcentertwerkmachine.util.ListeningPlayer
import io.github.monun.kommand.KommandContext
import io.github.monun.kommand.KommandSource
import io.github.monun.kommand.kommand
import io.github.monun.kommand.node.KommandNode
import io.github.monun.tap.fake.FakeEntityServer
import kotlinx.coroutines.delay
import org.bukkit.configuration.serialization.ConfigurationSerialization
import java.io.File


class SeniorCenterTwerkMachine : SuspendingJavaPlugin() {

    companion object {
        lateinit var plugin: SeniorCenterTwerkMachine
        lateinit var fakeServer: FakeEntityServer
    }

    override suspend fun onEnableAsync() {
        plugin = this
        fakeServer = FakeEntityServer.create(this)
        ConfigurationSerialization.registerClass(TwerkingMachine::class.java)
        ListeningPlayer.activate()
        TwerkingConfig.load(File(dataFolder, "config.yml"))
        NpcDataConfig.load(File(dataFolder, "npc-data.yml"))
        repeatingFakeUpdate()
        kommand {
            NpcKommand.register(this)
        }
        server.pluginManager.registerSuspendingEvents(JoinQuitListener, this)
        server.pluginManager.registerSuspendingEvents(InteractListener, this)
        server.onlinePlayers.forEach(fakeServer::addPlayer)
    }

    override suspend fun onDisableAsync() {
        server.onlinePlayers.forEach(fakeServer::removePlayer)
        NpcDataConfig.save()
    }

    private fun repeatingFakeUpdate() {
        launch {
            while (true) {
                fakeServer.update()
                delay(1)
            }
        }
    }
}

fun KommandNode.suspendExecutes(executes: suspend KommandSource.(KommandContext) -> Unit) {
    executes { kommandContext ->
        SeniorCenterTwerkMachine.plugin.launch {
            executes.invoke(this@executes, kommandContext)
        }
    }
}