package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "023c344f35be20f30c4c58ba58e2a037";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code

        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);

        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(myIntent);
            }
        });

    }

    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() called");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if(city != null)
        {
            getWeatherForNewCity(city);
        }

        else {

            Log.d("Clima", "Getting weather for current location");
            getWeatherForCurrentLocation();
        }
    }

    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city)
    {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);
    }

    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback received");

                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                Log.d("Clima", "Longitude is : " + longitude);
                Log.d("Clima", "Latitude is : " + latitude);

                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);

                letsDoSomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Clima", "onProviderEnabled() callback received");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled() callback received");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d("Clima", "onRequestPermissionResult() : Permission granted!");
                getWeatherForCurrentLocation();
            }

            else
            {
                Log.d("Clima", "onRequestPermissionResult() : Permission denied!");
            }
        }
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL,params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                Log.d("Clima", "Success! JSON : " +response.toString());

                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                assert weatherData != null;
                updateUI(weatherData);
            }

            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response)
            {
                Log.e("Clima", "Fail" + e.toString());
                Log.d("Clima", "Status Code : " +statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: Add updateUI() here:
    public void updateUI(WeatherDataModel weather)
    {
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        RelativeLayout layoutBG = (RelativeLayout) findViewById(R.id.backgroundLayout);
        TextView location = (TextView) findViewById(R.id.locationTV);

        int resourceID = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());

        mWeatherImage.setImageResource(resourceID);

        switch (weather.getIconName())
        {
            case "sunny":
                layoutBG.setBackgroundResource(R.drawable.sunnyday);
                location.setBackgroundColor(Color.parseColor("#8098E448"));
                location.setTextColor(Color.parseColor("#9996F82F"));
                break;
            case "cloudy2":
                layoutBG.setBackgroundResource(R.drawable.clouds);
                location.setBackgroundColor(Color.parseColor("#996696CF"));
                break;
            case "fog":
                layoutBG.setBackgroundResource(R.drawable.fog1);
                location.setTextColor(Color.parseColor("#99D7ECFF"));
                location.setBackgroundColor(Color.parseColor("#80008C8E"));
                break;
            case "light_rain" :
                layoutBG.setBackgroundResource(R.drawable.lightrain);
                location.setBackgroundColor(Color.parseColor("#996696CF"));
                break;
            case "shower3" :
                layoutBG.setBackgroundResource(R.drawable.shower);
                location.setBackgroundColor(Color.parseColor("#80112135"));
                location.setTextColor(Color.parseColor("#992C5B97"));
                break;
            case "tstorm1" :
                layoutBG.setBackgroundResource(R.drawable.wind);
                location.setBackgroundColor(Color.parseColor("#80456721"));
                location.setTextColor(Color.parseColor("#9962932F"));
                break;
            case "tstorm3" :
                layoutBG.setBackgroundResource(R.drawable.storm);
                location.setBackgroundColor(Color.parseColor("#80112135"));
                location.setTextColor(Color.parseColor("#992C5B97"));
                break;
            case "snow4" :
            case "snow5" :
                layoutBG.setBackgroundResource(R.drawable.snow);
                location.setBackgroundColor(Color.parseColor("#80FFFFFF"));
                location.setTextColor(Color.parseColor("#99FFFFFF"));
                break;
            default :
                layoutBG.setBackgroundResource(R.drawable.bg4);
                break;
        }


    }

    // TODO: Add onPause() here:
    @Override
    protected void onPause()
    {
        super.onPause();

        if(mLocationManager != null)
        {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
