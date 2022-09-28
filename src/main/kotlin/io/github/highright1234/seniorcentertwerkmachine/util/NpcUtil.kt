package io.github.highright1234.seniorcentertwerkmachine.util

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine.Companion.fakeServer
import io.github.highright1234.seniorcentertwerkmachine.SeniorCenterTwerkMachine.Companion.plugin
import io.github.highright1234.seniorcentertwerkmachine.TwerkingMachine
import io.github.highright1234.seniorcentertwerkmachine.config.NpcDataConfig
import io.github.highright1234.seniorcentertwerkmachine.config.TwerkingConfig
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.PlayerInfoAction
import io.github.monun.tap.mojangapi.MojangAPI
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import java.util.*

object NpcUtil {

    suspend fun createNpc(
        name: String,
        location: Location,
        uuid: UUID? = null,
        skin: MojangAPI.SkinProfile? = null,
        deltaY: Double = TwerkingConfig.deltaY
    ) : Result<TwerkingMachine> {

        val profile = skin ?: profileOf(name) ?: run {
            return Result.failure(Exception("Not found profile of player"))
        }

        val spawnLocation = location.clone().apply { y += deltaY }

        val armorStand = fakeServer.spawnEntity(
            spawnLocation,
            ArmorStand::class.java
        ).apply { updateMetadata { isInvisible = true } } // 투명으로 아머스탠스 생성

        val npc = fakeServer.spawnPlayer(
            spawnLocation,
            name,
            profile.profileProperties().toSet(),
            uniqueId = uuid ?: UUID.randomUUID()
        ).apply {
            updateMetadata {
                isGliding = true
                isCollidable = false // 충돌 방진데 작동 안함 ㅋㅋ 망할
            }
            armorStand.addPassenger(this)
        } // 아머스탠드에 앉히며 겉날개로 나는 모션 생성

        plugin.launch {
            delay(500)
            Bukkit.getOnlinePlayers().forEach {
                it.sendPacket(PacketSupport.playerInfoAction(PlayerInfoAction.REMOVE, npc.bukkitEntity))
            }
        }

        npc.twerk()
        val machine = TwerkingMachine(npc, profile)
        NpcDataConfig.npcData += machine
        return Result.success(machine)
    }

    private fun FakeEntity<Player>.twerk() = plugin.launch {
        while (valid) {
            for (i in TwerkingConfig.pitchUpRange) {
                pitch = i
                delay(1)
            }
            delay(TwerkingConfig.delayBetweenPositionChanging)
            for (i in TwerkingConfig.pitchDownRange) {
                pitch = i
                delay(1)
            }
        }
    }

    private var FakeEntity<Player>.pitch : Int
    get() = location.pitch.toInt()
    set(value) {
        rotate(location.yaw, value.toFloat())
    }

    private val profileImporter : MutableMap<String, Job> = mutableMapOf()
    private val cache : MutableMap<String, MojangAPI.SkinProfile?> = mutableMapOf()
    private val cacheRemover : MutableMap<String, Job> = mutableMapOf()

    private suspend fun profileOf(name: String): MojangAPI.SkinProfile? {

        profileCacheOf(name).onSuccess { return it } // 캐쉬에서 가져오기

        var profile : MojangAPI.SkinProfile? = null
        val job = plugin.launch(
            plugin.asyncDispatcher
        ) {
            MojangAPI.fetchProfile(name) // uuid 가져오기
                ?.uuid()
                ?.let(MojangAPI::fetchSkinProfile) // MojangAPI 로 스킨 가져오기
                .also { profile = it }
            cache[name] = profile
            profileImporter -= name
        }
        profileImporter[name] = job // 캐쉬로 다른 코드에서 가져올때 멈추는 용도
        job.join()
        delayRemoveCache(name)
        return profile

    }

    private suspend fun profileCacheOf(name: String) : Result<MojangAPI.SkinProfile?> {
        if (name !in cache) {
            profileImporter[name]?.join() ?: return Result.failure(IllegalStateException("Not found importer"))
        }
        delayRemoveCache(name)
        return Result.success(cache[name])
    }

    // 캐쉬 제거기 / 캐쉬 제거 딜레이
    private suspend fun delayRemoveCache(name: String) {
        plugin.launch(plugin.asyncDispatcher) {
            delay(TwerkingConfig.cacheRemovingDelay)
            cache -= name
            cacheRemover -= name
        }.let {
            cacheRemover[name]?.cancel()
            cacheRemover[name] = it
        }
    }

    private fun getNearbyEntities(location: Location, maxDistance: Double = 10.0): List<TwerkingMachine> {
        return NpcDataConfig.npcData
            .filter { it.fakePlayer.location.world == location.world }
            .filter { it.fakePlayer.location.distance(location) < maxDistance }
    }

    fun raytraceMachines(
        start: Location, direction: Vector, maxDistance: Double = 3.0, raySize: Double = 0.5
    ) : TwerkingMachine? {
        require(start.world != null) { "Location's world can't be null" }
        start.checkFinite()

        direction.checkFinite()

        require(direction.lengthSquared() > 0) { "Direction's magnitude is 0!" }

        if (maxDistance < 0.0) return null

        val startPos: Vector = start.toVector()
        val entities: Collection<TwerkingMachine> = getNearbyEntities(start)

        var nearestHitEntity: TwerkingMachine? = null
        var nearestDistanceSq = Double.MAX_VALUE

        for (entity in entities) {
            val location = entity.fakePlayer.location.clone()
            val center = location.clone().add(location.direction)
            val boundingBox: BoundingBox = BoundingBox.of(center.clone(), center.clone()).expand(raySize)
            val hitResult = boundingBox.rayTrace(startPos, direction, maxDistance)
            if (hitResult != null) {
                val distanceSq = startPos.distanceSquared(hitResult.hitPosition)
                if (distanceSq < nearestDistanceSq) {
                    nearestHitEntity = entity
                    nearestDistanceSq = distanceSq
                }
            }
        }

        return nearestHitEntity

    }

    fun removeNpc(twerkingMachine: TwerkingMachine) {
        twerkingMachine.vehicle?.remove()
        twerkingMachine.fakePlayer.remove()
        NpcDataConfig.npcData -= twerkingMachine
    }
}