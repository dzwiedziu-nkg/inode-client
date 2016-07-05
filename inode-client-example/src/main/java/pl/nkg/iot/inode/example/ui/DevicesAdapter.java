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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.nkg.iot.inode.example.R;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {

    private String[] mDevices;
    private OnClickListener mOnClickListener;

    public DevicesAdapter(String[] devices, OnClickListener onClickListener) {
        mDevices = devices;
        mOnClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);

        return new ViewHolder(v, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setValue(mDevices[position]);
    }

    @Override
    public int getItemCount() {
        return mDevices.length;
    }

    public void setDevices(String[] devices) {
        mDevices = devices;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private String value;

        public ViewHolder(View v, final OnClickListener onClickListener) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(ViewHolder.this);
                }
            });
            textView = (TextView) v.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
            textView.setText(value);
        }
    }

    public interface OnClickListener {
        void onClick(ViewHolder holder);
    }
}
