package io.github.highright1234.seniorcentertwerkmachine.config

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.highright1234.seniorcentertwerkmachine.util.NpcUtil
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine.Companion.plugin
import io.github.monun.tap.config.Config
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.mojangapi.MojangAPI
import kotlinx.coroutines.withContext
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import java.io.File
import java.util.*

object NpcDataConfig {
    /**
     * Tap Config 기능 쓸려고 했는데
     * 개같지만 ArrayList 안에 타입을 못찾아서
     * 그냥 직접 적은
     */
    private lateinit var file: File
    fun load(file: File) {
        this.file = file
        val config = YamlConfiguration.loadConfiguration(file)
        config.getKeys(false).forEach {
            val data = config[it] as NpcInformation
            data.createNpc()
        }
    }

    fun save() {
        var key = 0
        val config = YamlConfiguration()
        npcData.forEach {
            config.set("$key", it)
            key++
        }
        config.save(file)
    }

    @Config
    val npcData = arrayListOf<NpcInformation>()

    data class NpcInformation(
        val name: String,
        val uuid: UUID,
        val skinOwner: UUID,
        val location: Location,
    ) : ConfigurationSerializable {

        override fun serialize(): MutableMap<String, Any> {
            val out = mutableMapOf<String, Any>()
            out["location"] = location
            out["uuid"] = uuid.toString()
            out["name"] = name
            out["skin-owner"] = skinOwner.toString()
            return out
        }

        companion object {
            @JvmStatic
            fun deserialize(args: Map<String, Any>): NpcInformation {
                val location = args["location"] as Location
                val uuid = (args["uuid"] as String).toUUID()
                val name = (args["name"] as String)
                val skinOwner = (args["skin-owner"] as String).toUUID()
                return NpcInformation(name, uuid, skinOwner, location)
            }

            fun fromNpc(fakeEntity: FakeEntity<Player>, profile: MojangAPI.SkinProfile): NpcInformation {
                val name = fakeEntity.bukkitEntity.name
                val uuid = fakeEntity.bukkitEntity.uniqueId
                val location = fakeEntity.location
                val skinOwner = MojangAPI.Profile(profile.name, profile.id).uuid()
                return NpcInformation(
                    name,
                    uuid,
                    skinOwner,
                    location,
                )
            }

            private fun String.toUUID() = UUID.fromString(this)
        }

        fun createNpc() {
            plugin.launch {
                val skinProfile = withContext(plugin.asyncDispatcher) {
                    MojangAPI.fetchSkinProfile(skinOwner)
                }
                NpcUtil.createNpc(
                    name,
                    location,
                    uuid,
                    skinProfile
                )
            }
        }
    }
}