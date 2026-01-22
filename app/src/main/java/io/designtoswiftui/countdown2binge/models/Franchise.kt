package io.designtoswiftui.countdown2binge.models

import com.google.gson.annotations.SerializedName
import java.util.Locale

/**
 * Franchise data from Firebase containing parent show and spinoffs.
 */
data class Franchise(
    val franchiseName: LocalizedString? = null,
    val parentShow: ParentShow? = null,
    val spinoffs: List<Spinoff> = emptyList(),
    val watchOrder: WatchOrder? = null
) {
    val id: String get() = parentShow?.tmdbId?.toString() ?: ""
}

/**
 * Multi-language string support for franchise names and notes.
 */
data class LocalizedString(
    val en: String = "",
    val es: String = "",
    val fr: String = "",
    val de: String = "",
    val pt: String = "",
    val it: String = "",
    val ja: String = "",
    val ko: String = "",
    @SerializedName("zh-Hans") val zhHans: String = "",
    val ar: String = ""
) {
    /**
     * Get the string in the device's current language, falling back to English.
     */
    val localized: String
        get() {
            val languageCode = Locale.getDefault().language
            return when (languageCode) {
                "es" -> es.ifEmpty { en }
                "fr" -> fr.ifEmpty { en }
                "de" -> de.ifEmpty { en }
                "pt" -> pt.ifEmpty { en }
                "it" -> it.ifEmpty { en }
                "ja" -> ja.ifEmpty { en }
                "ko" -> ko.ifEmpty { en }
                "zh" -> zhHans.ifEmpty { en }
                "ar" -> ar.ifEmpty { en }
                else -> en
            }
        }
}

/**
 * Parent show information in a franchise.
 */
data class ParentShow(
    val title: String = "",
    val tmdbId: Int = 0,
    val years: String = ""
)

/**
 * Spinoff show reference in a franchise.
 */
data class Spinoff(
    val title: String = "",
    val tmdbId: Int = 0,
    val years: String = "",
    val type: String = "",   // "prequel", "sequel", "companion"
    val status: String = ""  // "active", "ended"
) {
    val id: Int get() = tmdbId
}

/**
 * Watch order information for a franchise.
 */
data class WatchOrder(
    val release: List<WatchOrderItem> = emptyList(),
    val chronological: List<WatchOrderItem> = emptyList()
)

/**
 * Individual item in a watch order list.
 */
data class WatchOrderItem(
    val title: String = "",
    val note: LocalizedString? = null
)
