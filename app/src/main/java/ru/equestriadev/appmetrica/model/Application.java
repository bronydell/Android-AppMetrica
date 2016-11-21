package ru.equestriadev.appmetrica.model;

import android.content.Context;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import ru.equestriadev.appmetrica.R;

/**
 * Created by Bronydell on 11/12/16.
 */

public class Application extends RealmObject{

    @PrimaryKey
    private int id;

    private String name;
    private String owner_login;
    private String api_key128;
    private String permission;
    private String status;
    private String time_zone_name;

    private RealmList<Install> installs;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner_login() {
        return owner_login;
    }

    public void setOwner_login(String owner_login) {
        this.owner_login = owner_login;
    }

    public String getApi_key128() {
        return api_key128;
    }

    public void setApi_key128(String api_key128) {
        this.api_key128 = api_key128;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermission(Context context){
        int strId = 0;
        switch (permission){
            case "view": strId = R.string.view; break;
            case "edit": strId = R.string.edit; break;
            case "own": strId = R.string.own; break;
            default: strId = R.string.view;
        }
        return context.getString(strId);
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime_zone_name() {
        return time_zone_name;
    }

    public void setTime_zone_name(String time_zone_name) {
        this.time_zone_name = time_zone_name;
    }

    public RealmList<Install> getInstalls() {
        return installs;
    }

    public void setInstalls(RealmList<Install> installs) {
        this.installs = installs;
    }
}
