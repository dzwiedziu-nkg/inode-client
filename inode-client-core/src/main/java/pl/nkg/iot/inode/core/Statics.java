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

import java.util.UUID;

public class Statics {
    public static final UUID UUID_EEPROM_SERVICE = new UUID(0x0000CB4A5EFD45BEL, 0xB5BE158DF376D8ADL);
    public static final UUID UUID_EEPROM_PAGE = new UUID(0x0000CB4D5EFD45BEL, 0xB5BE158DF376D8ADL);
    public static final UUID UUID_EEPROM_CONTROL = new UUID(0x0000CB4C5EFD45BEL, 0xB5BE158DF376D8ADL);
    public static final UUID UUID_CLIENT_CONFIG = new UUID(0x0000290200001000L, 0x800000805f9b34fbL);

    // UUID_EEPROM_CONTROL
    /// 0x04 0x01 x x x x where x is 32-bit seconds since 01.01.1970 in little endian
    public static final byte[] INODE_SET_CURRENT_TIME = {0x04, 0x01};
    public static final byte[] INODE_TURN_OFF_ARCHIVE_DATA = {0x02, 0x01};
    public static final byte[] INODE_SET_REVERSE_READ = {0x0B, 0x01};

    /// Return: 16-bit little endian unsigned word
    public static final byte[] INODE_READ_RECORD_LAST_ADDRESS = {0x07, 0x01, 0x10, 0x00};

    /// Return: 16-bit little endian unsigned word
    public static final byte[] INODE_READ_RECORD_COUNT = {0x07, 0x01, 0x12, 0x00};

    /// 0x03 0x01 x x y y where x is 16-bit start_addr and y is 16-bit len_bytes, little endian
    public static final byte[] INODE_SET_READ_SETTINGS = {0x03, 0x01};

    // UUID_CLIENT_CONFIG
    public static final byte[] INODE_TURN_ON_NOTIFICATION = {0x01, 0x00};

    // UUID_EEPROM_CONTROL
    public static final byte[] INODE_START_NOTIFICATION = {0x05, 0x01};

    /// 0x04 0x02 x x x x where x is 32-bit seconds since 01.01.1970 in little endian
    public static final byte[] INODE_ERASE_AND_SET_CURRENT_TIME = {0x04, 0x02};
    public static final byte[] INODE_SET_CYCLIC_ARCHIVE = {0x09, 0x01};
    public static final byte[] INODE_TURN_ON_ARCHIVE_DATA = {0x01, 0x01};

    public static final int MAX_RECORD_COUNT = 256 * 256 / 8;
}
