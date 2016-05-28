package Classes;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import org.json.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

public class Web {

    //region static variables
    private static final String API = "2f4c0a20237553a41c817ec5940f00bf";
    private static final String authenticationUrl = "http://api.adu.org.za/validation/user/login?";
    private static final String validationUrl = "http://api.adu.org.za/validation/data/access";
    private static String jsonResponse;
    public static String[] projects = null;
    public static String username = "";
    public static String token = "";
    // POST URL to ADU server
    private static final String postUrl = "http://vmus.adu.org.za/api/v1/insertrecord";
    // get all the projects
    private static final String projectUrl = "http://vmus.adu.org.za/api/v1/projects";
    // Keys for all fields
    private static final String TOKEN = "token";
    private static final String API_KEY = "API_KEY";
    private static final String PASS_ID = "passid";
    private static final String USERNAME = "username";
    private static final String USERID = "userid";
    private static final String EMAIL = "email";
    private static final String PROJECT = "project";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "long";
    private static final String MIN_ELEV = "minelev";
    private static final String MAX_ELEV = "maxelev";
    private static final String COUNTRY = "country";
    private static final String PROVINCE = "province";
    private static final String TOWN = "nearesttown";
    private static final String LOCALITY = "locality";
    private static final String DAY = "day";
    private static final String MONTH ="month";
    private static final String YEAR = "year";
    private static final String SOURCE = "source";
    private static final String IMAGES = "images[]";
    private static final String NOTE ="note";
    //endregion

    private static SharedPreferences settings;

    public static Boolean postRecord(Record record) {
        String recSubmissionResponse = "";
        URL url = null;
        BufferedReader reader = null;
        HttpURLConnection urlConn =  null;
        Uri builtUri = Uri.parse(postUrl).buildUpon()
                .appendQueryParameter(API_KEY, API)
                .appendQueryParameter(USERID, String.valueOf(record.getAdu()))
                .appendQueryParameter(USERNAME, record.getUsername())
                .appendQueryParameter(EMAIL, record.getEmail())
                .appendQueryParameter(PROJECT, record.getProject())
                .appendQueryParameter(LATITUDE, String.valueOf(record.getLatitude()))
                .appendQueryParameter(LONGITUDE, String.valueOf(record.getLongitude()))
                .appendQueryParameter(MIN_ELEV, String.valueOf(record.getAltitude()))
                .appendQueryParameter(MAX_ELEV, String.valueOf(record.getAltitude()))
                .appendQueryParameter(COUNTRY, record.getCountry())
                .appendQueryParameter(PROVINCE, record.getProvince())
                .appendQueryParameter(TOWN, record.getTown())
                .appendQueryParameter(LOCALITY, record.getDesc())
                .appendQueryParameter(DAY, String.valueOf(record.getDay()))
                .appendQueryParameter(MONTH, String.valueOf(record.getMonth()))
                .appendQueryParameter(YEAR, String.valueOf(record.getYear()))
                .appendQueryParameter(SOURCE, record.getSource())
                .appendQueryParameter(IMAGES, record.getUrl())
                //.appendQueryParameter(NOTE , record.getNote())
                .build();
        try {
           url = new URL(builtUri.toString());
            Log.i("BUILT URI STRING: ", builtUri.toString());
        } catch(MalformedURLException mal) {
            mal.printStackTrace();
        }

        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.connect();

            InputStream response = urlConn.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (response == null) jsonResponse = null;
            reader = new BufferedReader(new InputStreamReader(response));

            String line;
            while ((line = reader.readLine()) != null) buffer.append(line + "\n");

            if (buffer.length() == 0) jsonResponse = null;

            jsonResponse = buffer.toString(); // response message, so will have token

            JSONObject obj = new JSONObject(jsonResponse);
            //recSubmissionResponse = (String) obj.getJSONObject("registered").getJSONObject("status").get("result");
            Log.i("RESPONSE FROM SERVER", obj.toString());

        } catch (IOException e) {
            jsonResponse = null;
            e.printStackTrace();
        } catch (JSONException j) {
            j.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (urlConn != null) urlConn.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("TEST URI", builtUri.toString());
        return false;
    }


