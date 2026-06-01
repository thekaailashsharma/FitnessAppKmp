package org.awi.fitness.utils

/** Safe Firestore document id for values like email addresses.
 *  Uses a single consistent encoding: replace special chars with underscored tokens.
 *  kailashps.1011@gmail.com → kailashps_DOT_1011_AT_gmail_DOT_com
 */
fun toFirestoreDocId(value: String): String =
    value
        .replace(".", "_DOT_")
        .replace("@", "_AT_")
        .replace("/", "_SLASH_")
