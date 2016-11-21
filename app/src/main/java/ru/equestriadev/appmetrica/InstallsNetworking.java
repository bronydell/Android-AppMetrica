package ru.equestriadev.appmetrica;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.realm.Realm;
import ru.equestriadev.appmetrica.model.Application;
import ru.equestriadev.appmetrica.model.Install;

/**
 * Created by Bronydell on 11/14/16.
 */

public class InstallsNetworking {

    private static final String baseURL =
            "https://beta.api-appmetrika.yandex.ru/logs/v1/export/installations.json?";
    private Application application;

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getDate_since() {
        return date_since;
    }

    public void setDate_since(String date_since) {
        this.date_since = date_since;
    }

    public String getDate_until() {
        return date_until;
    }

    public void setDate_until(String date_until) {
        this.date_until = date_until;
    }

    private int appID = 0;
    private String filter = "install_datetime,app_version_name,publisher_name,os_name";

    private String date_since = "2016-06-06 00:00:00";
    private String date_until = "2016-11-14 00:00:00";

    public void fetchInstalls(final Context context, final LineChart mChart, final Activity activity, final int mode){
        SharedPreferences mPref = context.getSharedPreferences("YASettings", Context.MODE_PRIVATE);
        Toast.makeText(context, date_since, Toast.LENGTH_SHORT).show();
        AndroidNetworking.get(baseURL + "application_id={app_id}&oauth_token={oauth_token}&date_since={since}&date_until={until}&fields={fields}")
                .addPathParameter("oauth_token", mPref.getString("YAToken", ""))
                .addPathParameter("app_id", appID + "")
                .addPathParameter("since", date_since)
                .addPathParameter("until", date_until)
                .addPathParameter("fields", filter)
                .setPriority(Priority.MEDIUM)
                .build()

                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final ProgressDialog dialog = ProgressDialog.show(activity, "Data processing...",
                                "Just wait...", true);
                            final ArrayList<String> dates = new ArrayList<String>();
                            JSONArray toParse = response.getJSONArray("data");
                            response = null;
                            System.gc();

                            InstallsTask task = new InstallsTask();
                            task.setContext(context);
                            task.setmChart(mChart);
                            task.setMode(mode);
                            task.setDialog(dialog);
                            task.setActivity(activity);
                            task.execute(toParse);


                        } catch (JSONException e) {

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        final ProgressDialog dialog = ProgressDialog.show(activity, "Waiting for YA response...",
                                "Waiting for data...", true);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions to do after 10 seconds
                                dialog.dismiss();
                                fetchInstalls(context, mChart, activity, mode);
                            }
                        }, 6 * 1000);
                    }
                });

    }

    private class InstallsTask extends AsyncTask<JSONArray, Void, LineData> {

        private ArrayList<String> dates;
        private int mode;
        private Context context;
        private LineChart mChart;
        private ProgressDialog dialog;
        private Activity activity;
        @Override
        protected void onPreExecute(){
            dates = new ArrayList<>();
        }
        @Override
        protected LineData doInBackground(JSONArray ... toParse) {

                Arrangement arrangement = new Arrangement();
                arrangement.setContext(context);

                switch (mode){
                    case 0: return arrangement.getInstalls(toParse[0] , dates);
                    case 1: return arrangement.byTime(toParse[0], dates);
                    case 2: return arrangement.byOS(toParse[0], dates);
                    case 3: return arrangement.byVersion(toParse[0], dates);
                }

            return null;
        }

        @Override
        protected void onPostExecute( LineData result) {

            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    if(dates.size()>value&&value>-1)
                        return dates.get((int) value);
                    else
                        return "-?";
                }

                // we don't draw numbers, so no decimal digits needed
                @Override
                public int getDecimalDigits() {  return 0; }
            };

            XAxis xAxis = mChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);



            mChart.setData(result);
            mChart.invalidate();
            if(dialog!=null)
                dialog.dismiss();
        }

        public void setmChart(LineChart mChart) {
            this.mChart = mChart;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        public void setDialog(ProgressDialog dialog) {
            this.dialog = dialog;
        }
    }


    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }


}
