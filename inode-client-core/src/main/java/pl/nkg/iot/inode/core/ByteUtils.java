/*
 * Copyright (c) by Michał Niedźwiecki 2016
 * Contact: nkg753 on gmail or via GitHub profile: dzwiedziu-nkg
 *
 * This file is part of inode-client.
 *
 * inode-client is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * inode-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package pl.nkg.iot.inode.core;

/**
 * Import/export int/long values between <code>byte[]</code>
 */
public class ByteUtils {

    /**
     * Insert unsigned 32bit low endian value in <code>byte[]</code>.
     *
     * @param dest  <code>byte[]</code> to store value
     * @param pos   array index of first byte to be stored
     * @param value value to be encoded and stored
     */
    public static void insertLEU32(byte[] dest, int pos, long value) {
        dest[pos + 0] = (byte) (value & 0xFF);
        dest[pos + 1] = (byte) ((value >> 8) & 0xFF);
        dest[pos + 2] = (byte) ((value >> 16) & 0xFF);
        dest[pos + 3] = (byte) ((value >> 24) & 0xFF);
    }

    /**
     * Insert unsigned 16bit low endian value in <code>byte[]</code>.
     *
     * @param dest  <code>byte[]</code> to store value
     * @param pos   array index of first byte to be stored
     * @param value value to be encoded and stored
     */
    public static void insertLEU16(byte[] dest, int pos, int value) {
        dest[pos + 0] = (byte) (value & 0xFF);
        dest[pos + 1] = (byte) ((value >> 8) & 0xFF);
    }

    /**
     * Decode unsigned 16bit low endian value from <code>byte[]</code>.
     *
     * @param src source <code>byte[]</code>
     * @param pos array index of first byte to be encoded
     * @return encoded value
     */
    public static int extractLEU16(byte[] src, int pos) {
        return src[pos + 0] & 0xFF | (src[pos + 1] & 0xFF) << 8;
    }

    /**
     * Decode unsigned 32bit low endian value from <code>byte[]</code>.
     *
     * @param src source <code>byte[]</code>
     * @param pos array index of first byte to be encoded
     * @return encoded value
     */
    public static long extractLEU32(byte[] src, int pos) {
        return src[pos + 0] & 0xFF |
                (src[pos + 1] & 0xFF) << 8 |
                (src[pos + 2] & 0xFF) << 16 |
                (src[pos + 3] & 0xFF) << 24;
    }
}
