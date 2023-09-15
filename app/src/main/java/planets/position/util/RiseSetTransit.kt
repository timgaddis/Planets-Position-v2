package planets.position.util

class RiseSetTransit(val g: DoubleArray) {

    companion object {
        // load c library
        init {
            System.loadLibrary("planets_swiss")
        }
    }

    // c function prototypes
    private external suspend fun planetRST(dUT: Double, p: Int, loc: DoubleArray): DoubleArray

    suspend fun getRise(jdate: Double, planet: Int): Double {
        return planetRST(jdate, planet, g)[0]
    }

    suspend fun getSet(jdate: Double, planet: Int): Double {
        return planetRST(jdate, planet, g)[1]
    }

    suspend fun getTransit(jdate: Double, planet: Int): Double {
        return planetRST(jdate, planet, g)[2]
    }

}
