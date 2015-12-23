package com.tencent.avsdk.http;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

import java.io.IOException;


public class HttpServer {
    public final static String TAG = HttpServer.class.getName();

    private static final String BASE_URL = "http://211.149.218.190:3000/api/";
    static OkHttpClient client = new OkHttpClient();

    public static String[] onlines() {
        String url = BASE_URL + "onlines";

        Log.i(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = getClient().newCall(request).execute();
            JSONArray ja = new JSONArray(response.body().string());
            String[] users = new String[ja.length()];
            for (int i = 0; i < ja.length(); i++) {
                users[i] = ja.getString(i);
            }
            return users;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return new String[0];
    }

    public static String offline(String username) {
        String url = BASE_URL + "offline/%s";

        url = String.format(url, username);
        Log.i(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = getClient().newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return "";
    }

    public static String startSession(String from, String to) {
        String url = BASE_URL + "session_start/%s/%s";

        url = String.format(url, from, to);
        Log.i(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = getClient().newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return "";
    }

    public static String stopSession(String from, String to) {
        String url = BASE_URL + "session_stop/%s/%s";

        url = String.format(url, from, to);
        Log.i(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = getClient().newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return "";
    }

    public static String online(String username) {
        String url = BASE_URL + "online/%s";

        url = String.format(url, username);
        Log.i(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = getClient().newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        return "";
    }

    private static OkHttpClient getClient() {
        return client;
    }
}
