package si.pocketalbum.core.models

data class FilterModel (val year: Interval?,val index: Interval?)
{
    val valid: Boolean
        get() = year == null || index == null
}