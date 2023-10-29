package planets.position.util

import java.util.Calendar

class JDUTC {

    // load c library
    companion object {
        init {
            System.loadLibrary("planets_swiss")
        }
    }

    // c function prototypes
    private external fun utc2jd(
        m: Int, d: Int, y: Int, hr: Int, min: Int,
        sec: Double
    ): DoubleArray?

    private external fun jd2utc(jdate: Double): String

    /**
     * Calls the utc2jd JNI c function. Converts from an utc date to a jullian
     * date.
     *
     * @param m   month
     * @param d   day
     * @param y   year
     * @param hr  hour
     * @param min minute
     * @param sec second
     * @return array of 2 jullian dates, [ut1,tt]
     */
    fun utcjd(m: Int, d: Int, y: Int, hr: Int, min: Int, sec: Double): DoubleArray? {
        return utc2jd(m, d, y, hr, min, sec)
    }

    /**
     * Returns the given Julian date in milliseconds in local time.
     *
     * @param jdate  The given date in Jullian format.
     * @param offset The UTC offset in minutes.
     * @return Long containing the milliseconds.
     */
    fun jdmills(jdate: Double, offset: Double): Long {
        val utc = Calendar.getInstance()
        val dateArr: Array<String> =
            jd2utc(jdate).split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        utc[dateArr[1].toInt(), dateArr[2].toInt() - 1, dateArr[3].toInt(), dateArr[4].toInt()] =
            dateArr[5].toInt()
        utc[Calendar.MILLISECOND] = (dateArr[6].toDouble() * 1000).toInt()
        // convert utc to local time
        utc.add(Calendar.MINUTE, offset.toInt())
        return utc.timeInMillis
    }

    /**
     * Returns the given Jullian date in milliseconds in UTC time.
     *
     * @param jdate The given date in Jullian format.
     * @return Long containing the milliseconds.
     */
    fun jdmills(jdate: Double): Long {
        val utc = Calendar.getInstance()
        val dateArr: Array<String> =
            jd2utc(jdate).split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        utc[dateArr[1].toInt(), dateArr[2].toInt() - 1, dateArr[3].toInt(), dateArr[4].toInt()] =
            dateArr[5].toInt()
        utc[Calendar.MILLISECOND] = (dateArr[6].toDouble() * 1000).toInt()
        return utc.timeInMillis
    }
}
