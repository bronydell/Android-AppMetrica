package ru.equestriadev.appmetrica;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

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

    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy");

    public LineData getInstalls(ArrayList<Install> installs, ArrayList<String> dates) {

        LineData answ = new LineData();
        try {
            HashMap<String, Integer> map = new HashMap<>();
            for (Install install : installs) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                date = parser.parse(install.getInstall_datetime());
                String formattedDate = formatter.format(date);
                if (map.containsKey(formattedDate)) {
                    map.put(formattedDate, map.get(formattedDate) + 1);
                } else {
                    map.put(formattedDate, 1);
                    dates.add(formattedDate);
                }
            }

            int x = 0;
            sortDates(dates);
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
            }


            answ.addDataSet(line);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return answ;
    }

    public LineData byVersion(ArrayList<Install> installs, ArrayList<String> dates) {

        LineData answ = new LineData();

        try {
            HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
            for (Install install : installs) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                date = parser.parse(install.getInstall_datetime());
                String formattedDate = formatter.format(date);
                if (map.containsKey(formattedDate)) {
                    HashMap<String, Integer> version_map = map.get(formattedDate);
                    if (version_map.containsKey(install.getApp_version_name()))
                        version_map.put(install.getApp_version_name(), version_map.get(install.getApp_version_name()) + 1);
                    else
                        version_map.put(install.getApp_version_name(), 1);
                    map.put(formattedDate, version_map);
                } else {
                    HashMap<String, Integer> version_map = new HashMap<>();
                    if (!version_map.containsKey(install.getApp_version_name()))
                        version_map.put(install.getApp_version_name(), 1);
                    map.put(formattedDate, version_map);
                    dates.add(formattedDate);
                }
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
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return answ;
    }

    public LineData byOS(ArrayList<Install> installs, ArrayList<String> dates) {

        LineData answ = new LineData();

        try {
            HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
            for (Install install : installs) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                date = parser.parse(install.getInstall_datetime());
                String formattedDate = formatter.format(date);
                if (map.containsKey(formattedDate)) {
                    HashMap<String, Integer> os_map = map.get(formattedDate);
                    if (os_map.containsKey(install.getOs_name()))
                        os_map.put(install.getOs_name(), os_map.get(install.getOs_name()) + 1);
                    else
                        os_map.put(install.getOs_name(), 1);
                    map.put(formattedDate, os_map);
                } else {
                    HashMap<String, Integer> os_map = new HashMap<>();
                    if (!os_map.containsKey(install.getOs_name()))
                        os_map.put(install.getOs_name(), 1);
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
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return answ;
    }

    public LineData byTime(ArrayList<Install> installs, ArrayList<String> dates) {

        LineData answ = new LineData();

        try {
            HashMap<String, HashMap<String, Integer>> map = new HashMap<>();
            for (Install install : installs) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = null;
                date = parser.parse(install.getInstall_datetime());

                String timer = "Morning";
                if (date.getHours() > 11 && date.getHours() < 18)
                    timer = "Day";
                else if (date.getHours() > 18 || date.getHours() < 4) {
                    timer = "Night";
                }
                String formattedDate = formatter.format(date);
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
        } catch (ParseException e) {
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
            DateFormat f = formatter;

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
