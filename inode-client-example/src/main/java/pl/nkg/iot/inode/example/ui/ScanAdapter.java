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

package pl.nkg.iot.inode.example.ui;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import pl.nkg.iot.inode.core.RecordType;
import pl.nkg.iot.inode.core.devices.INodeFrame;

public class ScanAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mDevices;
    private LayoutInflater mInflater;
    private HashMap<String, INodeFrame> mFrames;

    public ScanAdapter(LayoutInflater inflater) {
        super();
        this.mInflater = inflater;
        mDevices = new ArrayList<>();
        mFrames = new HashMap<>();
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addDevice(BluetoothDevice device, int rtto, byte[] data) {
        if (!mDevices.contains(device)) {
            mDevices.add(device);
        }
        mFrames.put(device.getAddress(), new INodeFrame(data));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;

        if (rowView == null) {
            rowView = mInflater.inflate(android.R.layout.simple_list_item_multiple_choice, null);

            // configure view holder
            holder = new ViewHolder();
            holder.mTitleTextView = (CheckedTextView) rowView.findViewById(android.R.id.text1);
            //holder.mSummaryTextView = (TextView) rowView.findViewById(android.R.id.text2);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }


        BluetoothDevice device = mDevices.get(position);
        INodeFrame iNodeFrame = mFrames.get(device.getAddress());

        String values = "";
        switch (iNodeFrame.getDeviceType()) {
            case iNodeCareSensorPHT:
                values = " (";
                values += formatDouble(iNodeFrame.getRecords().get(RecordType.temperature).getValue()) + "°C ";
                values += formatDouble(iNodeFrame.getRecords().get(RecordType.humidity).getValue()) + "% ";
                values += formatDouble(iNodeFrame.getRecords().get(RecordType.pressure).getValue()) + "hPa";
                values += ")";
        }

        holder.mTitleTextView.setText(device.getName() + values);
        //holder.mSummaryTextView.setText(device.getAddress());
        return rowView;
    }

    private static String formatDouble(double v) {
        return String.format(Locale.getDefault(), "%.1f", v);
    }

    static class ViewHolder {
        public CheckedTextView mTitleTextView;
        //public TextView mSummaryTextView;
    }
}
