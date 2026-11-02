package com.neighborhero.neighborhero.data.model

/**
 * Standardized category taxonomy for NeighborHero.
 * Defines the six canonical categories alongside legacy aliases preserved for backward compatibility.
 */
object RequestCategory {
    const val GROCERIES = "Groceries"
    const val BOOKS = "Books"
    const val REPAIRS = "Repairs"
    const val TRANSPORT = "Transport"
    const val PET_HELP = "Pet Help"
    const val OTHER = "Other"

    /**
     * The six canonical categories for NLP classification, UI selection, and helper matchmaking.
     */
    val CANONICAL: List<String> = listOf(
        GROCERIES,
        BOOKS,
        REPAIRS,
        TRANSPORT,
        PET_HELP,
        OTHER
    )

    /**
     * Legacy category aliases preserved for backward compatibility.
     * Note: Books and Tutoring are kept distinct; Tutoring is not silently mapped to Books.
     */
    val LEGACY: List<String> = listOf(
        "Pet Care",
        "Tutoring",
        "Moving",
        "Yardwork"
    )

    /**
     * All recognized category names (canonical + legacy).
     */
    val ALL_SUPPORTED: List<String> = CANONICAL + LEGACY

    fun isCanonical(category: String): Boolean = CANONICAL.contains(category)

    fun isSupported(category: String): Boolean = ALL_SUPPORTED.contains(category)

    /**
     * Determines whether a request category matches a selected filter category,
     * supporting Pet Help <-> Pet Care alias compatibility while keeping Books and Tutoring distinct.
     */
    fun matches(requestCategory: String, filterCategory: String): Boolean {
        if (filterCategory.equals("All", ignoreCase = true)) return true
        if (requestCategory.equals(filterCategory, ignoreCase = true)) return true
        if (filterCategory.equals(PET_HELP, ignoreCase = true) && requestCategory.equals("Pet Care", ignoreCase = true)) return true
        if (filterCategory.equals("Pet Care", ignoreCase = true) && requestCategory.equals(PET_HELP, ignoreCase = true)) return true
        return false
    }
}
