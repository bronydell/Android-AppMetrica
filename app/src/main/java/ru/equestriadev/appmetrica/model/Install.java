package ru.equestriadev.appmetrica.model;

/**
 * Created by Bronydell on 11/12/16.
 */

public class Install {

    private String install_datetime;
    private String app_version_name;
    private String os_name;
    private String publisher_name;
    private boolean is_reinstallation;


    public String getInstall_datetime() {
        return install_datetime;
    }

    public void setInstall_datetime(String install_datetime) {
        this.install_datetime = install_datetime;
    }

    public String getApp_version_name() {
        return app_version_name;
    }

    public void setApp_version_name(String app_version_name) {
        this.app_version_name = app_version_name;
    }

    public boolean is_reinstallation() {
        return is_reinstallation;
    }

    public void setIs_reinstallation(boolean is_reinstallation) {
        this.is_reinstallation = is_reinstallation;
    }

    public String getOs_name() {
        return os_name;
    }

    public void setOs_name(String os_name) {
        this.os_name = os_name;
    }

    public String getPublisher_name() {
        return publisher_name;
    }

    public void setPublisher_name(String publisher_name) {
        this.publisher_name = publisher_name;
    }
}
