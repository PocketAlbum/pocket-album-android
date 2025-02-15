package si.pocketalbum.core.models

data class FilterModel (val year: Interval?, val timeOfDay: TimesOfDay?)
{
    enum class TimesOfDay {
        Morning,
        Afternoon,
        Evening,
        Night
    }

    val hasAny: Boolean
        get() = year != null || timeOfDay != null
}