package com.example.WeatherApp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    String city;
    TextView weatherText;

    class DownloadManager extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {       //used to do background work - you cannot do anything UI related here
            String result = "";
            try {       //reads HTML of entire page
                URL url = new URL(urls[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data;
                char current;
                data = reader.read();
                while (data != -1) {
                    current = (char) data;
                    result += current;
                    data = reader.read();
                }
            }
            catch(Exception e) {
                Log.i("Error", e.getMessage());
                result = "Failed";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {        //used to work with UI
            super.onPostExecute(s);     //s is actually the return argument from doInBackground()
            //Log.i("JSON", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject part, part2;
                String displayInfo = "", main, description;
                double temp, humidity;
                temp = humidity = -1;
                String weatherInfo = jsonObject.getString("weather");       //enter key from JSON data exactly - maintain case
                String weatherInfo2 = jsonObject.getString("main");       //enter key from JSON data exactly - maintain case
                JSONArray jsonArray = new JSONArray(weatherInfo);       //splitting string into array elements
                JSONObject jsonObject2 = new JSONObject(weatherInfo2);       //splitting string into array elements
                for(int i=0; i<jsonArray.length(); i++) {
                    part = jsonArray.getJSONObject(i);      //getting array element
                    main = part.getString("main");      //JSON key
                    description = part.getString("description");        //JSON key
                    if(!main.isEmpty() && !description.isEmpty()) {
                        //displayInfo += main + ": " + description + "\n";        //appending to msg
                        displayInfo += description + ", ";
                    }
                    //Log.i("Info", part.getString("main"));
                    //Log.i("Info", part.getString("description"));
                }
                displayInfo = displayInfo.substring(0, 1).toUpperCase() + displayInfo.substring(1, displayInfo.length()-2) + "\n\n";
                temp = jsonObject2.getDouble("temp");      //JSON key
                humidity = jsonObject2.getDouble("humidity");      //JSON key
                if(temp != -1 && humidity != -1) {
                   displayInfo += "Temperature: " + Double.toString(temp) + " C\nHumidity: " + Double.toString(humidity) + "%\n";        //appending to msg
                }
                //Log.i("Info", String.valueOf(jsonObject2.getDouble("temp")));
                //Log.i("Info", String.valueOf(jsonObject2.getDouble("humidity")));

                //Log.i("Info", weatherInfo2);
                if(!displayInfo.isEmpty()) {
                    weatherText.setText(displayInfo);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Could not find weather", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Log.i("Error", e.getMessage());
                Toast.makeText(getApplicationContext(), "Could not find weather", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkWeather(View view) {
        String result;
        EditText cityName = findViewById(R.id.cityName);
        DownloadManager task = new DownloadManager();
        try {
            city = URLEncoder.encode(cityName.getText().toString(), "UTF-8");       //encodes whitespaces to proper URL lingo
            result = task.execute("https://openweathermap.org/data/2.5/weather?q=" + city + "&appid=439d4b804bc8187953eb36d2a8c26a02").get();
            if(result.equals("Failed")) {
                Toast.makeText(getApplicationContext(), "Could not find weather", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e) {
            Log.i("Error", e.getMessage());
            Toast.makeText(getApplicationContext(), "Could not find weather", Toast.LENGTH_SHORT).show();
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(weatherText.getWindowToken(), 0);       //automatically hides keyboard when button clicked
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherText = findViewById(R.id.weatherInfo);
    }
}