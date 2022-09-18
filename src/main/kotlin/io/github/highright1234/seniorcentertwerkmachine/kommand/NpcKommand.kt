package io.github.highright1234.seniorcentertwerkmachine.kommand

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine
import io.github.highright1234.seniorcentertwerkmachine.suspendExecutes
import io.github.monun.kommand.PluginKommand
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.setLocation
import io.github.monun.tap.mojangapi.MojangAPI
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import java.util.concurrent.ExecutionException

object NpcKommand {

    fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("twerk", "t") {
            requires { isPlayer }
            then("npc-name" to string()) {
                suspendExecutes { kommandContext ->

                    val location = player.location.clone()

                    player.sendMessage(text("Creating twerk machine").color(NamedTextColor.GREEN))

                    val npcName : String = kommandContext["npc-name"]
                    val profile = profileOf(npcName) ?: run {
                        player.sendMessage(text("Unknown player").color(NamedTextColor.RED))
                        return@suspendExecutes
                    }

                    val armorStand = SeniorCenterTwerkMachine.fakeServer.spawnEntity(
                        location.apply { y -= 0.4 },
                        ArmorStand::class.java
                    ).apply { updateMetadata { isInvisible = true } } // 투명으로 아머스탠스 생성

                    val npc = SeniorCenterTwerkMachine.fakeServer.spawnPlayer(
                        location,
                        npcName,
                        profile.profileProperties().toSet()
                    ).apply {
                        updateMetadata {
                            isGliding = true
                            isCollidable = false
                        }
                        armorStand.addPassenger(this)
                    } // 아머스탠드에 앉히며 겉날개로 나는 모션 생성

                    player.sendMessage(
                        text()
                            .append(text("npc "))
                            .append(npc.bukkitEntity.displayName())
                            .append(text(" is successfully created"))
                            .color(NamedTextColor.GREEN)
                    )

                    npc.twerk()
                }
            }
        }
    }

    private fun FakeEntity<Player>.twerk() = SeniorCenterTwerkMachine.plugin.launch {
        while (valid) {
            for (i in -20..10 step 10) {
                newPitch(i)
                delay(1)
            }
            delay(100)
            for (i in 10 downTo -20 step 5) {
                newPitch(i)
                delay(1)
            }
        }
    }

    private fun FakeEntity<Player>.newPitch(newPitch: Int) {

        val newLocation = location.clone()
            .apply { pitch = newPitch.toFloat() }
            .also { bukkitEntity.setLocation(it) }

        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendPacket(
                PacketSupport.entityTeleport(bukkitEntity, newLocation)
            )
        }

    }

    private val profileImporter : MutableMap<String, Job> = mutableMapOf()
    private val cache : MutableMap<String, MojangAPI.SkinProfile> = mutableMapOf()
    private val cacheRemover : MutableMap<String, Job> = mutableMapOf()

    private suspend fun profileOf(name: String): MojangAPI.SkinProfile? {
        profileImporter[name]?.join()
        cache[name]?.let { return it }
        var profile : MojangAPI.SkinProfile? = null
        profileImporter[name] = SeniorCenterTwerkMachine.plugin.launch(
            SeniorCenterTwerkMachine.plugin.asyncDispatcher
        ) {
            try {
                MojangAPI.fetchProfile(name)
                    ?.uuid()
                    ?.let(MojangAPI::fetchSkinProfile)
                    .also { profile = it }
            } catch (_: ExecutionException) {
            }
            profile?.let { cache[name] = it }
        }
        profile?.let {
            SeniorCenterTwerkMachine.plugin.launch {
                delay(60000)
                cache -= name
                cacheRemover -= name
            }.let {
                cacheRemover[name]?.cancel()
                cacheRemover[name] = it
            }
        }
        return profile
    }
}