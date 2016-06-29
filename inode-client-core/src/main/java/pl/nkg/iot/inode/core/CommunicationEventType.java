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
 * Event types from communication providers.
 */
public enum CommunicationEventType {
    /// Received data from BTLE descriptor
    descriptorRead(true, false),

    /// Received data from BTLE characteristic
    characteristicRead(true, false),

    /// Received data from BTLE characteristic notification
    characteristicChanged(true, false),

    /// Confirmation of finish of write data to BTLE descriptor
    descriptorWrite(false, false),

    /// Confirmation of finish of write data to BTLE characteristic
    characteristicWrite(false, false),

    /// No new data since specified time
    timeout(false, true),

    /// Connection lost
    disconnected(false, true);

    /// Event has new data for processing
    public final boolean hasData;

    /// Event is a error event
    public final boolean isError;

    CommunicationEventType(boolean hasData, boolean isError) {
        this.hasData = hasData;
        this.isError = isError;
    }
}
