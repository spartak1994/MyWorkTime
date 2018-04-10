package ru.pabloid.myworktime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class MyWorkTimeService extends Service {

    final String LOG_TAG = "myLogs";

    public MyWorkTimeService() {
    }


    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        someTask();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    void someTask() {
        for (; ; ) {

            try {
                TimeUnit.SECONDS.sleep(1);




            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
