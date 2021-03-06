package com.kaltura.playkit.plugins.Youbora;

import android.content.Context;

import com.google.gson.JsonObject;
import com.kaltura.playkit.MessageBus;
import com.kaltura.playkit.PKEvent;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKPlugin;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.npaw.youbora.youboralib.data.Options;

import java.util.Map;

/**
 * Created by zivilan on 02/11/2016.
 */

public class YouboraPlugin extends PKPlugin {
    private static final PKLog log = PKLog.get("YouboraPlugin");

    private static YouboraLibraryManager pluginManager;
    private static YouboraAdManager adsManager;

    private PKMediaConfig mediaConfig;
    private JsonObject pluginConfig;
    private Player player;
    private MessageBus messageBus;
    private boolean adAnalytics = false;
    private boolean isMonitoring = false;
    private boolean isAdsMonitoring = false;

    public static final Factory factory = new Factory() {
        @Override
        public String getName() {
            return "Youbora";
        }

        @Override
        public PKPlugin newInstance() {
            return new YouboraPlugin();
        }

        @Override
        public void warmUp(Context context) {
            
        }
    };


    @Override
    protected void onUpdateMedia(PKMediaConfig mediaConfig) {
        stopMonitoring();
        log.d("youbora - onUpdateMedia");
        this.mediaConfig = mediaConfig;
        Map<String, Object> opt  = YouboraConfig.getYouboraConfig(pluginConfig, this.mediaConfig, player);
        // Refresh options with updated media
        pluginManager.setOptions(opt);
        startMonitoring(player);
    }

    @Override
    protected void onUpdateConfig(Object config) {
        log.d("youbora - onUpdateConfig");
        this.pluginConfig = (JsonObject) config;
        Map<String, Object> opt  = YouboraConfig.getYouboraConfig(pluginConfig, mediaConfig, player);
        // Refresh options with updated media
        pluginManager.setOptions(opt);
    }

    @Override
    protected void onApplicationPaused() {
        pluginManager.pauseMonitoring();
    }

    @Override
    protected void onApplicationResumed() {
        pluginManager.resumeMonitoring();
    }

    @Override
    public void onDestroy() {
        if (isMonitoring) {
            stopMonitoring();
        }
    }

    @Override
    protected void onLoad(final Player player, Object config, final MessageBus messageBus, Context context) {
        this.player = player;
        this.pluginConfig = (JsonObject) config;
        this.messageBus = messageBus;
        pluginManager = new YouboraLibraryManager(new Options(), messageBus, mediaConfig, player);
        loadPlugin();
    }

    private void loadPlugin(){
        log.d("loadPlugin");
        if (pluginConfig != null) {
            if (!pluginConfig.has("youboraConfig") || pluginConfig.get("youboraConfig").isJsonNull() ) {
                log.e("Youbora PluginConfig is missing the youboraConfig key in json object");
                return;
            }
            if (pluginConfig.getAsJsonObject("youboraConfig").has("adsAnalytics")  &&
                !pluginConfig.getAsJsonObject("youboraConfig").getAsJsonPrimitive("adsAnalytics").isJsonNull()) {
                adAnalytics = pluginConfig.getAsJsonObject("youboraConfig").getAsJsonPrimitive("adsAnalytics").getAsBoolean();
            }
            startMonitoring(this.player);
            log.d("onLoad");
        }

    }

    PKEvent.Listener eventListener = new PKEvent.Listener() {
        @Override
        public void onEvent(PKEvent event) {
            setPluginOptions();
        }
    };

    private void startMonitoring(Player player) {
        log.d("start monitoring");
        messageBus.listen(eventListener, PlayerEvent.Type.DURATION_CHANGE);
        setPluginOptions();
    }

    private void setPluginOptions(){
        //update the isLive value
        if (pluginConfig != null && pluginConfig.has("media")) {
            if (!((JsonObject) pluginConfig.get("media")).has("isLive")) {
                boolean isLiveFlag = pluginManager.getIsLive();
                log.d("isLiveFlag = " + pluginManager.getIsLive());
                ((JsonObject) pluginConfig.get("media")).addProperty("isLive", isLiveFlag);
            }
        }

        Map<String, Object> opt  = YouboraConfig.getYouboraConfig(pluginConfig, mediaConfig, player);
        // Set options
        pluginManager.setOptions(opt);

        if (!isMonitoring) {
            isMonitoring = true;
            pluginManager.startMonitoring(player);
        }
        if (adAnalytics && !isAdsMonitoring){
            isAdsMonitoring = true;
            adsManager = new YouboraAdManager(pluginManager, messageBus);
            adsManager.startMonitoring(this.player);
            pluginManager.setAdnalyzer(adsManager);
        }
    }

    private void stopMonitoring() {
        log.d("stop monitoring");
        isMonitoring = false;
        pluginManager.stopMonitoring();
        if (adsManager != null){
            adsManager.stopMonitoring();
            isAdsMonitoring = false;
        }
    }
}
