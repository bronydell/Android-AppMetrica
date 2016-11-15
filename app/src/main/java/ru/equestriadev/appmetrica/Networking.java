package ru.equestriadev.appmetrica;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ru.equestriadev.appmetrica.model.Install;

/**
 * Created by Bronydell on 11/14/16.
 */

public class Networking {

    private static final String baseURL =
            "https://beta.api-appmetrika.yandex.ru/logs/v1/export/installations.json?";

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
    private String filter = "install_datetime,app_version_name,is_reinstallation,publisher_name,os_name";

    private String date_since = "2016-06-06 00:00:00";
    private String date_until = "2016-11-14 00:00:00";

    public void fetchInstalls(final Context context, final LineChart mChart, final Activity activity, final int mode){
        SharedPreferences mPref = context.getSharedPreferences("YASettings", Context.MODE_PRIVATE);
        AndroidNetworking.get(baseURL + "application_id={app_id}&oauth_token={oauth_token}&date_since={since}&date_until={until}&fields={fields}")
                .addPathParameter("oauth_token", mPref.getString("YAToken", ""))
                .addPathParameter("app_id", appID + "")
                .addPathParameter("since", date_since)
                .addPathParameter("until", date_until)
                .addPathParameter("fields", filter)
                .setPriority(Priority.MEDIUM)
                .build()

                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final ArrayList<String> dates = new ArrayList<String>();
                            JSONObject main = new JSONObject(response);
                            JSONArray toParse = main.getJSONArray("data");
                            Gson gson = new Gson();
                            Type type = new TypeToken<ArrayList<Install>>() {
                            }.getType();

                            ArrayList<Install> installs = gson.fromJson(toParse.toString(), type);
                            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                                @Override
                                public String getFormattedValue(float value, AxisBase axis) {
                                    if(dates.size()>value)
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

                            Arrangement arrangement = new Arrangement();
                            arrangement.setContext(context);
                            LineData lineData = null;
                            switch (mode){
                                case 0: lineData = arrangement.getInstalls(installs, dates); break;
                                case 1: lineData = arrangement.byTime(installs, dates); break;
                                case 2: lineData = arrangement.byOS(installs, dates); break;
                                case 3: lineData = arrangement.byVersion(installs, dates); break;
                            }
                            mChart.setData(lineData);
                            mChart.invalidate();


                        } catch (JSONException e) {
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
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, anError.getErrorBody(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
