package pl.nkg.iot.inode.core.devices;

import java.util.HashMap;
import java.util.Map;

import pl.nkg.iot.inode.core.ByteUtils;
import pl.nkg.iot.inode.core.DecodedRecord;
import pl.nkg.iot.inode.core.RecordType;
import pl.nkg.iot.inode.core.ValuesDecoder;

public class INodeFrame {
    private DeviceType mDeviceType;
    private boolean mRtto;
    private boolean mLowBattery;
    private Map<RecordType, DecodedRecord> mRecords;

    public INodeFrame(byte[] frame) {
        mRecords = new HashMap<>();
        mRtto = (frame[5] & (byte) 0b00000100) != 0;
        mLowBattery = (frame[5] & (byte) 0b00001000) != 0;
        switch (frame[6]) {
            case (byte) 0x9D:
                mDeviceType = DeviceType.iNodeCareSensorPHT;
                mRecords.put(RecordType.temperature, new DecodedRecord(
                        System.currentTimeMillis(),
                        RecordType.temperature,
                        ValuesDecoder.decode(RecordType.temperature,
                                ByteUtils.extractLEU16(frame, 13))));

                mRecords.put(RecordType.pressure, new DecodedRecord(
                        System.currentTimeMillis(),
                        RecordType.pressure,
                        ValuesDecoder.decode(RecordType.pressure,
                                ByteUtils.extractLEU16(frame, 11))));

                mRecords.put(RecordType.humidity, new DecodedRecord(
                        System.currentTimeMillis(),
                        RecordType.humidity,
                        ValuesDecoder.decode(RecordType.humidity,
                                ByteUtils.extractLEU16(frame, 15))));
                break;

            case (byte) 0x89:
                mDeviceType = DeviceType.iNodeNAV;
                break;
        }
    }

    public DeviceType getDeviceType() {
        return mDeviceType;
    }

    public boolean isRtto() {
        return mRtto;
    }

    public boolean isLowBattery() {
        return mLowBattery;
    }

    public Map<RecordType, DecodedRecord> getRecords() {
        return mRecords;
    }
}
