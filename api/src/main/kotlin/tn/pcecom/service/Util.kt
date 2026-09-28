package tn.pcecom.service

import java.text.Normalizer
import kotlin.random.Random

/** "Gaming Portables É" -> "gaming-portables-e" */
fun slugify(input: String): String =
    Normalizer.normalize(input, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')

fun randomSuffix(length: Int = 6): String {
    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
    return (1..length).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}

/** Escapes LIKE wildcards in user input. */
fun escapeLike(value: String): String =
    value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

/** Normalises to international format without "+", e.g. "22 123 456" -> "21622123456". */
fun normalizeWhatsapp(raw: String): String {
    val digits = raw.filter { it.isDigit() }.removePrefix("00")
    val full = if (digits.length == 8) "216$digits" else digits
    require(full.length in 10..15) { "invalid WhatsApp number" }
    return full
}

fun String?.cleanOrNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
