package si.pocketalbum.core

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import si.pocketalbum.core.models.AlbumInfo
import si.pocketalbum.core.models.ImageInfo
import si.pocketalbum.core.models.YearIndex

class IntegrityCheckerTest {
    @Test
    fun invalidYears() {
        val album = mockk<IAlbum>()
        every { album.getInfo(any()) } returns AlbumInfo(5, 3, 500, 5000, listOf(
            YearIndex(2000, 2, 0U, 0U),
            YearIndex(2001, 4, 0U, 0U),
            YearIndex(2002, 4, 0U, 0U)
        ))

        every { album.getYearIndex() } returns listOf(
            YearIndex(2000, 2, 0x123456U, 2000U),
            YearIndex(2001, 3, 0x654321U, 3000U),
            YearIndex(2003, 1, 0x123123U, 1000U))

        var years = IntegrityChecker.invalidYears(album);
        assertEquals(listOf(2001, 2002, 2003), years);
    }

    @Test
    fun checkYear() {
        val album = mockk<IAlbum>()
        every { album.getInfo(any()) } returns AlbumInfo(2, 1, 200, 2000, listOf(
            YearIndex(2001, 2, 0U, 0U)
        ))
        every { album.list(any(), any()) } returns listOf(
            ImageInfo("abc", "abc.jpg", "image/jpeg", "20010101", 4000, 3000, 10, null, null,
                0xA684C7C6U),
            ImageInfo("def", "def.jpg", "image/jpeg", "20010101", 4000, 3000, 10, null, null,
                0x83DDB0B5U))
        every { album.removeYearIndex(any()) } just Runs
        every { album.storeYearIndex(any()) } just Runs

        IntegrityChecker.checkYear(album, 2001)

        verify { album.removeYearIndex(2001) }

        val yearIndexSlot = slot<YearIndex>()
        verify { album.storeYearIndex(capture(yearIndexSlot)) }
        assertEquals(2001, yearIndexSlot.captured.year)
        assertEquals(2, yearIndexSlot.captured.count)
        assertEquals(0xD9D0CDA6U, yearIndexSlot.captured.crc)
        assertEquals(20.toULong(), yearIndexSlot.captured.size)
    }
}