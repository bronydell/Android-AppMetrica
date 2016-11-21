package ru.equestriadev.appmetrica;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import ru.equestriadev.appmetrica.model.Install;

/**
 * Created by Bronydell on 11/14/16.
 */

public class Arrangement {

    private Context context;

    SimpleDateFormat output_formatter = new SimpleDateFormat("dd.MM.yy");
    SimpleDateFormat compare_formatter = new SimpleDateFormat("yyyy-MM-dd");

    public LineData getInstalls(JSONArray installs, ArrayList<String> dates) {

        LineData answ = new LineData();
        try {
            HashMap<String, Integer> map = new HashMap<>();
            Log.d("DEBUG", "I'm sorry");
            for(int  i = 0; i < installs.length(); i++) {
                String formattedDate = null;
                JSONObject del = installs.getJSONObject(i);
                formattedDate = del.getString("install_datetime").substring(0, 10);
                del = null;
                if (map.containsKey(formattedDate)) {
                    map.put(formattedDate, map.get(formattedDate) + 1);
                } else {
                    map.put(formattedDate, 1);
                    dates.add(formattedDate);
                }
            }
            installs = null;
            System.gc();
            Log.d("Debug", "Why so slow?");
            int x = 0;
            sortDates(dates);

            Log.d("Debug", "Is this sorted, right?");
            LineDataSet line = null;
            for (int i = 0; i < dates.size(); i++) {
                String key = dates.get(i);
                if (line == null) {
                    ArrayList<Entry> entrs = new ArrayList<>();
                    entrs.add(new Entry(x, map.get(key)));
                    line = new LineDataSet(entrs, context.getString(R.string.legend_installs));
                    styleDataSet(Color.RED, line);
                } else
                    line.addEntry(new Entry(x, map.get(key)));
                x++;
            } //wtf?

            for (int i = 0; i < dates.size(); i++) {
                try {
                    dates.set(i, output_formatter.format(compare_formatter.parse(dates.get(i))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            answ.addDataSet(line);

            Log.d("Debug", "Done?");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return answ;

    }

    public LineData byVersion(JSONArray installs, ArrayList<String> dates) {

        LineData answ = new LineData();

        try {
            HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
            for (int i = 0; i < installs.length(); i++) {
                String formattedDate = null;
                JSONObject del = installs.getJSONObject(i);
                formattedDate = del.getString("install_datetime").substring(0, 10);
                if (map.containsKey(formattedDate)) {
                    HashMap<String, Integer> version_map = map.get(formattedDate);
                    if (version_map.containsKey(del.getString("app_version_name")))
                        version_map.put(del.getString("app_version_name"), version_map.get(del.getString("app_version_name")) + 1);
                    else
                        version_map.put(del.getString("app_version_name"), 1);
                    map.put(formattedDate, version_map);
                } else {
                    HashMap<String, Integer> version_map = new HashMap<>();
                    if (!version_map.containsKey(del.getString("app_version_name")))
                        version_map.put(del.getString("app_version_name"), 1);
                    map.put(formattedDate, version_map);
                    dates.add(formattedDate);
                }
                del = null;
            }

            int x = 0;
            sortDates(dates);
            HashMap<String, LineDataSet> lines = new HashMap<>();
            for (int i = 0; i < dates.size(); i++) {
                String key = dates.get(i);
                for (String version : map.get(key).keySet())
                    if (!lines.containsKey(version)) {
                        ArrayList<Entry> pts = new ArrayList<>();
                        pts.add(new Entry(x, map.get(key).get(version)));
                        LineDataSet ds = new LineDataSet(pts, version);
                        Random randomGenerator = new Random();
                        int red = randomGenerator.nextInt(256);
                        int green = randomGenerator.nextInt(256);
                        int blue = randomGenerator.nextInt(256);
                        styleDataSet(Color.rgb(red, green, blue), ds);
                        ds.setLabel(version);
                        lines.put(version, ds);
                    } else
                        lines.get(version).addEntry(new Entry(x, map.get(key).get(version)));
                x++;
            }
            for (String os : lines.keySet()) {
                answ.addDataSet(lines.get(os));
            }

            for (int i = 0; i < dates.size(); i++) {
                try {
                    dates.set(i, output_formatter.format(compare_formatter.parse(dates.get(i))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }
            catch (JSONException e) {
            e.printStackTrace();
        }

        return answ;
    }

    public LineData byOS(JSONArray installs, ArrayList<String> dates) {

        LineData answ = new LineData();

        try {
            HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
            for (int i = 0; i < installs.length(); i++) {
                String formattedDate = null;
                JSONObject del = installs.getJSONObject(i);
                formattedDate = del.getString("install_datetime").substring(0, 10);

                if (map.containsKey(formattedDate)) {
                    HashMap<String, Integer> os_map = map.get(formattedDate);
                    if (os_map.containsKey(del.getString("os_name")))
                        os_map.put(del.getString("os_name"), os_map.get(del.getString("os_name")) + 1);
                    else
                        os_map.put(del.getString("os_name"), 1);
                    map.put(formattedDate, os_map);
                } else {
                    HashMap<String, Integer> os_map = new HashMap<>();
                    if (!os_map.containsKey(del.getString("os_name")))
                        os_map.put(del.getString("os_name"), 1);
                    map.put(formattedDate, os_map);
                    dates.add(formattedDate);
                }
            }

            int x = 0;
            sortDates(dates);
            HashMap<String, LineDataSet> lines = new HashMap<>();
            for (int i = 0; i < dates.size(); i++) {
                String key = dates.get(i);
                for (String os : map.get(key).keySet())
                    if (!map.get(key).containsKey(os) || !lines.containsKey(os)) {
                        ArrayList<Entry> pts = new ArrayList<>();
                        pts.add(new Entry(x, map.get(key).get(os)));
                        LineDataSet ds = new LineDataSet(pts, os);

                        switch (os) {
                            case "android":
                                styleDataSet(Color.GREEN, ds);
                                ds.setLabel("Android");
                                break;
                            case "ios":
                                styleDataSet(Color.BLUE, ds);
                                ds.setLabel("iOS");
                                break;
                            case "windows":
                                styleDataSet(Color.RED, ds);
                                ds.setLabel("WinOS");
                                break;
                            default:
                                styleDataSet(Color.BLACK, ds);
                                ds.setLabel(context.getResources().getString(R.string.legend_others));
                                break;
                        }
                        lines.put(os, ds);
                    } else
                        lines.get(os).addEntry(new Entry(x, map.get(key).get(os)));
                x++;
            }
            for (String os : lines.keySet()) {
                answ.addDataSet(lines.get(os));
            }

            for (int i = 0; i < dates.size(); i++) {
                try {
                    dates.set(i, output_formatter.format(compare_formatter.parse(dates.get(i))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return answ;
    }

    public LineData byTime(JSONArray installs, ArrayList<String> dates) {

        LineData answ = new LineData();

        try {
            HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
            for (int i = 0; i < installs.length(); i++) {
                String formattedDate = null;
                int HH = 0;
                JSONObject del = installs.getJSONObject(i);
                formattedDate = del.getString("install_datetime").substring(0, 10);
                HH = Integer.parseInt(del.getString("install_datetime").substring(11, 13));

                String timer = "Morning";
                if (HH > 11 && HH < 18)
                    timer = "Day";
                else if (HH > 18 || HH < 4) {
                    timer = "Night";
                }
                if (map.containsKey(formattedDate)) {
                    HashMap<String, Integer> time_map = map.get(formattedDate);
                    if (time_map.containsKey(timer))
                        time_map.put(timer, time_map.get(timer) + 1);
                    else
                        time_map.put(timer, 1);
                    map.put(formattedDate, time_map);
                } else {
                    HashMap<String, Integer> time_map = new HashMap<>();
                    if (!time_map.containsKey(timer))
                        time_map.put(timer, 1);
                    map.put(formattedDate, time_map);
                    dates.add(formattedDate);
                }
            }

            int x = 0;
            sortDates(dates);
            HashMap<String, LineDataSet> lines = new HashMap<>();
            for (int i = 0; i < dates.size(); i++) {
                String key = dates.get(i);
                for (String timer : map.get(key).keySet()) {
                    String translated_legend = context.getString(R.string.legend_morning);
                    switch (timer) {
                        case "Day":
                            translated_legend = context.getString(R.string.legend_day);
                            break;
                        case "Night":
                            translated_legend = context.getString(R.string.legend_night);
                            break;
                    }
                    if (!lines.containsKey(timer)) {
                        ArrayList<Entry> pts = new ArrayList<>();
                        pts.add(new Entry(x, map.get(key).get(timer)));
                        LineDataSet ds = new LineDataSet(pts, timer);

                        switch (timer) {
                            case "Morning":
                                styleDataSet(Color.rgb(255, 99, 71), ds);
                                break;
                            case "Day":
                                styleDataSet(Color.rgb(149, 149, 20), ds);
                                break;
                            case "Night":
                                styleDataSet(Color.rgb(139, 0, 139), ds);
                                break;
                            default:
                                styleDataSet(Color.BLACK, ds);
                                break;
                        }
                        ds.setLabel(translated_legend);
                        lines.put(timer, ds);
                    } else
                        lines.get(timer).addEntry(new Entry(x, map.get(key).get(timer)));
                }
                x++;
            }
            for (String timer : lines.keySet()) {
                answ.addDataSet(lines.get(timer));
            }
            for (int i = 0; i < dates.size(); i++) {
                try {
                    dates.set(i, output_formatter.format(compare_formatter.parse(dates.get(i))));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }  catch (JSONException e) {
            e.printStackTrace();
        }

        return answ;
    }

    public void styleDataSet(int color, LineDataSet ds) {
        ds.setColor(color);
        ds.setCircleColor(color);
        ds.setLineWidth(1.5f);
        ds.setDrawCircleHole(false);
        ds.setHighlightEnabled(true);
        ds.setDrawHighlightIndicators(true);
        ds.setHighLightColor(Color.BLACK);
        ds.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return (int) value + "";
            }
        });
    }

    public ArrayList<String> sortDates(ArrayList<String> arr) {
        Collections.sort(arr, new Comparator<String>() {
            DateFormat f = compare_formatter;

            @Override
            public int compare(String o1, String o2) {
                try {
                    return f.parse(o1).compareTo(f.parse(o2));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
        return arr;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
