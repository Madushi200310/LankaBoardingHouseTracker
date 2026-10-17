package lk.lankaboardinghouse.backend.security

/**
 * Single source of truth for password rules across register, change-password
 * and admin seeding. Keeping it in one place ensures the rule can never
 * drift between endpoints.
 */
object PasswordPolicy {

    /** Minimum length — matches the Android client's validation. */
    const val MIN_LENGTH = 6

    /**
     * bcrypt silently truncates input at 72 bytes, so reject anything longer
     * rather than let it be silently cut.
     */
    const val MAX_BYTES = 72

    fun validate(password: String) {
        if (password.length < MIN_LENGTH) {
            throw IllegalArgumentException(
                "Password must be at least $MIN_LENGTH characters"
            )
        }
        if (password.toByteArray(Charsets.UTF_8).size > MAX_BYTES) {
            throw IllegalArgumentException("Password is too long")
        }
    }
}