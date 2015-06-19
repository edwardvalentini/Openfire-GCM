package de.meisterfuu.openfire.gcm;

import com.google.gson.Gson;
import org.apache.http.client.ClientProtocolException;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNameManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.ParamUtils;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class GcmPlugin implements Plugin, PacketInterceptor {

    private static final Logger Log = LoggerFactory.getLogger(GcmPlugin.class);
    private static final String GCM_HOST = "plugin.gcmh.host";
    private static final String GCM_PORT = "plugin.gcmh.port";
    private static final String GCM_API_KEY = "plugin.gcmh.api_key";
    private static final String GCM_SENDER_ID = "plugin.gcmh.sender_id";

    public static final String DEFAULT_GCM_HOST = "gcm.googleapis.com";
    public static final int DEFAULT_GCM_PORT = 5235;
    public static final String GCM_ELEMENT_NAME = "gcm";
    public static final String GCM_NAMESPACE = "google:mobile:data";


    private static final String MODE = "plugin.gcmh.mode";
    private static final String DEBUG = "plugin.gcmh.debug";

    public static final String MODE_ALL = "1";
    public static final String MODE_OFFLINE = "2";
    public static final String MODE_NO_MOBILE = "3";
    public static final String MODE_EXCEPTION = "4";

    public static final String DEBUG_ON = "1";
    public static final String DEBUG_OFF = "2";

    private CcsClient ccsClient;

    private String mGCMHost;
    private int mGCMPort;
    private String mGCMApiKey;
    private String mGCMSengerId;

    private InterceptorManager interceptorManager;
    private XMPPServer mServer;
    private PresenceManager mPresenceManager;
    private UserManager mUserManager;
    private Gson mGson;

    private String mMode;
    private boolean mDebug = false;

    public GcmPlugin() {
        interceptorManager = InterceptorManager.getInstance();
    }


    private void initConf() {
        mGCMHost = this.getHost();
        mGCMPort = this.getPort();
        mGCMApiKey = this.getApiKey();
        mGCMSengerId = this.getSenderId();
        mMode = this.getMode();
        if (this.getDebug()) {
            mDebug = true;
        } else {
            mDebug = false;
        }
    }

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        Log.info("GCM Plugin started");
        initConf();
        mServer = XMPPServer.getInstance();
        mPresenceManager = mServer.getPresenceManager();
        mUserManager = mServer.getUserManager();
        mGson = new Gson();

        interceptorManager.addInterceptor(this);
        ccsClient = new CcsClient(mGCMHost, mGCMPort, mGCMApiKey, mGCMSengerId);
        try {
            ccsClient.connect();
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    public void destroyPlugin() {
        Log.info("GCM Plugin destroyed");
        interceptorManager.removeInterceptor(this);
        ccsClient.disconnect();
    }

    public void interceptPacket(Packet packet, Session session,
                                boolean incoming, boolean processed) throws PacketRejectedException {

        if (processed) {
            return;
        }
        if (!incoming) {
            return;
        }

        if (packet instanceof Message) {
            Message msg = (Message) packet;
            process(msg);
        }

    }

    private void process(final Message msg) {
        if (mDebug) Log.info("GCM Plugin process() called");
        try {
            if (checkTarget(msg)) {
                if (mDebug) Log.info("GCM Plugin Check=true");
                TimerTask messageTask = new TimerTask() {
                    @Override
                    public void run() {
                        sendExternalMsg(msg);
                    }
                };
                TaskEngine.getInstance().schedule(messageTask, 20);
            } else {
                if (mDebug) Log.info("GCM Plugin Check=false");
            }
        } catch (UserNotFoundException e) {
            Log.error("GCM Plugin (UserNotFoundException) Something went reeeaaaaally wrong");
            e.printStackTrace();
            // Something went reeeaaaaally wrong if you end up here!!
        }
    }

    private boolean checkTarget(Message msg) throws UserNotFoundException {
        if (msg.getBody() == null || msg.getBody().equals("")) {
            return false;
        }

        JID toJID = msg.getTo().asBareJID();
        if (mDebug) Log.info("GCM Plugin check() called");

        if (!toJID.getDomain().contains(mServer.getServerInfo().getXMPPDomain())) {
            return false;
        }

        if (mMode.equalsIgnoreCase(GcmPlugin.MODE_ALL)) {
            return true;
        } else if (mMode.equalsIgnoreCase(GcmPlugin.MODE_OFFLINE)) {

            String y = UserNameManager.getUserName(toJID);
            if (mDebug) Log.info("GCM Plugin getUserName(...) = " + y);
            User x = mUserManager.getUser(y);
            if (mDebug) Log.info("GCM Plugin getUser(...) = " + x.toString());
            try {
                Presence z = mPresenceManager.getPresence(x);
                if (z == null) return true;
                if (mDebug) Log.info("GCM Plugin getPresence(...) = " + z.toString());
                return !z.isAvailable();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (mMode.equalsIgnoreCase(GcmPlugin.MODE_NO_MOBILE)) {

        } else if (mMode.equalsIgnoreCase(GcmPlugin.MODE_EXCEPTION)) {

        }

        return true;
    }

    private void sendExternalMsg(Message msg) {
        if (mDebug) Log.info("GCM Plugin sendExternalMsg() called");

        if (!ccsClient.isConnected()) {
            Log.error("GCM Plugin: Not connected to GCM");
            return;
        }

        EventObject temp = new EventObject();
        temp.setBody(msg.getBody());
        temp.setTo(msg.getTo().toBareJID());
        temp.setFrom(msg.getFrom().toBareJID());


        try {
            // Send a sample hello downstream message to a device.
            Map<String, String> payload = new HashMap<String, String>();
            payload.put("message", msg.getBody());
            String collapseKey = "sample";
            Long timeToLive = 10000L;
            Boolean delayWhileIdle = true;
            ccsClient.send(ccsClient.createJsonMessage("/topics/global", msg.getID(), payload, collapseKey,
                    timeToLive, delayWhileIdle));
        } catch (Exception e) {
            Log.error("GCM Plugin: (Unknown)Exception");
            Log.error("GCM Plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public String getHost() {
        return JiveGlobals.getProperty(GCM_HOST, DEFAULT_GCM_HOST);
    }

    public void setHost(String gcmHost) {
        JiveGlobals.setProperty(GCM_HOST, gcmHost);
        initConf();
    }

    public int getPort() {
        return JiveGlobals.getIntProperty(GCM_PORT, DEFAULT_GCM_PORT);
    }

    public void setPort(int gcmPort) {
        JiveGlobals.setProperty(GCM_PORT, String.valueOf(gcmPort));
        initConf();
    }

    public String getSenderId() {
        return JiveGlobals.getProperty(GCM_SENDER_ID, null);
    }

    public void setSenderId(String gcmSenderId) {
        JiveGlobals.setProperty(GCM_SENDER_ID, gcmSenderId);
        initConf();
    }

    public String getApiKey() {
        return JiveGlobals.getProperty(GCM_API_KEY, null);
    }

    public void setApiKey(String gcmApiKey) {
        JiveGlobals.setProperty(GCM_API_KEY, gcmApiKey);
        initConf();
    }

    public String getMode() {
        return JiveGlobals.getProperty(MODE, GcmPlugin.MODE_ALL);
    }

    public void setMode(String mode) {
        JiveGlobals.setProperty(MODE, mode);
        initConf();
    }

    public boolean getDebug() {
        if (JiveGlobals.getProperty(DEBUG, GcmPlugin.DEBUG_OFF).equalsIgnoreCase(GcmPlugin.DEBUG_ON)) {
            return true;
        } else {
            return false;
        }
    }

    public void setDebug(boolean mode) {
        if (mode) {
            JiveGlobals.setProperty(DEBUG, GcmPlugin.DEBUG_ON);
        } else {
            JiveGlobals.setProperty(DEBUG, GcmPlugin.DEBUG_OFF);
        }
        initConf();
    }
}
