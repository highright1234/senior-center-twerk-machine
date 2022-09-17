package io.github.highright1234.seniorcentertwerkmachine.kommand

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine
import io.github.highright1234.seniorcentertwerkmachine.suspendExecutes
import io.github.monun.kommand.PluginKommand
import io.github.monun.tap.fake.setLocation
import io.github.monun.tap.mojangapi.MojangAPI
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import java.util.concurrent.ExecutionException

object NpcKommand {
    fun register(pluginKommand: PluginKommand) {
        pluginKommand.register("twerk", "t") {
            requires { isPlayer }
            then("npc-name" to string()) {
                suspendExecutes { kommandContext ->
                    val npcName : String = kommandContext["npc-name"]

                    val pig = SeniorCenterTwerkMachine.fakeServer.spawnEntity(
                        player.location.clone().apply { y -= 0.4 },
                        ArmorStand::class.java
                    )
                    pig.updateMetadata {
                        isInvisible = true
                    }
                    var profile : MojangAPI.SkinProfile? = null
                    player.sendMessage(text("Creating twerk machine").color(NamedTextColor.GOLD))
                    withContext(SeniorCenterTwerkMachine.plugin.asyncDispatcher) {
                        try {
                            MojangAPI.fetchProfile(npcName)!!
                                .uuid()
                                .let(MojangAPI::fetchSkinProfile)
                                .also { skinProfile ->
                                    profile = skinProfile
                                }
                        } catch (_: ExecutionException) {
                            player.sendMessage(text("Unknown player").color(NamedTextColor.RED))
                        } catch (_: NullPointerException) {
                            player.sendMessage(text("Unknown player").color(NamedTextColor.RED))
                        }
                    }
                    profile ?: return@suspendExecutes
                    val npc = SeniorCenterTwerkMachine.fakeServer.spawnPlayer(
                        player.location, npcName, profile!!.profileProperties().toSet()
                    )
                    npc.updateMetadata {
                        isGliding = true
                    }
                    pig.addPassenger(npc)
                    player.sendMessage(
                        text()
                            .append(text("npc "))
                            .append(npc.bukkitEntity.displayName())
                            .append(text(" is successfully created"))
                            .color(TextColor.fromHexString("#00FF00"))
                    )
                    SeniorCenterTwerkMachine.plugin.launch {
                        while (npc.valid) {
                            fun newLocation(newPitch : Int) {
                                val newLocation = npc.location.clone().apply { pitch = newPitch.toFloat() }
                                    .also {
                                        npc.bukkitEntity.setLocation(it)
                                    }
                                Bukkit.getOnlinePlayers().forEach {
                                    it.sendPacket(PacketSupport.entityTeleport(
                                        npc.bukkitEntity, newLocation
                                    ))
                                }
                            }
                            for (i in -20..20 step 10) {
                                newLocation(i)
                                delay(1)
                            }
                            delay(100)
                            for (i in 20 downTo -20 step 5) {
                                newLocation(i)
                                delay(1)
                            }
                        }
                    }
                }
            }
        }
    }
}