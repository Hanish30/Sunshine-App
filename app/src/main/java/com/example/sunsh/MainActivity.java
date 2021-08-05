package com.example.sunsh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ForecastAdapter.ForecastAdapterOnClickHandler {

    private static final String  TAG=MainActivity.class.getSimpleName();
    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };
    private TextView mWeatherTextView;
    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;
    private static int FORECAST_LOADER_ID=0;
    private int mPosition = RecyclerView.NO_POSITION;
    private ProgressBar mProgressDisplay;
    private static final int ID_FORECAST_LOADER = 44;
    private TextView merrorMessageTextView;
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);
        FakeDataUtils.insertFakeData(this);

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */
        mProgressDisplay=(ProgressBar) findViewById(R.id.progress_bar_view);
        mRecyclerView=(RecyclerView) findViewById(R.id.recyclerview_forecast) ;
        LinearLayoutManager layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        int loaderId=FORECAST_LOADER_ID;
        mForecastAdapter=new ForecastAdapter(this,this);
       // LoaderManager.LoaderCallbacks<Cursor> callback =  MainActivity.this;
        mRecyclerView.setAdapter(mForecastAdapter);
        Log.d(TAG, "onCreate: registering preference changed listener");
       // PreferenceManager.getDefaultSharedPreferences(this).
        //        registerOnSharedPreferenceChangeListener(this);
        showLoading();
        //Bundle bundleForLoader=null;
      getSupportLoaderManager().initLoader(ID_FORECAST_LOADER,null,this);
        // COMPLETED (4) Delete the dummy weather data. You will be getting REAL data from the Internet in this lesson.

        // COMPLETED (3) Delete the for loop that populates the TextView with dummy data

        // COMPLETED (9) Call loadWeatherData to perform the network request to get the weather

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PREFERENCES_HAVE_BEEN_UPDATED)
        {
            Log.d(TAG,"the prefernece has been changed reload the loader");
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID,null,this);
            PREFERENCES_HAVE_BEEN_UPDATED=false;
        }
    }
    // COMPLETED (8) Create a method that will get the user's preferred location and execute your new AsyncTask and call it loadWeatherData
    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */

   // private void invalidateData() {
     //   mForecastAdapter.wea(null);
   // }

    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mProgressDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }
    private void showLoading() {
        /* Then, hide the weather data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mProgressDisplay.setVisibility(View.VISIBLE);
    }
    private void openLocationInMap() {
        // COMPLETED (9) Use preferred location rather than a default location to display in the map
      //  String addressString = SunshinePreferences.getPreferredWeatherLocation(this);
      //  Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);
        double[] coords = SunshinePreferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
        }
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {
        switch (id)
        {
            case ID_FORECAST_LOADER:
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);


        }

    }



    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
       // COMPLETED (27) Remove the previous body of onLoadFinished

//      COMPLETED (28) Call mForecastAdapter's swapCursor method and pass in the new Cursor
        mForecastAdapter.swapCursor(data);
//      COMPLETED (29) If mPosition equals RecyclerView.NO_POSITION, set it to 0
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
//      COMPLETED (30) Smooth scroll the RecyclerView to mPosition
        mRecyclerView.smoothScrollToPosition(mPosition);

//      COMPLETED (31) If the Cursor's size is not equal to 0, call showWeatherDataView
        if (data.getCount() != 0) showWeatherDataView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

   // @Override
   // public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    //    PREFERENCES_HAVE_BEEN_UPDATED=true;

   // }
    // COMPLETED (4) When the load is finished, show either the data or an error message if there is no data
    /**
     * Called when a previously created loader has finished its load.
*/



    // COMPLETED (4) When the load is finished, show either the data or an error message if there is no data








    // COMPLETED (5) Create a class that extends AsyncTask to perform network requests
   // public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        // COMPLETED (6) Override the doInBackground method to perform your network requests

    //    @Override
    //    protected void onPreExecute() {
   //         super.onPreExecute();
   //         mProgressDisplay.setVisibility(View.VISIBLE);
   //     }

  //      @Override
  //      protected String[] doInBackground(String... params) {

            /* If there's no zip code, there's nothing to look up. */
   //         if (params.length == 0) {
   //             return null;
  //          }

       //     String location = params[0];
       //     URL weatherRequestUrl = NetworkUtils.buildUrl(location);

       //     try {
        //        String jsonWeatherResponse = NetworkUtils
               //         .getResponseFromHttpUrl(weatherRequestUrl);

             //   String[] simpleJsonWeatherData = OpenWeatherJsonUtils
             //           .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

             //   return simpleJsonWeatherData;

           // } catch (Exception e) {
          //      e.printStackTrace();
          //      return null;
         //   }
      //  }

        // COMPLETED (7) Override the onPostExecute method to display the results of the network request
      //  @Override
      //  protected void onPostExecute(String[] weatherData) {
      //      mProgressDisplay.setVisibility(View.INVISIBLE);
      //      if (weatherData != null) {
                /*
                 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
              //  showWeatherDataView();
           //     mForecastAdapter.setWeatherData(weatherData);
          //  }
         //   else
         //   {
          //    showErrorMessage();
         //   }
     //   }

   // }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.menu_forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
       // if(id==R.id.refresh_it)
       // {
       //     invalidateData();
       //     getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID,null,this);
       //     return true;
      //  }
        if (id == R.id.action_map) {
            openLocationInMap();
            return true;
        }
        if(id==R.id.action_settings)
        {
            Intent intent=new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
//      COMPLETED (39) Refactor onClick to pass the URI for the clicked date with the Intent
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }
}
