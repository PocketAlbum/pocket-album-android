package si.pocketalbum.core

class CrcUtilities {
    companion object {
        const val POLY: UInt = 0xedb88320U;

        val x2n_table: Array<Long> = arrayOf(
            0x40000000, 0x20000000, 0x08000000, 0x00800000, 0x00008000,
            0xedb88320, 0xb1e6b092, 0xa06a2517, 0xed627dae, 0x88d14467,
            0xd7bbfe6a, 0xec447f11, 0x8e7ea170, 0x6427800e, 0x4d47bae0,
            0x09fe548f, 0x83852d0f, 0x30362f1a, 0x7b5a9cc3, 0x31fec169,
            0x9fec022a, 0x6c8dedc4, 0x15d6874d, 0x5fde7a4e, 0xbad90e37,
            0x2e4e5eef, 0x4eaba214, 0xa8a472c0, 0x429a969e, 0x148d302a,
            0xc40ba6d0, 0xc4e22c3c
        );

        /*
        Return a(x) multiplied by b(x) modulo p(x), where p(x) is the CRC polynomial,
        reflected. For speed, this requires that a not be zero.
        */
        fun multmodp(a: UInt, b: UInt): UInt
        {
            var x = b;
            var m: UInt = (1 shl 31).toUInt();
            var p: UInt = 0.toUInt();
            while (true) {
                if ((a and m) > 0U)
                {
                    p = p xor x;
                    if ((a and (m - 1U)) == 0U)
                    {
                        break;
                    }
                }
                m = m shr 1;
                x = if ((x and 1U) > 0U) (x shr 1) xor POLY else x shr 1;
            }
            return p;
        }

        /*
          Return x^(n * 2^k) modulo p(x). Requires that x2n_table[] has been
          initialized.
         */
        fun x2nmodp(n: ULong, k: UInt): UInt
        {
            var y = n
            var x = k
            var p: UInt = (1 shl 31).toUInt(); // x^0 == 1
            while (y > 0U) {
                if ((y and 1U) > 0U)
                {
                    p = multmodp(x2n_table[x.toInt() and 31].toUInt(), p);
                }
                y = y shr 1;
                x++;
            }
            return p;
        }

        /**
         * Calculate combined CRC32 value from two CRC values and length the of second part.
         * @param crc1 CRC32 of the first part
         * @param crc2 CRC32 of the second part
         * @param len2 length of the second part in bytes
         * @return combined CRC32 of first and second part concatenated
         */
        fun combineCrc32(crc1: UInt, crc2: UInt, len2: ULong): UInt
        {
            return multmodp(x2nmodp(len2, 3.toUInt()), crc1) xor (crc2 and UInt.MAX_VALUE);
        }
    }
}