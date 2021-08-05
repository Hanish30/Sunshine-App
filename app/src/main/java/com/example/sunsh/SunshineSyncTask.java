package com.example.sunsh;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import java.net.URL;

public class SunshineSyncTask {
       synchronized public static void syncWeather(Context context)
       {
             try
             {
                 URL weatherRequestUrl=NetworkUtils.getUrl(context);
                 String jsonWeatherResponse=NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);
                 ContentValues[] weatherValues=OpenWeatherJsonUtils.getWeatherContentValuesFromJson(context,jsonWeatherResponse);
                 if(weatherValues!=null&&weatherValues.length!=0)
                 {
                     ContentResolver sunshineContentResolver = context.getContentResolver();

//              COMPLETED (4) If we have valid results, delete the old data and insert the new
                     /* Delete old weather data because we don't need to keep multiple days' data */
                     sunshineContentResolver.delete(
                             WeatherContract.WeatherEntry.CONTENT_URI,
                             null,
                             null);

                     /* Insert our new weather data into Sunshine's ContentProvider */
                     sunshineContentResolver.bulkInsert(
                             WeatherContract.WeatherEntry.CONTENT_URI,
                             weatherValues);
                    // COMPLETED (13) Check if notifications are enabled
                     /*
                      * Finally, after we insert data into the ContentProvider, determine whether or not
                      * we should notify the user that the weather has been refreshed.
                      */
                     boolean notificationsEnabled = SunshinePreferences.areNotificationsEnabled(context);

                     /*
                      * If the last notification was shown was more than 1 day ago, we want to send
                      * another notification to the user that the weather has been updated. Remember,
                      * it's important that you shouldn't spam your users with notifications.
                      */
                     long timeSinceLastNotification = SunshinePreferences
                             .getEllapsedTimeSinceLastNotification(context);

                     boolean oneDayPassedSinceLastNotification = false;

//              COMPLETED (14) Check if a day has passed since the last notification
                     if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                         oneDayPassedSinceLastNotification = true;
                     }

                     /*
                      * We only want to show the notification if the user wants them shown and we
                      * haven't shown a notification in the past day.
                      */
//              COMPLETED (15) If more than a day have passed and notifications are enabled, notify the user
                     if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                         NotificationUtils.notifyUserOfNewWeather(context);
                     }
                 }
             }
             catch (Exception e)
             {
                 e.printStackTrace();
             }
       }
}
