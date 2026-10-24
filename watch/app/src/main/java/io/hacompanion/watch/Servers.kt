package io.hacompanion.watch

class Server(val name: String, val cpu: String, val ram: String, val disk: String, val temp: String?) {
    val ids get() = listOfNotNull(cpu, ram, disk, temp)
}

// all home-specific: from assets/config.json (see Config)
val SERVERS get() = Config.servers
val ALARM get() = Config.alarm
val WEATHER get() = Config.weather
val AWAY get() = Config.away
val EXTRA get() = listOf(ALARM, WEATHER, AWAY).filter { it.isNotBlank() }
