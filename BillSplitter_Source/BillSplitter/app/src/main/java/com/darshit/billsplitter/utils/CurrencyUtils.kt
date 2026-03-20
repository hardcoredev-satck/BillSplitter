package com.darshit.billsplitter.utils

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val name: String
)

object CurrencyUtils {
    val currencies = listOf(
        CurrencyOption("USD", "$", "US Dollar"),
        CurrencyOption("EUR", "€", "Euro"),
        CurrencyOption("GBP", "£", "British Pound"),
        CurrencyOption("INR", "₹", "Indian Rupee"),
        CurrencyOption("JPY", "¥", "Japanese Yen"),
        CurrencyOption("CAD", "CA$", "Canadian Dollar"),
        CurrencyOption("AUD", "A$", "Australian Dollar"),
        CurrencyOption("CHF", "CHF", "Swiss Franc"),
        CurrencyOption("CNY", "¥", "Chinese Yuan"),
        CurrencyOption("SGD", "S$", "Singapore Dollar"),
        CurrencyOption("AED", "د.إ", "UAE Dirham"),
        CurrencyOption("MXN", "MX$", "Mexican Peso"),
        CurrencyOption("BRL", "R$", "Brazilian Real"),
        CurrencyOption("KRW", "₩", "South Korean Won"),
        CurrencyOption("SEK", "kr", "Swedish Krona")
    )

    fun getSymbolForCode(code: String): String {
        return currencies.find { it.code == code }?.symbol ?: "$"
    }

    fun formatAmount(amount: Double, currencySymbol: String): String {
        return if (amount == Math.floor(amount) && !amount.isInfinite()) {
            "$currencySymbol${amount.toLong()}"
        } else {
            "$currencySymbol${"%.2f".format(amount)}"
        }
    }
}
