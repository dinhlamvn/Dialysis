package com.dialysis.app.ui.home.tabs.profile

import com.dialysis.app.data.network.response.LoginUser
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Localized texts the formatter needs. The UI fills it from string resources; tests fill it with literals,
 * so the formatter stays Android-free. Formats take one argument (`%1$d` / `%1$s`).
 */
data class ProfileTexts(
    val placeholder: String,
    val genderMale: String,
    val genderFemale: String,
    val genderOther: String,
    val ageFormat: String,
    val mlPerDayFormat: String,
    val timesPerWeekFormat: String,
    val weightFormat: String,
)

/** Display values of the read-only profile card; every missing value is [ProfileTexts.placeholder]. */
data class AccountProfileUi(
    val avatarLetter: String,
    val name: String,
    val username: String,
    val email: String,
    val phone: String,
    val gender: String,
    val age: String,
    val createdAt: String,
    val dailyWaterTarget: String,
    val initialWeight: String,
    val dialysisStartYear: String,
    val dialysisFreqWeek: String,
    val dailyUrine: String,
)

/** Pure mapping LoginUser -> [AccountProfileUi]. */
object AccountProfileFormatter {

    const val AVATAR_FALLBACK = "?"
    private val CREATED_AT_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.US)
    private val SQL_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun format(user: LoginUser, texts: ProfileTexts, zone: ZoneId): AccountProfileUi {
        // Gson ignores Kotlin nullability, so "non-null" fields of LoginUser may still be null here.
        val name = user.name.clean()
        val username = user.username.clean()
        val placeholder = texts.placeholder
        return AccountProfileUi(
            avatarLetter = avatarLetter(name, username),
            name = name ?: placeholder,
            username = username?.let { "@$it" } ?: placeholder,
            email = user.email.clean() ?: placeholder,
            phone = user.phone.clean() ?: placeholder,
            gender = gender(user.gender, texts) ?: placeholder,
            age = user.age.nonNegative()?.let { texts.ageFormat.formatWith(it) } ?: placeholder,
            createdAt = parseApiDateTime(user.createdAt)?.let { CREATED_AT_FORMAT.withZone(zone).format(it) }
                ?: placeholder,
            dailyWaterTarget = user.dailyWaterTarget.nonNegative()?.let { texts.mlPerDayFormat.formatWith(it) }
                ?: placeholder,
            initialWeight = weight(user.initialWeight, texts) ?: placeholder,
            dialysisStartYear = user.dialysisStartYear.nonNegative()?.toString() ?: placeholder,
            dialysisFreqWeek = user.dialysisFreqWeek.nonNegative()?.let { texts.timesPerWeekFormat.formatWith(it) }
                ?: placeholder,
            dailyUrine = user.dailyUrineMl.nonNegative()?.let { texts.mlPerDayFormat.formatWith(it) }
                ?: placeholder,
        )
    }

    /**
     * Parses the API `created_at`: ISO instant (`2025-10-01T08:30:00.000000Z`), ISO with offset,
     * or `yyyy-MM-dd HH:mm:ss` / ISO without offset read as UTC. Returns null when it cannot be parsed.
     */
    fun parseApiDateTime(value: String?): Instant? {
        val text = value.clean() ?: return null
        return runCatching { Instant.parse(text) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(text).toInstant() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(text, SQL_DATE_TIME).toInstant(ZoneOffset.UTC) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(text).toInstant(ZoneOffset.UTC) }.getOrNull()
    }

    private fun avatarLetter(name: String?, username: String?): String =
        (name ?: username)?.first()?.uppercase() ?: AVATAR_FALLBACK

    private fun gender(value: String?, texts: ProfileTexts): String? {
        val gender = value.clean() ?: return null
        return when {
            gender.equals("male", ignoreCase = true) -> texts.genderMale
            gender.equals("female", ignoreCase = true) -> texts.genderFemale
            else -> texts.genderOther
        }
    }

    private fun weight(value: Double?, texts: ProfileTexts): String? {
        if (value == null || !value.isFinite() || value < 0) return null
        return texts.weightFormat.formatWith(String.format(Locale.US, "%.2f", value))
    }

    private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun Int?.nonNegative(): Int? = this?.takeIf { it >= 0 }

    private fun String.formatWith(arg: Any): String = String.format(Locale.US, this, arg)
}
