package si.pocketalbum.core

import si.pocketalbum.core.models.FilterModel
import si.pocketalbum.core.models.Interval
import si.pocketalbum.core.models.YearIndex
import java.util.function.Consumer

class IntegrityChecker {
    companion object {

        fun checkAllYears(album: IAlbum, progress: Consumer<Double>? = null)
        {
            val invalidYears = invalidYears(album, { p -> progress?.accept(p * 0.1) })
            checkYears(album, invalidYears, { p -> progress?.accept(0.1 + (p * 0.9)) })
        }

        fun checkYears(album: IAlbum, years: List<Int>, progress: Consumer<Double>? = null)
        {
            var cnt = 0;
            for (y in years)
            {
                progress?.accept(cnt.toDouble() / years.size)
                cnt++
                checkYear(album, y)
            }
        }

        fun invalidYears(album: IAlbum, progress: Consumer<Double>? = null): List<Int>
        {
            val info = album.getInfo(FilterModel.Empty)
            val index = album.getYearIndex().toMutableList()

            val invalidYears = mutableListOf<Int>()
            var cnt = 0
            for (yearMap in info.years)
            {
                progress?.accept(cnt.toDouble() / info.years.size)
                cnt++

                var i = index.find{ y -> y.year == yearMap.year }
                if (i == null || i.count != yearMap.count)
                {
                    invalidYears.add(yearMap.year)
                }
                else
                {
                    index.remove(i)
                }
            }

            invalidYears.addAll(index.map{y -> y.year})

            return invalidYears.distinct().sorted().toList()
        }

        fun checkYear(album: IAlbum, year: Int)
        {
            var filter = FilterModel(Interval(year.toLong()), null, null)
            var info = album.getInfo(filter)
            var index = info.years.find {y -> y.year == year}

            if (index == null)
            {
                // There are no images from this year, remove the index
                album.removeYearIndex(year)
                return
            }

            var images = album.list(filter, Interval(0, index.size.toLong())).sortedBy { it.id }

            var crc = images.first().crc
            var size = images.first().size.toULong()
            for (img in images.drop(1))
            {
                val len2 = img.size.toULong()
                crc = CrcUtilities.combineCrc32(crc, img.crc, len2)
                size += len2
            }

            album.removeYearIndex(year)
            album.storeYearIndex(YearIndex(year, images.size, crc, size))
        }
    }
}