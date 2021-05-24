package com.stripe.android.compose.elements

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.regex.Pattern

class EmailConfigTest {
    private val emailConfig = EmailConfig(PATTERN)

    @Test
    fun `verify determine state returns blank and required when empty or null`() {
        assertThat(emailConfig.determineState(null)).isEqualTo(EmailConfig.Companion.Error.BlankAndRequired)
        assertThat(emailConfig.determineState("")).isEqualTo(EmailConfig.Companion.Error.BlankAndRequired)
    }

    @Test
    fun `verify the if email doesn't match the pattern it returns incomplete`() {
        assertThat(emailConfig.determineState("sdf")).isEqualTo(EmailConfig.Companion.Error.Incomplete)
    }

    @Test
    fun `verify the if email matches the pattern it returns Limitless`() {
        assertThat(emailConfig.determineState("sdf@gmail.com")).isEqualTo(EmailConfig.Companion.Valid.Limitless)
    }

    @Test
    fun `verify incomplete errors are shown when don't have focus`() {
        assertThat(
            emailConfig.shouldShowError(
                EmailConfig.Companion.Error.Incomplete,
                true
            )
        ).isEqualTo(false)
        assertThat(
            emailConfig.shouldShowError(
                EmailConfig.Companion.Error.Incomplete,
                false
            )
        ).isEqualTo(true)
    }

    @Test
    fun `verify blank and required errors are never shown`() {
        assertThat(
            emailConfig.shouldShowError(
                EmailConfig.Companion.Error.BlankAndRequired,
                true
            )
        ).isEqualTo(false)
        assertThat(
            emailConfig.shouldShowError(
                EmailConfig.Companion.Error.BlankAndRequired,
                false
            )
        ).isEqualTo(false)
    }

    @Test
    fun `verify Limitless states are never shown as error`() {
        assertThat(
            emailConfig.shouldShowError(
                EmailConfig.Companion.Valid.Limitless,
                true
            )
        ).isEqualTo(false)
        assertThat(
            emailConfig.shouldShowError(
                EmailConfig.Companion.Valid.Limitless,
                false
            )
        ).isEqualTo(false)
    }

    @Test
    fun `verify that only letters, numbers, periods and @ is allowed in the field`() {
        assertThat(emailConfig.filter("123^@gmail[\uD83E\uDD57.com"))
            .isEqualTo("123@gmail.com")
    }

    companion object {
        val PATTERN: Pattern = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
            )
    }
}