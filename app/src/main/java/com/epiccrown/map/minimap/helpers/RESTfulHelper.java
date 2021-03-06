package com.epiccrown.map.minimap.helpers;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.epiccrown.map.minimap.Preferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class RESTfulHelper {
    private static final String FIRTS_PART_URL = "https://msg.altervista.org/rest/iTracked/";
    private static final String SEND_PATH = "updater/";
    private static final String REGISTER_PATH = "registration/";
    private static final String LOGIN_PATH = "login/";
    private static final String DOWNLOAD_PATH = "search/";
    private static final String CHANGE_USERNAME_PATH = "change_username/";
    private static final String CHANGE_FAMILY_PATH = "change_family/";
    private static final String SEARCH_FAVS_PATH = "search_list/";
    private static final String DELETE_USER_PATH = "delete_user/";

    private Uri ENDPOINT;

    public String deleteUser(String username){
        ENDPOINT = Uri.parse(FIRTS_PART_URL + DELETE_USER_PATH)
                .buildUpon()
                .appendQueryParameter("username", username)
                .build();

        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String searchByJSONList(String JSONlist){
        ENDPOINT = Uri.parse(FIRTS_PART_URL + SEARCH_FAVS_PATH)
                .buildUpon()
                .appendQueryParameter("list", JSONlist)
                .build();

        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String sendInfo(Location location, Context context) {

        if (Preferences.getFamily(context) == null) {
            ENDPOINT = Uri.parse(FIRTS_PART_URL + SEND_PATH)
                    .buildUpon()
                    .appendQueryParameter("idcode", Preferences.getIDcode(context))
                    .appendQueryParameter("username", Preferences.getUsername(context))
                    .appendQueryParameter("latitude", location.getLatitude() + "")
                    .appendQueryParameter("longitude", location.getLongitude() + "")
                    .appendQueryParameter("lastupdate", getUnixTime() + "")
                    .build();
        } else {
            ENDPOINT = Uri.parse(FIRTS_PART_URL + SEND_PATH)
                    .buildUpon()
                    .appendQueryParameter("idcode", Preferences.getIDcode(context))
                    .appendQueryParameter("username", Preferences.getUsername(context))
                    .appendQueryParameter("latitude", location.getLatitude() + "")
                    .appendQueryParameter("longitude", location.getLongitude() + "")
                    .appendQueryParameter("lastupdate", getUnixTime() + "")
                    .appendQueryParameter("family", Preferences.getFamily(context))
                    .build();
        }

        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    public String changeFamily(String family, Context context, boolean isToChange) {
        if (!isToChange)
            ENDPOINT = Uri.parse(FIRTS_PART_URL + CHANGE_FAMILY_PATH)
                    .buildUpon()
                    .appendQueryParameter("username", Preferences.getUsername(context))
                    .appendQueryParameter("idcode", Preferences.getIDcode(context))
                    .appendQueryParameter("family", family)
                    .build();
        else
            ENDPOINT = Uri.parse(FIRTS_PART_URL + CHANGE_FAMILY_PATH)
                    .buildUpon()
                    .appendQueryParameter("username", Preferences.getUsername(context))
                    .appendQueryParameter("idcode", Preferences.getIDcode(context))
                    .appendQueryParameter("family", family)
                    .appendQueryParameter("change", "true")
                    .build();
        if (family.length() == 0)
            ENDPOINT.buildUpon()
                    .appendQueryParameter("null_fam", "true")
                    .build();
        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String changeUsername(String newusername, Context context, boolean isToChange) {
        if (!isToChange)
            ENDPOINT = Uri.parse(FIRTS_PART_URL + CHANGE_USERNAME_PATH)
                    .buildUpon()
                    .appendQueryParameter("username", Preferences.getUsername(context))
                    .appendQueryParameter("idcode", Preferences.getIDcode(context))
                    .appendQueryParameter("new_username", newusername)
                    .build();
        else
            ENDPOINT = Uri.parse(FIRTS_PART_URL + CHANGE_USERNAME_PATH)
                    .buildUpon()
                    .appendQueryParameter("username", Preferences.getUsername(context))
                    .appendQueryParameter("idcode", Preferences.getIDcode(context))
                    .appendQueryParameter("new_username", newusername)
                    .appendQueryParameter("change", "true")
                    .build();
        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String search(String username, String family) {
        if (family == null) {
            ENDPOINT = Uri.parse(FIRTS_PART_URL + DOWNLOAD_PATH)
                    .buildUpon()
                    .appendQueryParameter("username", username)
                    .build();
        } else {
            ENDPOINT = Uri.parse(FIRTS_PART_URL + DOWNLOAD_PATH)
                    .buildUpon()
                    .appendQueryParameter("username", username)
                    .appendQueryParameter("family", family)
                    .build();
        }

        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000 * 60 * 10);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    public String sendUser(String username, String password, String idcode, Context context) {

        ENDPOINT = Uri.parse(FIRTS_PART_URL + REGISTER_PATH)
                .buildUpon()
                .appendQueryParameter("idcode", idcode)
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .build();

        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getUser(String username, String password) {

        ENDPOINT = Uri.parse(FIRTS_PART_URL + LOGIN_PATH)
                .buildUpon()
                .appendQueryParameter("username", username)
                .appendQueryParameter("password", password)
                .build();

        try {
            return getUrlString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }


    public String getUrlString() throws IOException {
        return new String(getUrlBytes(ENDPOINT.toString()));
    }


    @Deprecated
    public String execURL() {
        try {
            URL url = new URL(ENDPOINT.toString());
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            String final_object = "";

            while ((str = in.readLine()) != null)
                final_object += str;

            return final_object;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private long getUnixTime() {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        return now;
    }

    public String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

//            to set cookie use this scheme
//            String myCookie ="cookie=data;";
//            conn.setRequestProperty("Cookie", myCookie);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }else{
                response = "cazzo";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
