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

public class ValuesDecoder {
    public static RecordType detectRecordType(byte[] bytes, int record) {
        if (bytes[record * 8] == (byte) 0xa0) {
            return RecordType.timestamp;
        } else {
            int kind = bytes[record * 8 + 5];

            switch (kind) {
                case 1:
                    return RecordType.temperature;

                case 2:
                    return RecordType.humidity;

                case 4:
                    return RecordType.pressure;
            }
        }

        return null;
    }

    public static long extractTimestampFromRecord(byte[] bytes, int record) {
        return INodeSpecialQueryFrames.u32ToTimestamp(ByteUtils.extractLEU32(bytes, record * 8 + 1));
    }

    public static int extractValueFromRecord(byte[] bytes, int record) {
        return ByteUtils.extractLEU16(bytes, record * 8 + 6);
    }

    public static boolean validateValue(int value) {
        return value != 0xFFFF;
    }

    public static double decode(RecordType type, int value) {
        switch (type) {
            case timestamp:
                return value;

            case temperature:
                return (175.72 * value * 4.0 / 65536.0) - 46.85;

            case humidity:
                return (125.0 * value * 4.0 / 65536.0) - 6.0;

            case pressure:
                return value / 16.0;
        }

        return 0;
    }
}
