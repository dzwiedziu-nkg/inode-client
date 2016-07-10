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

package pl.nkg.iot.inode.example.services;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

import pl.nkg.iot.inode.core.LogProvider;
import pl.nkg.iot.inode.example.MyApplication;
import pl.nkg.iot.inode.example.PreferencesProvider;
import pl.nkg.iot.inode.example.events.LogEvent;

public class PostRestService extends IntentService {

    private static final String TAG = PostRestService.class.getSimpleName();

    private static LinkedBlockingQueue<String> sQueue = new LinkedBlockingQueue<>();

    public PostRestService() {
        super("PostRestService");
    }

    public static void startService(Context context, String content) {
        Intent intent = new Intent(context, PostRestService.class);
        sQueue.add(content);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || sQueue.size() == 0) {
            return;
        }

        MyApplication application = (MyApplication) getApplication();
        PreferencesProvider preferencesProvider = application.getPreferencesProvider();

        if (!preferencesProvider.isUploadToRest()) {
            Log.w(TAG, "Upload to REST is disabled because URL for REST service is blank");
            EventBus.getDefault().post(new LogEvent(LogProvider.WARN, TAG, "Upload to REST is disabled because URL for REST service is blank", null));
            return;
        }

        String urlStr = preferencesProvider.getPrefRest();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(sQueue.poll());

            writer.flush();
            writer.close();
            os.close();

            int response = urlConnection.getResponseCode();


            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String resultToDisplay = IOUtils.toString(in, "UTF-8");
            in.close();

            if (response != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Url response is not HTTP_OK: " + response + "\n\nReturn:\n" + resultToDisplay);
                EventBus.getDefault().post(new LogEvent(LogProvider.ERROR, TAG, "Url response is not HTTP_OK: " + response + "\n\nReturn:\n" + resultToDisplay, null));
            } else {
                Log.d(TAG, "HTTP_OK\n\nReturn:\n" + resultToDisplay);
                EventBus.getDefault().post(new LogEvent(LogProvider.DEBUG, TAG, "HTTP_OK\n\nReturn:\n" + resultToDisplay, null));
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid URL for REST service: " + urlStr, e);
            EventBus.getDefault().post(new LogEvent(LogProvider.ERROR, TAG, "Invalid URL for REST service: " + urlStr, e));
        } catch (IOException e) {
            Log.e(TAG, "Connection error", e);
            EventBus.getDefault().post(new LogEvent(LogProvider.ERROR, TAG, "Connection error" + urlStr, e));
        }
    }
}
