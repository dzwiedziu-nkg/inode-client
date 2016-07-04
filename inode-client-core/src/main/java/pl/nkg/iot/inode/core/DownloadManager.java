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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class DownloadManager implements CommunicationEventListener {

    private static final String TAG = DownloadManager.class.getSimpleName();

    private Machine mMachine = Machine.idle;
    private CommunicationEventType mWaitForEvent;
    private UUID mWaitForServiceUUID;
    private UUID mWaitForCharacteristicUUID;
    private UUID mWaitForDescriptorUUID;

    private boolean mHasError = false;
    private Machine mErrorOnState = Machine.idle;

    private boolean mDoEraseData = false;
    private CommunicationProvider mCommunicationProvider;
    private DateProvider mDateProvider = new DefaultDateProvider();
    private DownloadManagerListener mDownloadManagerListener;
    private LogProvider mLogProvider;

    private int mLastAddress;
    private int mRecordCount;
    private int mBytesToRead;
    private ByteArrayOutputStream mReadBufferStream;

    private ArrayList<DecodedRecord> mDecodedRecords = new ArrayList<>();

    private UUID mService = Statics.UUID_EEPROM_SERVICE;
    private UUID mControlCh = Statics.UUID_EEPROM_CONTROL;
    private UUID mPageCh = Statics.UUID_EEPROM_PAGE;
    private UUID mConfigDesc = Statics.UUID_CLIENT_CONFIG;

    public DownloadManager(CommunicationProvider communicationProvider, DownloadManagerListener downloadManagerListener, LogProvider logProvider) {
        mCommunicationProvider = communicationProvider;
        mDownloadManagerListener = downloadManagerListener;
        mLogProvider = logProvider;
    }

    public ArrayList<DecodedRecord> getDecodedRecords() {
        return mDecodedRecords;
    }

    public boolean isHasError() {
        return mHasError;
    }

    public Machine getMachine() {
        return mMachine;
    }

    public Machine getErrorOnState() {
        return mErrorOnState;
    }

    public boolean isDoEraseData() {
        return mDoEraseData;
    }

    public void setDoEraseData(boolean doEraseData) {
        mDoEraseData = doEraseData;
    }

    public void setDateProvider(DateProvider dateProvider) {
        mDateProvider = dateProvider;
    }

    private void switchState(Machine machine, CommunicationEventType type, UUID service, UUID characteristic, UUID descriptor) {
        mMachine = machine;
        mWaitForEvent = type;
        mWaitForServiceUUID = service;
        mWaitForCharacteristicUUID = characteristic;
        mWaitForDescriptorUUID = descriptor;
        mLogProvider.log(LogProvider.DEBUG, TAG, "State changed: " + machine);
        mDownloadManagerListener.onChangeState();
    }

    private void switchStateWriteControl(Machine machine) {
        switchState(machine, CommunicationEventType.characteristicWrite, mService, mControlCh, null);
    }

    private void switchReadControl() {
        switchState(mMachine, CommunicationEventType.characteristicRead, mService, mControlCh, null);
    }

    private void toggleError() {
        if (mHasError) {
            return;
        }

        mHasError = true;
        mErrorOnState = mMachine;
    }

    @Override
    public void onCommunicationEvent(CommunicationEventType type, byte[] data, UUID service, UUID characteristic, UUID descriptor) {
        if (mMachine == Machine.finish || mMachine == Machine.idle) {
            return;
        }

        if (type.isError) {
            toggleError();
            if (type == CommunicationEventType.timeout) {
                switchStateWriteControl(Machine.setStoreMode);
            } else if (type == CommunicationEventType.disconnected) {
                switchState(Machine.finish, null, null, null, null);
            }
        } else {
            if (validateEvent(type, service, characteristic, descriptor)) {
                processEvent(type, data);
            }
        }
    }

    @Override
    public void enableToRun() {
        if (mMachine == Machine.idle) {
            switchStateWriteControl(Machine.sendTimeToDevice);
            processEvent(null, null);
        }
    }

    private void processEvent(CommunicationEventType type, byte[] data) {
        boolean eventPerformed = false;

        while (!eventPerformed) {
            switch (mMachine) {
                case sendTimeToDevice:
                    eventPerformed = iNodeSendTimeToDevice();
                    break;

                case turnOffStoreData:
                    eventPerformed = iNodeTurnOffStoreData();
                    break;

                case setReadMode:
                    eventPerformed = iNodeSetReadMode();
                    break;

                case requestReadLastRecordAddress:
                    eventPerformed = iNodeRequestReadLastRecordAddress();
                    break;

                case responseReadLastRecordAddress:
                    eventPerformed = iNodeResponseReadLastRecordAddress(type, data);
                    break;

                case requestReadRecordCount:
                    eventPerformed = iNodeRequestReadRecordCount();
                    break;

                case responseReadRecordCount:
                    eventPerformed = iNodeResponseReadRecordCount(type, data);
                    break;

                case setReadAddress:
                    eventPerformed = iNodeSetReadAddress();
                    break;

                case turnOnNotification:
                    eventPerformed = iNodeTurnOnNotification();
                    break;

                case startNotification:
                    eventPerformed = iNodeStartNotification();
                    break;

                case receivingData:
                    eventPerformed = iNodeReceivingData(type, data);
                    break;

                case setEraseAndSetTime:
                    eventPerformed = iNodeSetEraseAndSetTime();
                    break;

                case setStoreMode:
                    eventPerformed = iNodeSetStoreMode();
                    break;

                case turnOnStoreData:
                    eventPerformed = iNodeTurnOnStoreData();
                    break;
            }
        }
    }

    private boolean iNodeSendTimeToDevice() {
        switchStateWriteControl(Machine.turnOffStoreData);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, INodeSpecialQueryFrames.makeSetCurrentTimeFrame(mDateProvider.getCurrentTimestamp()));
        return true;
    }

    private boolean iNodeTurnOffStoreData() {
        switchStateWriteControl(Machine.setReadMode);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_TURN_OFF_STORE_DATA);
        return true;
    }

    private boolean iNodeSetReadMode() {
        switchStateWriteControl(Machine.requestReadLastRecordAddress);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_SET_READ_MODE_REVERSE);
        return true;
    }

    private boolean iNodeRequestReadLastRecordAddress() {
        switchStateWriteControl(Machine.responseReadLastRecordAddress);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_READ_RECORD_LAST_ADDRESS);
        return true;
    }

    private boolean iNodeResponseReadLastRecordAddress(CommunicationEventType type, byte[] data) {
        if (type == CommunicationEventType.characteristicWrite) {
            switchReadControl();
            mCommunicationProvider.requestReadFromCharacteristic(mService, mControlCh);
        } else if (type == CommunicationEventType.characteristicRead) {
            switchStateWriteControl(Machine.requestReadRecordCount);
            mLastAddress = ByteUtils.extractLEU16(data, 0);
            return false;
        }
        return true;
    }

    private boolean iNodeRequestReadRecordCount() {
        switchStateWriteControl(Machine.responseReadRecordCount);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_READ_RECORD_COUNT);
        return true;
    }

    private boolean iNodeResponseReadRecordCount(CommunicationEventType type, byte[] data) {
        if (type == CommunicationEventType.characteristicWrite) {
            switchReadControl();
            mCommunicationProvider.requestReadFromCharacteristic(mService, mControlCh);
        } else if (type == CommunicationEventType.characteristicRead) {
            switchStateWriteControl(Machine.setReadAddress);
            mRecordCount = ByteUtils.extractLEU16(data, 0);
            return false;
        }
        return true;
    }

    private boolean iNodeSetReadAddress() {
        int lenBytes = (8 * mRecordCount) % Statics.MAX_RECORD_COUNT;
        int startAddress = (mLastAddress - lenBytes) & 0xFFFF;
        mBytesToRead = Math.min(8 * mRecordCount, Statics.MAX_RECORD_COUNT);

        mLogProvider.log(LogProvider.DEBUG, TAG, "Start address: " + startAddress + "; Len bytes: " + lenBytes + "; Bytes to read: " + mBytesToRead);

        switchStateWriteControl(Machine.turnOnNotification);

        // As Elsat documentation
        //mCommunicationProvider.writeToCharacteristic(mService, mControlCh, FrameBuilder.makeWithU16LEU16LE(Statics.INODE_SET_READ_SETTINGS, startAddress, lenBytes));
        // but is not correct. Correct is:
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, FrameBuilder.makeWithU16LEU16LE(Statics.INODE_SET_READ_SETTINGS, startAddress, mBytesToRead));

        return true;
    }

    private boolean iNodeTurnOnNotification() {
        switchState(Machine.startNotification, CommunicationEventType.descriptorWrite, mService, mPageCh, mConfigDesc);
        mCommunicationProvider.writeToDescriptor(mService, mPageCh, mConfigDesc, Statics.INODE_TURN_ON_NOTIFICATION);
        return true;
    }

    private boolean iNodeStartNotification() {
        switchStateWriteControl(Machine.receivingData);
        mCommunicationProvider.setCharacteristicNotification(mService, mPageCh, true);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_START_NOTIFICATION);
        return true;
    }

    private boolean iNodeReceivingData(CommunicationEventType type, byte[] data) {
        if (type == CommunicationEventType.characteristicWrite) {
            switchState(Machine.receivingData, CommunicationEventType.characteristicChanged, mService, mPageCh, null);
            mReadBufferStream = new ByteArrayOutputStream();
            mCommunicationProvider.requestNotificationFromCharacteristic(mService, mPageCh);
        } else if (type == CommunicationEventType.characteristicChanged) {
            mReadBufferStream.write(data, 0, data.length);
            mLogProvider.log(LogProvider.DEBUG, TAG, "Received: " + mReadBufferStream.size() + " of " + mBytesToRead + " (" + (mReadBufferStream.size() * 100 / mBytesToRead) + "%)");
            if (mReadBufferStream.size() >= mBytesToRead) {
                mCommunicationProvider.setCharacteristicNotification(mService, mPageCh, false);
                decodeValues(mReadBufferStream.toByteArray());

                try {
                    mReadBufferStream.close();
                } catch (IOException e) {
                }

                mReadBufferStream = null;
                mDownloadManagerListener.onDataAvailable();

                if (mDoEraseData) {
                    switchStateWriteControl(Machine.setEraseAndSetTime);
                } else {
                    switchStateWriteControl(Machine.setStoreMode);
                }

                return false;
            }
            mCommunicationProvider.requestNotificationFromCharacteristic(mService, mPageCh);
        }
        return true;
    }

    private void decodeValues(byte[] bytes) {
        long timestamp = -1;
        int records = Math.min(mBytesToRead, bytes.length) / 8;
        for (int i = 0; i < records; i++) {

            RecordType type = ValuesDecoder.detectRecordType(bytes, i);
            int raw = ValuesDecoder.extractValueFromRecord(bytes, i);
            double value = ValuesDecoder.decode(type, raw);


            if (type == RecordType.timestamp) {
                timestamp = ValuesDecoder.extractTimestampFromRecord(bytes, i);
                mDecodedRecords.add(new DecodedRecord(timestamp, type, value));
            } else if (timestamp != -1) {
                if (ValuesDecoder.validateValue(raw)) {
                    mDecodedRecords.add(new DecodedRecord(timestamp, type, value));
                }
                timestamp += 60 * 1000;
            }
        }
    }

    private boolean iNodeSetEraseAndSetTime() {
        switchStateWriteControl(Machine.setStoreMode);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, INodeSpecialQueryFrames.makeEraseAndSetCurrentTimeFrame(mDateProvider.getCurrentTimestamp()));
        return true;
    }

    private boolean iNodeSetStoreMode() {
        switchStateWriteControl(Machine.turnOnStoreData);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_SET_CYCLIC_ARCHIVE);
        return true;
    }

    private boolean iNodeTurnOnStoreData() {
        switchStateWriteControl(Machine.finish);
        mCommunicationProvider.writeToCharacteristic(mService, mControlCh, Statics.INODE_TURN_ON_STORE_DATA);
        return true;
    }

    private boolean validateEvent(CommunicationEventType type, UUID service, UUID characteristic, UUID descriptor) {
        if (mWaitForEvent == null) {
            return false;
        }

        if (!mWaitForEvent.equals(type)) {
            return false;
        }

        if (!service.equals(mWaitForServiceUUID)) {
            return false;
        }

        if (!characteristic.equals(mWaitForCharacteristicUUID)) {
            return false;
        }

        switch (type) {
            case descriptorRead:
            case descriptorWrite:
                if (!descriptor.equals(mWaitForDescriptorUUID)) {
                    return false;
                }
        }

        return true;
    }


    enum Machine {
        idle(0),
        sendTimeToDevice(10),
        turnOffStoreData(20),
        setReadMode(30),
        requestReadLastRecordAddress(40),
        responseReadLastRecordAddress(45),
        requestReadRecordCount(50),
        responseReadRecordCount(55),
        setReadAddress(80),
        turnOnNotification(90),
        startNotification(100),
        receivingData(110),
        setEraseAndSetTime(120),
        setStoreMode(130),
        turnOnStoreData(140),
        finish(-1);

        public final int step;

        Machine(int step) {
            this.step = step;
        }
    }
}
