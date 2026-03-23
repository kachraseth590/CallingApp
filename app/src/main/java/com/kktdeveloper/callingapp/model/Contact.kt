package com.kktdeveloper.callingapp.model

data class Contact(
    val name: String,
    val number: String
)

// Simulated contacts for name mapping (bonus feature)
val sampleContacts = listOf(
    Contact("Amit Sharma", "9876543210"),
    Contact("Priya Patel", "9123456780"),
    Contact("Rahul Verma", "8899001122"),
    Contact("Sneha Roy", "7700112233"),
    Contact("Mom", "9000011111"),
    Contact("Dad", "9000022222")
)

fun getContactName(number: String): String {
    return sampleContacts.find { it.number == number }?.name ?: number
}
