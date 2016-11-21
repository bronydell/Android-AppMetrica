package ru.equestriadev.appmetrica.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import ru.equestriadev.appmetrica.Arrangement;
import ru.equestriadev.appmetrica.InstallsNetworking;
import ru.equestriadev.appmetrica.R;
import ru.equestriadev.appmetrica.model.Application;
import ru.equestriadev.appmetrica.model.Install;

/**
 * A simple {@link Fragment} subclass.
 */
public class InstallsFragment extends Fragment {

    private Realm realm;

    private int appID = 0;
    private int mode = 0;
    private int days_range = 30;
    private String filter = "install_datetime,app_version_name,is_reinstallation,publisher_name,os_name";

    private String date_since = "2016-06-06 00:00:00";
    private String date_until = "2016-11-21 00:00:00";

    private SharedPreferences mPref;
    private InstallsNetworking netw;
    private boolean showedDays = false;
    private boolean showedFilter = false;

    private String modes[];

    private String daysmode[];

    @BindView(R.id.installs_chart)
    LineChart mChart;



    public InstallsFragment() {
        // Required empty public constructor

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_active_users, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        realm = Realm.getDefaultInstance();
        modes = getResources().getStringArray(R.array.filters_array);
        daysmode = getResources().getStringArray(R.array.days_array);
        this.getActivity().setTitle(getString(R.string.install_fragment));
        setHasOptionsMenu(true);
        daysCalculate();
        if(savedInstanceState != null){
            appID = savedInstanceState.getInt("appID");
            date_until = savedInstanceState.getString("until");
            date_since = savedInstanceState.getString("since");
            mode = savedInstanceState.getInt("mode");
            showedFilter = savedInstanceState.getBoolean("filter");
            showedDays = savedInstanceState.getBoolean("days");
        }

        /*if(showedFilter)
            initFilter();
        else if(showedDays)
            initDays();*/



        netw = new InstallsNetworking();
        mPref = getActivity().getApplicationContext().getSharedPreferences("YASettings", Context.MODE_PRIVATE);
        styleChart();
        makeThisRight();
    }

    public void daysCalculate(){
        Date nowADay = new Date();
        nowADay.setHours(0);
        nowADay.setMinutes(0);
        nowADay.setSeconds(0);
        date_until = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nowADay);
        date_since = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calcuteDate(nowADay, -days_range));
        mChart.zoom(0,0,0,0);
    }

    // Furi
    public void makeThisRight(){
        Application application = realm.where(Application.class).equalTo("id", appID).findFirst();
        netw.setApplication(application);
        netw.setAppID(appID);
        netw.setDate_since(date_since);
        netw.setDate_until(date_until);
        netw.setFilter(filter);
        netw.fetchInstalls(getActivity().getApplicationContext(), mChart, getActivity(), mode);
    }

    public void exportDatabase(Context context) {

        // init realm
        Realm realm = Realm.getDefaultInstance();

        File exportRealmFile = null;
        // get or create an "export.realm" file
        exportRealmFile = new File(context.getExternalCacheDir(), "export.realm");

        // if "export.realm" already exists, delete
        exportRealmFile.delete();

        // copy current realm to "export.realm"
        realm.writeCopyTo(exportRealmFile);

        realm.close();

        // init email intent and add export.realm as attachment
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, "YOUR MAIL");
        intent.putExtra(Intent.EXTRA_SUBJECT, "YOUR SUBJECT");
        intent.putExtra(Intent.EXTRA_TEXT, "YOUR TEXT");
        Uri u = Uri.fromFile(exportRealmFile);
        intent.putExtra(Intent.EXTRA_STREAM, u);

        // start email intent
        context.startActivity(Intent.createChooser(intent, "YOUR CHOOSER TITLE"));
    }

    public static InstallsFragment newInstance(int appId) {
        InstallsFragment mFragment = new InstallsFragment();

        Bundle args = new Bundle();
        args.putInt("appId", appId);
        mFragment.setArguments(args);

        return mFragment;
    }

    private void styleChart() {
        mChart.setScaleXEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setScaleYEnabled(false);
        mChart.setDescription(null);


        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);

    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putInt("appID", appID);
        saveState.putString("since", date_since);
        saveState.putString("until", date_until);
        saveState.putInt("mode", mode);
        saveState.putBoolean("filter", showedFilter);
        saveState.putBoolean("days", showedDays);
    }

    private void initFilter(){
        showedFilter = true;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle(getString(R.string.select_filter));

        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showedFilter = false;
                        dialog.dismiss();
                    }
                });

        builderSingle.setSingleChoiceItems(
                modes, mode,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showedFilter = false;
                        mode = which;
                        makeThisRight();
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    private void initDays(){
        showedDays = true;
        int md = 0;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setTitle(getString(R.string.date_range));

        builderSingle.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showedDays = false;
                        dialog.dismiss();
                    }
                });
        if(days_range == 31)
            md = 1;
        else if(days_range == 93)
            md = 2;
        else if(days_range == 365)
            md = 3;
        builderSingle.setSingleChoiceItems(
                daysmode, md,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showedDays = false;
                        switch (which){
                            case 0: days_range = 7; break;
                            case 1: days_range = 31; break;
                            case 2: days_range = 93; break;
                            case 3: days_range = 365; break;
                        }
                        daysCalculate();
                        makeThisRight();
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    public int getAppID() {
        return appID;
    }

    public Date calcuteDate(Date now, int days){
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_filter: initFilter(); return true;
            case R.id.action_days: initDays(); return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
