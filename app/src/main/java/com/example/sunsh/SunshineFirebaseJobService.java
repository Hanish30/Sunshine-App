package com.example.sunsh;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.RetryStrategy;

public class SunshineFirebaseJobService extends JobService {
    private AsyncTask<Void,Void,Void> mFetchWeatherTask;


    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {
        mFetchWeatherTask=new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Context context=getApplicationContext();
                SunshineSyncTask.syncWeather(context);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                jobFinished(job,false);
            }
        };
        mFetchWeatherTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        if(mFetchWeatherTask!=null)
        {
            mFetchWeatherTask.cancel(true);
        }
        return true;
    }
}
