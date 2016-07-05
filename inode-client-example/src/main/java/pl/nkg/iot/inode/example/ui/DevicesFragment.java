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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Set;

import pl.nkg.iot.inode.example.MyApplication;
import pl.nkg.iot.inode.example.R;

public class DevicesFragment extends Fragment implements DevicesAdapter.OnClickListener {

    private static final String TAG = DevicesFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private DevicesAdapter mDevicesAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyApplication mApplication;

    private ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    DevicesAdapter.ViewHolder holder = (DevicesAdapter.ViewHolder) viewHolder;
                    mListener.onNodeRemoved(holder.getValue());
                }
            };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mApplication = (MyApplication) inflater.getContext().getApplicationContext();
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        view.setTag(TAG);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        refreshList();

        return view;
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

    public void refreshList() {
        Set<String> nodesSet = mApplication.getPreferencesProvider().getPrefNodes();
        String[] nodes = nodesSet.toArray(new String[nodesSet.size()]);
        if (mDevicesAdapter == null) {
            mDevicesAdapter = new DevicesAdapter(nodes, this);
            mRecyclerView.setAdapter(mDevicesAdapter);
        } else {
            mDevicesAdapter.setDevices(nodes);
            mDevicesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(DevicesAdapter.ViewHolder holder) {
        mListener.onNodeClick(holder.getValue());
    }

    public interface OnFragmentInteractionListener {
        void onNodeRemoved(String value);

        void onNodeClick(String value);
    }
}
