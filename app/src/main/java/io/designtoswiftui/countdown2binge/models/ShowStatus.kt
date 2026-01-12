package io.designtoswiftui.countdown2binge.models

enum class ShowStatus {
    RETURNING,
    ENDED,
    CANCELED,
    IN_PRODUCTION,
    PLANNED,
    UNKNOWN;

    companion object {
        fun fromTmdb(status: String): ShowStatus {
            return when (status.lowercase()) {
                "returning series" -> RETURNING
                "ended" -> ENDED
                "canceled" -> CANCELED
                "in production" -> IN_PRODUCTION
                "planned" -> PLANNED
                else -> UNKNOWN
            }
        }
    }
}
