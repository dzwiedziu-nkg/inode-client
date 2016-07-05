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

package pl.nkg.iot.inode.example;


import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class PreferencesProvider {
    public final static String PREF_REST = "rest";
    public final static String PREF_NODES = "nodes";
    public final static String PREF_ERASE = "erase";

    private SharedPreferences sharedPreferences;

    public PreferencesProvider(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * REST service URL. When empty then disabled.
     *
     * @return REST service URL
     */
    public String getPrefRest() {
        return sharedPreferences.getString(PREF_REST, "");
    }

    public void setPrefRest(String rest) {
        sharedPreferences.edit().putString(PREF_REST, rest).apply();
    }

    /**
     * When REST URL is blank then upload to REST is disabled.
     *
     * @return true - enabled, false - disabled
     */
    public boolean isUploadToRest() {
        return !StringUtils.isBlank(getPrefRest());
    }

    /**
     * Set of ID of nodes from data is acquiring.
     *
     * @return set of ID of nodes
     */
    public Set<String> getPrefNodes() {
        return new HashSet<>(sharedPreferences.getStringSet(PREF_NODES, new HashSet<String>()));
    }

    public void setPrefNodes(Set<String> nodes) {
        sharedPreferences.edit().putStringSet(PREF_NODES, nodes).apply();
    }

    /**
     * After download values in IoT device memory must be erased or not.
     *
     * @return true - erase, false - not
     */
    public boolean getPrefErase() {
        return sharedPreferences.getBoolean(PREF_ERASE, false);
    }

    public void setPrefErase(boolean erase) {
        sharedPreferences.edit().putBoolean(PREF_ERASE, erase).apply();
    }
}
