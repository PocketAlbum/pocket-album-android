package si.pocketalbum.core.models

data class Interval(val from: Long, val to: Long) {

    init {
        require(to >= from) { "Value 'from' must be less than or equal to 'to'" }
    }

    val singleValue: Boolean
        get() = from == to

    constructor(value: Long) : this(value, value)
}
