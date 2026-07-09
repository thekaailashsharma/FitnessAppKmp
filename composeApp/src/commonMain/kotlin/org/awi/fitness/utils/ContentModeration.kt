package org.awi.fitness.utils

/**
 * Lightweight client-side content filter for user-generated text (community posts & comments).
 *
 * This is the first line of the App Store Guideline 1.2 requirement (a method to filter
 * objectionable material). It blocks submission of hate slurs and explicit sexual content
 * BEFORE it ever reaches Firestore. Report + block flows handle anything that gets through.
 *
 * Deliberately conservative: it targets hard slurs and explicit NSFW terms, not mild profanity,
 * so ordinary gym talk ("this workout is hard as hell") is never blocked. Matching is
 * whole-word, case-insensitive, and normalises a few common leetspeak substitutions.
 */
object ContentModeration {

    sealed class Result {
        data object Allowed : Result()
        data class Blocked(val message: String) : Result()
    }

    // Hard slurs + explicit sexual terms. Kept intentionally short and high-precision.
    private val blockedWords: Set<String> = setOf(
        // hate slurs
        "nigger", "nigga", "faggot", "fag", "retard", "retarded", "chink", "spic",
        "kike", "wetback", "tranny", "coon", "dyke",
        // explicit sexual / NSFW
        "porn", "porno", "pornhub", "xxx", "cum", "blowjob", "handjob", "creampie",
        "dildo", "anal", "hentai", "milf", "nudes", "nsfw", "onlyfans",
    )

    private fun normalize(text: String): String =
        text.lowercase()
            .replace('0', 'o')
            .replace('1', 'i')
            .replace('3', 'e')
            .replace('4', 'a')
            .replace('5', 's')
            .replace('7', 't')
            .replace('@', 'a')
            .replace('$', 's')

    /** Returns [Result.Blocked] when the text contains disallowed content, else [Result.Allowed]. */
    fun check(text: String): Result {
        val normalized = normalize(text)
        // Split into word tokens on any non-letter so punctuation/emoji don't hide a slur.
        val tokens = normalized.split(Regex("[^a-z]+")).filter { it.isNotEmpty() }
        val tokenSet = tokens.toHashSet()
        val hit = blockedWords.firstOrNull { it in tokenSet }
        return if (hit != null) {
            Result.Blocked("Your post contains language that isn't allowed in the community. Please edit it and try again.")
        } else {
            Result.Allowed
        }
    }
}
