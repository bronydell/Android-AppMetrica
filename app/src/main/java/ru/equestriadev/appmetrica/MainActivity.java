package ru.equestriadev.appmetrica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import ru.equestriadev.appmetrica.adapters.AppsAdapter;
import ru.equestriadev.appmetrica.model.Application;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener {

    private static final String TAG = "DBGya";
    private static final String appid = "f9198d3e186e4c498ca3f01eaa64a0df";
    private static final String authURL =
            "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + appid;
    private static final String baseURl = "https://beta.api-appmetrika.yandex.ru/management/v1/";

    private Realm realm;

    private SharedPreferences mPref;

    @BindView(R.id.applist)
    ListView mAppsList;

    @BindView(R.id.buttonLogin)
    Button logBtn;

    @BindView(R.id.swipe_to_apps)
    SwipeRefreshLayout swipeRefreshLayout;

    private void LogOut() {
        mPref.edit().putString("YAToken", null).commit();
        mAppsList.setAdapter(null);
        makeItDisappear(false);

        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    public void LogIn(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authURL));
        startActivity(browserIntent);
        swipeRefreshLayout.setRefreshing(false);
        finish();
    }

    private void makeItDisappear(boolean log) {
        if (log) {
            logBtn.setVisibility(View.GONE);
            mAppsList.setVisibility(View.VISIBLE);
        }
        else{
            logBtn.setVisibility(View.VISIBLE);
            mAppsList.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Realm.init(getApplicationContext());
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

        mPref = getApplicationContext().getSharedPreferences("YASettings", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            uri = Uri.parse(uri.toString().replace('#', '?'));
            mPref.edit().putString("YAToken", uri.getQueryParameter("access_token")).apply();
        }

        makeItDisappear(isLogged());

        if(isLogged())
            getApps();
        else
            LogIn(null);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isLogged())
                    getApps();
                else
                    LogIn(null);
            }
        });


    }

    private boolean isLogged(){
        return mPref.getString("YAToken", null) != null;
    }


    private void getApps(){
        AndroidNetworking.get(baseURl + "applications.json?oauth_token={oauth_token}")
                .addPathParameter("oauth_token", mPref.getString("YAToken", ""))
                //.addPathParameter("oauth_token", "not_the_token")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                // do anything with response
                JSONArray toParse = null;
                try {
                    toParse = response.getJSONArray("applications");

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Application.class);
                    ArrayList<Application> apps = mapper.readValue(toParse.toString(),
                            type);
                    Application demoapp = new Application();
                    demoapp.setId(1111);
                    demoapp.setName("Example Application");
                    demoapp.setPermission("view");
                    apps.add(demoapp);
                    realm.beginTransaction();
                    realm.insertOrUpdate(apps);
                    realm.commitTransaction();

                    AppsAdapter adapter = new AppsAdapter(apps, getApplicationContext());
                    mAppsList.setAdapter(adapter);
                    mAppsList.setOnItemClickListener(MainActivity.this);
                    swipeRefreshLayout.setRefreshing(false);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ANError error) {
                // handle error
                if (error.getErrorCode() == 403) {
                    LogOut();
                    Toast.makeText(MainActivity.this, "Something wrong with the key...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Turn on the internet", Toast.LENGTH_SHORT).show();
                    AppsAdapter adapter = new AppsAdapter(getAppsOffline(), getApplicationContext());
                    mAppsList.setAdapter(adapter);
                    mAppsList.setOnItemClickListener(MainActivity.this);
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app_list, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.logout:
                LogOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Application app = (Application) ((AppsAdapter) mAppsList.getAdapter()).getItem(i);
        Intent intent = new Intent(getApplicationContext(), DetailsActivity.class);
        intent.putExtra("appID", app.getId());
        startActivity(intent);
    }

    public ArrayList<Application> getAppsOffline(){
        RealmResults<Application> apps = realm.where(Application.class).findAll();
        ArrayList<Application> apps_array = new ArrayList<>();
        apps_array.addAll(apps.subList(0, apps.size()));
        return apps_array;
    }
}