    public static Boolean attemptAduLogin(String email, String adu, String password) {
        String success = "";
        URL url = null;
        HttpURLConnection urlConn =  null;
        BufferedReader reader = null;

        Uri builtUri = Uri.parse(authenticationUrl).buildUpon()
                .appendQueryParameter(API_KEY, API)
                .appendQueryParameter(USERID, adu)
                .appendQueryParameter(EMAIL, email)
                .appendQueryParameter(PASS_ID, MD5(password))
                .build();

        try {
            url = new URL(builtUri.toString());
        } catch(MalformedURLException mal) {
            mal.printStackTrace();
        }

        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.connect();

            InputStream response = urlConn.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (response == null) jsonResponse = null;
            reader = new BufferedReader(new InputStreamReader(response));

            String line;
            while ((line = reader.readLine()) != null) buffer.append(line + "\n");

            if (buffer.length() == 0) jsonResponse = null;

            jsonResponse = buffer.toString(); // response message, so will have token

            JSONObject obj = new JSONObject(jsonResponse);
            Log.i("LOGIN RESPONSE" , obj.toString());
            success = (String) obj.getJSONObject("registered").getJSONObject("status").get("result");
            token = (String) obj.getJSONObject("registered").getJSONObject("status").get("token");
            Log.i("TOKEN", token);

            String firstname = (String) obj.getJSONObject("registered").getJSONObject("data").get("Name");
            String surname = (String) obj.getJSONObject("registered").getJSONObject("data").get("Surname");

            username = firstname + " " + surname;
            Log.i("USERNAME SET TO: ", username);


        } catch (IOException e) {
            jsonResponse = null;
            e.printStackTrace();
        } catch (JSONException j) {
            j.printStackTrace();
        } finally {
            if (urlConn != null) urlConn.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        validate();

        if (success.equals("success")) return true;
        else return false;
    }

    public static void validate(){
        URL url = null;
        HttpURLConnection urlConn =  null;
        BufferedReader reader = null;

        Uri builtUri = Uri.parse(validationUrl).buildUpon()
                .appendQueryParameter(TOKEN, token)
                .build();

        try {
            url = new URL(builtUri.toString());
        } catch(MalformedURLException mal) {
            mal.printStackTrace();
        }

        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.connect();

            InputStream response = urlConn.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (response == null) jsonResponse = null;
            reader = new BufferedReader(new InputStreamReader(response));

            String line;
            while ((line = reader.readLine()) != null) buffer.append(line + "\n");

            if (buffer.length() == 0) jsonResponse = null;

            jsonResponse = buffer.toString(); // response message, so will have token

            JSONObject obj = new JSONObject(jsonResponse);
            Log.i("VALIDATION RESPONSE", obj.toString());


        } catch (IOException e) {
            jsonResponse = null;
            e.printStackTrace();
        } catch (JSONException j) {
            j.printStackTrace();
        } finally {
            if (urlConn != null) urlConn.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static void getProjects() {
        URL url;
        BufferedReader reader = null;
        HttpURLConnection urlConn =  null;

        try {
            url = new URL(projectUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.connect();

            InputStream response = urlConn.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (response == null) jsonResponse = null;
            reader = new BufferedReader(new InputStreamReader(response));

            String line;
            while ((line = reader.readLine()) != null) buffer.append(line + "\n");

            if (buffer.length() == 0) jsonResponse = null;

            jsonResponse = buffer.toString(); // response message, so will have token

            JSONObject obj = new JSONObject(jsonResponse);
            JSONArray arr = obj.getJSONArray("projects");

            if (arr != null) {
                int len = arr.length();
                projects = new String[len];
                for (int i = 0; i < len; i++) projects[i] = arr.getJSONObject(i).getString("Project_acronym");
            }
            Log.i("test", "test");

        } catch (IOException e) {
            jsonResponse = null;
            e.printStackTrace();
        } catch (JSONException j) {
            j.printStackTrace();
        } finally {
            if (urlConn != null) urlConn.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
