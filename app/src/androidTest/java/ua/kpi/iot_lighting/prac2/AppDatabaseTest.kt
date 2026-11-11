package ua.kpi.iot_lighting.prac2

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ua.kpi.iot_lighting.prac2.data.local.AppDatabase
import ua.kpi.iot_lighting.prac2.data.local.entity.DeviceEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.EnergyReadingEntity
import ua.kpi.iot_lighting.prac2.data.local.entity.ZoneEntity
import ua.kpi.iot_lighting.prac2.domain.model.DeviceType
import ua.kpi.iot_lighting.prac2.domain.model.HistorySort
import ua.kpi.iot_lighting.prac2.domain.model.ReadingSource

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun joinedHistoryQuery_returnsDeviceAndZoneNames() = runBlocking {
        database.zoneDao().insert(ZoneEntity(1L, "Вітальня", "Тестова зона"))
        val deviceId = database.deviceDao().insert(deviceEntity(name = "Люстра"))
        database.energyReadingDao().insert(readingEntity(deviceId = deviceId, energy = 1.5))

        val history = database.energyReadingDao()
            .observeHistory(zoneId = null, deviceId = null, sort = HistorySort.NEWEST.name)
            .first()

        assertEquals(1, history.size)
        assertEquals("Люстра", history.first().deviceName)
        assertEquals("Вітальня", history.first().zoneName)
        assertEquals(1.5, history.first().energyKwh, 0.0001)
    }

    @Test
    fun deviceFilterQuery_appliesSearchAndZone() = runBlocking {
        database.zoneDao().insertAll(
            listOf(
                ZoneEntity(1L, "Вітальня", "A"),
                ZoneEntity(2L, "Кухня", "B")
            )
        )
        database.deviceDao().insert(deviceEntity(name = "Люстра", zoneId = 1L))
        database.deviceDao().insert(deviceEntity(name = "Підсвітка", zoneId = 2L))

        val livingRoom = database.deviceDao()
            .observeFiltered("", zoneId = 1L, type = null, onlyPoweredOn = false, sort = HistorySort.NEWEST.name)
            .first()
        assertEquals(1, livingRoom.size)
        assertEquals("Люстра", livingRoom.first().name)

        val search = database.deviceDao()
            .observeFiltered("Підс", zoneId = null, type = null, onlyPoweredOn = false, sort = HistorySort.NEWEST.name)
            .first()
        assertEquals(1, search.size)
        assertEquals("Підсвітка", search.first().name)
    }

    @Test
    fun deleteDevice_cascadesItsReadings() = runBlocking {
        database.zoneDao().insert(ZoneEntity(1L, "Вітальня", "Тест"))
        val deviceId = database.deviceDao().insert(deviceEntity())
        database.energyReadingDao().insert(readingEntity(deviceId = deviceId, energy = 2.0))

        database.deviceDao().deleteById(deviceId)

        val readings = database.energyReadingDao().observeAll().first()
        assertTrue(readings.isEmpty())
    }

    @Test
    fun aggregatesReturnExpectedValues() = runBlocking {
        database.zoneDao().insert(ZoneEntity(1L, "Вітальня", "Тест"))
        val deviceId = database.deviceDao().insert(deviceEntity(powerW = 20.0, quantity = 2, hours = 5.0))
        database.energyReadingDao().insertAll(
            listOf(
                readingEntity(deviceId = deviceId, energy = 1.0, cost = 4.32),
                readingEntity(deviceId = deviceId, energy = 3.0, cost = 12.96)
            )
        )

        assertEquals(4.0, database.energyReadingDao().observeTotalEnergy().first() ?: 0.0, 0.0001)
        assertEquals(17.28, database.energyReadingDao().observeTotalCost().first() ?: 0.0, 0.0001)
        assertEquals(2.0, database.energyReadingDao().observeAverageEnergy().first() ?: 0.0, 0.0001)
        assertEquals(0.2, database.deviceDao().observeDailyKwh().first() ?: 0.0, 0.0001)
    }

    @Test
    fun setPower_updatesStateAndCounter() = runBlocking {
        database.zoneDao().insert(ZoneEntity(1L, "Вітальня", "Тест"))
        val deviceId = database.deviceDao().insert(deviceEntity())

        database.deviceDao().setPower(deviceId, isOn = false, updatedAt = System.currentTimeMillis())

        val updated = database.deviceDao().getById(deviceId)
        assertTrue(updated != null)
        assertEquals(false, updated?.isPoweredOn)
        assertEquals(0, database.deviceDao().observePoweredOnCount().first())
    }

    @Test
    fun deleteRange_replacesOnlySelectedPeriod() = runBlocking {
        database.zoneDao().insert(ZoneEntity(1L, "Вітальня", "Тест"))
        val deviceId = database.deviceDao().insert(deviceEntity())
        val now = System.currentTimeMillis()
        val yesterday = now - 86_400_000L
        database.energyReadingDao().insertAll(
            listOf(
                readingEntity(deviceId = deviceId, energy = 1.0, timestamp = yesterday),
                readingEntity(deviceId = deviceId, energy = 2.0, timestamp = now)
            )
        )

        val from = now - 1_000L
        val to = now + 1_000L
        assertEquals(
            2.0,
            database.energyReadingDao().observeEnergyBetween(from, to).first() ?: 0.0,
            0.0001
        )

        database.energyReadingDao().deleteRange(from, to)

        val remaining = database.energyReadingDao().observeAll().first()
        assertEquals(1, remaining.size)
        assertEquals(1.0, remaining.first().energyKwh, 0.0001)
    }

    private fun deviceEntity(
        name: String = "Люстра",
        zoneId: Long = 1L,
        powerW: Double = 24.0,
        quantity: Int = 1,
        hours: Double = 6.0
    ) = DeviceEntity(
        id = 0L,
        zoneId = zoneId,
        name = name,
        type = DeviceType.LED.name,
        powerW = powerW,
        brightnessPercent = 80,
        colorTempK = 4000,
        hoursPerDay = hours,
        quantity = quantity,
        isPoweredOn = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun readingEntity(
        deviceId: Long,
        energy: Double,
        cost: Double = energy * 4.32,
        timestamp: Long = System.currentTimeMillis()
    ) = EnergyReadingEntity(
        id = 0L,
        deviceId = deviceId,
        timestamp = timestamp,
        energyKwh = energy,
        cost = cost,
        voltage = 230.0,
        current = 0.1,
        temperature = 30.0,
        source = ReadingSource.CALCULATED.name
    )
}
