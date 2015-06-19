package de.meisterfuu.openfire.gcm;

/**
 * Created by gologuzov on 18.06.15.
 */
public class GcmPluginConfig {
    private String mGCMHost;
    private int mGCMPort;
    private String mGCMApiKey;
    private String mGCMSengerId;

    public String getmGCMHost() {
        return mGCMHost;
    }

    public void setmGCMHost(String mGCMHost) {
        this.mGCMHost = mGCMHost;
    }

    public int getmGCMPort() {
        return mGCMPort;
    }

    public void setmGCMPort(int mGCMPort) {
        this.mGCMPort = mGCMPort;
    }

    public String getmGCMApiKey() {
        return mGCMApiKey;
    }

    public void setmGCMApiKey(String mGCMApiKey) {
        this.mGCMApiKey = mGCMApiKey;
    }

    public String getmGCMSengerId() {
        return mGCMSengerId;
    }

    public void setmGCMSengerId(String mGCMSengerId) {
        this.mGCMSengerId = mGCMSengerId;
    }
}
