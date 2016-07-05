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
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.HashSet;
import java.util.Set;

public class ScanFragment extends ListFragment {
    private OnFragmentInteractionListener mListener;
    private ScanAdapter mScanAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);
        mScanAdapter = new ScanAdapter(getActivity().getLayoutInflater());
        ((ListView) listFragmentView.findViewById(android.R.id.list)).setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListAdapter(mScanAdapter);
        return listFragmentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void addDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mScanAdapter.addDevice(device);
        mScanAdapter.notifyDataSetChanged();
    }

    public Set<BluetoothDevice> getChecked() {
        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        HashSet<BluetoothDevice> selectedItems = new HashSet<>();
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            if (checked.valueAt(i)) {
                selectedItems.add((BluetoothDevice) mScanAdapter.getItem(position));
            }
        }

        return selectedItems;
    }

    public interface OnFragmentInteractionListener {
        //void onRefreshBookList(boolean force);

        //void onListItemClick(Book book);
    }
}
