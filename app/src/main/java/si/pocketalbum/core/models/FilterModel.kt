package si.pocketalbum.core.models

data class FilterModel (val year: Interval?, val timeOfDay: TimesOfDay?, val location: BoundingBox?)
{
    companion object {
        val Empty = FilterModel(null, null, null)
    }

    enum class TimesOfDay {
        Morning,
        Day,
        Evening,
        Night
    }

    val hasAny: Boolean
        get() = year != null || timeOfDay != null || location != null
}