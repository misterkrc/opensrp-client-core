package org.smartregister;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samuelgithengi on 10/19/18.
 */
public abstract class SyncConfiguration {

    private int connectTimeout = 60000;
    private int readTimeout = 60000;

    public abstract int getSyncMaxRetries();

    public abstract SyncFilter getSyncFilterParam();

    public abstract String getSyncFilterValue();

    public abstract int getUniqueIdSource();

    public abstract int getUniqueIdBatchSize();

    public abstract int getUniqueIdInitialBatchSize();

    //if need to set it true override this method and return true.

    public boolean disableActionService() {
        return false;
    }

    /**
     * Determines whether to sync settings from server side. return false if not
     */
    public boolean isSyncSettings() {
        return false;
    }

    /**
     * Flag that determines whether to sync the data that is on the device to the server if user's account is disabled
     *
     * @return true to disable sync or false to sync data before logout
     */
    public boolean disableSyncToServerIfUserIsDisabled() {
        return false;
    }

    public abstract SyncFilter getEncryptionParam();

    public abstract boolean updateClientDetailsTable();

    /**
     * Returns the read timeout in milliseconds
     * <p>
     * This value will be read when setting the default value for sync read timeout in {@link org.smartregister.sync.intent.BaseSyncIntentService}
     *
     * @return read timeout value in milliseconds
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Returns the connection timeout in milliseconds
     * <p>
     * This value will be read when setting the default value for sync connection timeout in {@link org.smartregister.sync.intent.BaseSyncIntentService}
     *
     * @return connection timeout value in milliseconds
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connection timeout in milliseconds
     * <p>
     * Setting this will call {@link java.net.HttpURLConnection#setConnectTimeout(int)}
     * on the {@link java.net.HttpURLConnection} instance in {@link org.smartregister.service.HTTPAgent}
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Sets the read timeout in milliseconds
     * <p>
     * Setting this will call {@link java.net.HttpURLConnection#setReadTimeout(int)}
     * on the {@link java.net.HttpURLConnection} instance in {@link org.smartregister.service.HTTPAgent}
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }


    /**
     * This method control if POST of GET HTTP method is used to sync clients and events
     *
     * @return true to sync using POST, false to sync using GET
     */
    public boolean isSyncUsingPost() {
        return false;
    }

    /**
     * This method determines the param used for Settings Sync
     *
     * @return the settings sync filter name
     */
    public SyncFilter getSettingsSyncFilterParam() {
        return getSyncFilterParam();
    }

    /**
     * This method determines the param value used for Settings Sync
     *
     * @return the settings sync filter value
     */
    public String getSettingsSyncFilterValue() {
        return getSyncFilterValue();
    }

    /**
     * Gives the ability to specify whether the settings need to be resolved per location
     *
     * @return true/false {@link Boolean}
     */
    public boolean resolveSettings() {
        return false;
    }

    /**
     * Allows the app to also fetch global settings
     *
     * @return true/false -- {@link Boolean}
     */
    public boolean hasGlobalSettings() {
        return true;
    }

    /**
     * We lll allow the app to define it own custom settings parameters
     *
     * @return true/false -- {@link Boolean}
     */
    public boolean hasExtraSettingsSync() {
        return false;
    }

    /**
     * Allows the user to define extra settings sync parameters
     *
     * @return settingsParams {@link List<String>}
     */
    public List<String> getExtraSettingsParameters() {
        return new ArrayList<>();
    }

    public abstract List<String> getSynchronizedLocationTags();

    public abstract String getTopAllowedLocationLevel();

    public boolean clearDataOnNewTeamLogin() {
        return false;
    }

    public boolean runPlanEvaluationOnClientProcessing() {
        return false;
    }

}
