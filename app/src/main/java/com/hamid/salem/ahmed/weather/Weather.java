package com.hamid.salem.ahmed.weather;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

class Weather {

    private static final String OPEN_WEATHER_MAP_URL =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";

    private static final String OPEN_WEATHER_MAP_API = "f6293c91e3a94765cd883c4af8039ec1";//insert your API key here//

    public interface AsyncResponse {

        void processFinish(String output1, String output2, String output3, String output4, String output5, String output6);
    }


    public static class placeIdTask extends AsyncTask<String, Void, JSONObject> {

        AsyncResponse delegate;


        placeIdTask(AsyncResponse asyncResponse) {
            delegate = asyncResponse;

        }

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;
            try {
                jsonWeather = getWeatherJSON(params[0]);
            } catch (Exception e) {
                Log.d("Error", "Cannot process JSON results", e);
            }


            return jsonWeather;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if(json != null){
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();
                    String city = json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country");
                    String description = details.getString("description").toUpperCase(Locale.US);
                    @SuppressLint("DefaultLocale") String temperature = String.format("%.2f", main.getDouble("temp"))+ "°";
                    String humidity = main.getString("humidity") + "%";
                    String pressure = main.getString("pressure") + " hPa";
                    String updatedOn = df.format(new Date(json.getLong("dt")*1000));

                    delegate.processFinish(city, description, temperature, humidity, pressure, updatedOn);

                }
            } catch (JSONException ignored) {

            }

        }
    }

    static JSONObject getWeatherJSON(String nameCity){
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_URL, nameCity));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key", OPEN_WEATHER_MAP_API);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            // This value will be 404 if the request was not successful
            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }
}