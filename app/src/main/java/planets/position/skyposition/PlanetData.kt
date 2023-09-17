package planets.position.skyposition

data class PlanetData(
    val name: String,
    val number: Int,
    val rise: Int,
    val ra: Double,
    val dec: Double,
    val az: Double,
    val alt: Double,
    val distance: Double,
    val magnitude: Double,
    val setTime: Double,
    val riseTime: Double,
    val transit: Double,
    val time: Long
)
