package io.agora.meeting.http.been;

import java.util.Map;

/**
 * Description:
 *
 * @since 3/3/21
 */
public final class AppVersionResp {
    public int code;
    public String msg;
    public long ts;
    public Data data;

    public static class Data {
        public String appPackage;
        public String appVersion;
        public int forcedUpgrade;
        public String id;
        public String latestVersion;
        public int osType;
        public int terminalType;
        public int remindTimes;
        public int reviewing;
        public String upgradeDescription;
        public String upgradeUrl;

        public Config config;
    }

    public static class Config {
        public Map<String, Map<String, String>> multiLanguage;
        public int whiteboardOperatorCount;
    }

}
