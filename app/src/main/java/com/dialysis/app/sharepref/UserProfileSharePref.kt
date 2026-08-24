package com.dialysis.app.sharepref

import android.content.Context
import android.content.SharedPreferences
import com.dialysis.app.config.AppGoals
import com.dialysis.app.ui.info.InfoState
import com.dialysis.app.ui.info.UserProfile
import com.dialysis.app.ui.info.toUserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class UserProfileSharePref(
    context: Context,
    private val gson: Gson
) : SharePref(context, "user_profile_pref") {

    companion object {
        private const val KEY_PROFILE_JSON = "profile_json"
        private const val KEY_INITIAL_WEIGHT_KG = "initial_weight_kg"
        private const val KEY_WEIGHT_GOAL_KG = "weight_goal_kg"
        private const val KEY_DAILY_WATER_GOAL_ML = "daily_water_goal_ml"
        private const val KEY_DAILY_URINE_ML = "daily_urine_ml"
        private const val KEY_URINE_SAMPLES_JSON = "urine_samples_json"
        private const val MAX_LOCAL_URINE_SAMPLES = 100
    }

    fun saveProfile(state: InfoState) {
        put(KEY_PROFILE_JSON, gson.toJson(state.toUserProfile()))
        saveInitialWeightKg(state.weight)
    }

    fun hasProfile(): Boolean {
        return getProfile() != null
    }

    fun getProfile(): UserProfile? {
        val raw = get(KEY_PROFILE_JSON, "")
        if (raw.isBlank()) return null
        return runCatching {
            gson.fromJson(raw, UserProfile::class.java)
        }.getOrNull()
    }

    fun saveInitialWeightKg(weightKg: Int) {
        saveInitialWeightKg(weightKg.toFloat())
    }

    fun saveInitialWeightKg(weightKg: Float) {
        if (weightKg > 0f) {
            put(KEY_INITIAL_WEIGHT_KG, weightKg)
        }
    }

    fun getInitialWeightKg(): Int {
        val stored = getInitialWeightKgFloat()
        if (stored > 0f) return stored.toInt()
        return getProfile()?.weight ?: 0
    }

    fun getInitialWeightKgFloat(): Float {
        val stored = sharePref.all[KEY_INITIAL_WEIGHT_KG]
        if (stored is Number && stored.toFloat() > 0f) return stored.toFloat()
        return getProfile()?.weight?.toFloat() ?: 0f
    }

    fun saveWeightGoalKg(weightKg: Float) {
        if (weightKg > 0f) {
            put(KEY_WEIGHT_GOAL_KG, weightKg)
        }
    }

    fun getWeightGoalKg(defaultWeightKg: Float): Float {
        return get(KEY_WEIGHT_GOAL_KG, defaultWeightKg)
    }

    fun saveDailyWaterGoalMl(goalMl: Int) {
        if (goalMl > 0) {
            put(KEY_DAILY_WATER_GOAL_ML, goalMl)
        }
    }

    fun getDailyWaterGoalMl(): Int {
        val stored = get(KEY_DAILY_WATER_GOAL_ML, 0)
        return if (stored > 0) stored else AppGoals.DAILY_WATER_GOAL_ML
    }

    fun observeDailyWaterGoalMl(): Flow<Int> = callbackFlow {
        trySend(getDailyWaterGoalMl())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DAILY_WATER_GOAL_ML) {
                trySend(getDailyWaterGoalMl())
            }
        }
        sharePref.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sharePref.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun saveDailyUrineMl(urineMl: Int) {
        if (urineMl >= 0) {
            put(KEY_DAILY_URINE_ML, urineMl)
        }
    }

    fun getDailyUrineMl(): Int {
        val stored = get(KEY_DAILY_URINE_ML, -1)
        return if (stored >= 0) stored else getProfile()?.dailyUrineMl ?: 0
    }

    fun saveLocalUrineSample(
        amountMl: Int,
        loggedAt: String,
        note: String?,
        clientId: String
    ): LocalUrineSample {
        val sample = LocalUrineSample(
            id = -System.currentTimeMillis(),
            amountMl = amountMl,
            loggedAt = loggedAt,
            note = note,
            clientId = clientId,
            createdAt = loggedAt
        )
        val updated = buildList {
            add(sample)
            addAll(getLocalUrineSamples().filterNot { it.clientId == clientId })
        }.take(MAX_LOCAL_URINE_SAMPLES)
        put(KEY_URINE_SAMPLES_JSON, gson.toJson(updated))
        return sample
    }

    fun getLocalUrineSamples(): List<LocalUrineSample> {
        val raw = get(KEY_URINE_SAMPLES_JSON, "")
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<LocalUrineSample>>() {}.type
            gson.fromJson<List<LocalUrineSample>>(raw, type)
        }.getOrNull().orEmpty()
    }

    fun clear() {
        remove(KEY_PROFILE_JSON)
        remove(KEY_INITIAL_WEIGHT_KG)
        remove(KEY_WEIGHT_GOAL_KG)
        remove(KEY_DAILY_WATER_GOAL_ML)
        remove(KEY_DAILY_URINE_ML)
        remove(KEY_URINE_SAMPLES_JSON)
    }
}

data class LocalUrineSample(
    val id: Long,
    val amountMl: Int,
    val loggedAt: String,
    val note: String?,
    val clientId: String,
    val createdAt: String
)
