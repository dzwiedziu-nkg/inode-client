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
 * Simple class for made <code>byte[]</code> with encoded values
 */
public class FrameBuilder {

    /**
     * Make <code>byte[]</code> with <code>tag</code> as begin and <code>length = tag.length +
     * additionalLength</code>
     *
     * @param tag              source tag
     * @param additionalLength bytes appended after <code>tag</code> length
     * @return copy of <code>tag</code> with <code>appended additionalLength</code> bytes
     */
    public static byte[] make(byte[] tag, int additionalLength) {
        byte[] data = new byte[tag.length + additionalLength];
        System.arraycopy(tag, 0, data, 0, tag.length);
        return data;
    }

    /**
     * Make <code>byte[]</code> with <code>tag</code> as begin and appended unsigned 32bit little
     * endian <code>value</code>.
     *
     * @param tag   source tag
     * @param value unsigned 32bit value to append after <code>tag</code>
     * @return concatenated <code>tag</code> with <code>value</code>
     */
    public static byte[] makeWithU32LE(byte[] tag, long value) {
        byte[] data = make(tag, 4);
        ByteUtils.insertLEU32(data, tag.length, value);
        return data;
    }

    /**
     * Make <code>byte[]</code> with <code>tag</code> as begin and appended unsigned 16bit little
     * endian values.
     *
     * @param tag    source tag
     * @param value1 unsigned 16bit value to append after <code>tag</code>
     * @param value2 unsigned 16bit value to append after <code>value1</code>
     * @return concatenated <code>tag</code> with <code>value1</code> and <code>value2</code>
     */
    public static byte[] makeWithU16LEU16LE(byte[] tag, int value1, int value2) {
        byte[] data = make(tag, 4);
        ByteUtils.insertLEU16(data, tag.length, value1);
        ByteUtils.insertLEU16(data, tag.length + 2, value2);
        return data;
    }
}
