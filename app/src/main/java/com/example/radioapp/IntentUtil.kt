package com.example.radioapp
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.GsonBuilder

const val DEFAULT_NAME = "object"

/**
 * helper Object to put  parsable objects as extra on the Intent
 *
 */
object IntentUtil {
    @Suppress("SpellCheckingInspection")
    val gson: Gson = GsonBuilder().create()
}

/**
 * Put extra data on Intent
 *
 * @param name the keyword for the data
 * @param src data
 */
fun Intent.putExtraJson(name: String, src: Any) {
    putExtra(name, IntentUtil.gson.toJson(src))
}

/**
 * Put extra data on Intent
 *
 * @param src data
 */
fun Intent.putExtraJson(src: Any) {
    putExtra(DEFAULT_NAME, IntentUtil.gson.toJson(src))
}

/**
 * Get the extra class data from the intent
 *
 * @param T class type
 * @param name keyword for the data
 * @param class class
 * @return the data as class object defined in T
 */
fun <T> Intent.getJsonExtra(name: String, `class`: Class<T>): T? {
    val stringExtra = getStringExtra(name)
    if (stringExtra != null) {
        return IntentUtil.gson.fromJson<T>(stringExtra, `class`)
    }
    return null
}

/**
 * Get the extra class data from the intent without keyword
 *
 * @param T class Type
 * @param class class
 * @return the data as class object defined in T
 */
fun <T> Intent.getJsonExtra(`class`: Class<T>): T? {
    val stringExtra = getStringExtra(DEFAULT_NAME)
    if (stringExtra != null) {
        return IntentUtil.gson.fromJson<T>(stringExtra, `class`)
    }
    return null
}