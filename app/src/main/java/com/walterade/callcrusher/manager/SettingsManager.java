package com.walterade.callcrusher.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsManager {

    public static final int CRUSH_STATE_ALLOW = 0;
    public static final int CRUSH_STATE_BLOCK = 1;
    public static final int CRUSH_STATE_CRUSH = 2;

    private final SharedPreferences pref;

    public SettingsManager(Context c) {
        pref = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public long getIncomingCallsLastViewTime() {
        return pref.getLong("incoming calls last view time", 0);
    }
    public void setIncomingCallsLastViewTime(long time) {
        pref.edit().putLong("incoming calls last view time", time).apply();
    }

    public long getBlockedCallsLastViewTime() {
        return pref.getLong("blocked calls last view time", 0);
    }
    public void setBlockedCallsLastViewTime(long time) {
        pref.edit().putLong("blocked calls last view time", time).apply();
    }

    public int getCallsCrushState() {
        return pref.getInt("calls crush state", CRUSH_STATE_BLOCK);
    }

    public void setCallsCrushState(int state) {
        pref.edit().putInt("calls crush state", state).apply();
    }

    public void setCurrentTab(int tab) {
        pref.edit().putInt("landing tab", tab).apply();
    }

    public int getCurrentTab() {
        return pref.getInt("landing tab", 0);
    }

}
