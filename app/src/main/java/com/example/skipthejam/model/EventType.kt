package com.example.skipthejam.model

enum class EventType(val eventName: String) {
    BLOCKED_STREET("Zatvorena ulica"),
    ACCIDENT("Saobraćajna nezgoda"),
    CONSTRUCTION("Radovi na putu"),
    FLOOD("Poplavljena ulica"),
    BORDER_CROSSING("Granični prelaz"),
    TRAFFIC_JAM("Gužva u saobraćaju")
}