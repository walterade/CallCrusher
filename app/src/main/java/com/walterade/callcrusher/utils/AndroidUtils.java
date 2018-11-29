package com.walterade.callcrusher.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by Walter on 11/26/16.
 */

public class AndroidUtils {

    static Handler handler = new Handler(Looper.getMainLooper());

    private static final int LOW_BATTERY_PCT = 15;


    public static final int ROTATION_PORTRAIT = 0;
    public static final int ROTATION_LANDSCAPE = 1;
    public static final int ROTATION_REVERSE_PORTRAIT = 2;
    public static final int ROTATION_REVERSE_LANDSCAPE = 3;

    public static final int POWERSAVE_MODE_NO = 0;
    public static final int POWERSAVE_MODE_YES = 1;
    public static final int POWERSAVE_MODE_COULD_BE = 2;

    public static final int LOCATION_MODE_OFF = 0;
    public static final int LOCATION_MODE_SENSORS_ONLY = 1;
    public static final int LOCATION_MODE_BATTERY_SAVING = 2;
    public static final int LOCATION_MODE_HIGH_ACCURACY = 3;
    public static final int LOCATION_MODE_UNKNOWN = 4;

    private static HashMap<String, Field> privateFields = new HashMap<>();

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic");
    }

    public static boolean runOnUIThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
            return true;
        } else handler.post(r);
        return false;
    }

    public static int inPowerSaveMode(Context context) {

        if (isBatteryCharging(context)) return POWERSAVE_MODE_NO;

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (powerManager != null) {
                return powerManager.isPowerSaveMode() ? POWERSAVE_MODE_YES : POWERSAVE_MODE_NO;
            }
        }

        if (getLocationMode(context) != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
            return getBatteryPercentage(context) <= LOW_BATTERY_PCT ? POWERSAVE_MODE_COULD_BE : POWERSAVE_MODE_NO;
        }
        return getBatteryPercentage(context) <= LOW_BATTERY_PCT ? POWERSAVE_MODE_COULD_BE : POWERSAVE_MODE_NO;
    }

    public static int getLocationMode(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return LOCATION_MODE_UNKNOWN;
    }

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public static boolean isBatteryCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        // Are we charging / charged?
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
        }

        return false;
    }

    public static boolean isBatteryLow(Context context) {
        return getBatteryPercentage(context) <= LOW_BATTERY_PCT;
    }

    public static Object getFieldValue(Object obj, Class cls, String field) {
        String clsName = cls.getName();
        String key = clsName + ":" + field;
        Field f = privateFields.get(key);
        Object ret = null;

        try {
            if (f == null) {
                f = cls.getDeclaredField(field);
                if (f != null) {
                    f.setAccessible(true);
                    privateFields.put(key, f);
                }
            }
            if (f != null) ret = f.get(obj);
        } catch(NoSuchFieldException ex){
            Timber.e("No Such Field.");
        } catch(IllegalAccessException ex){
            Timber.e("Illegal Access.");
        }

        return ret;
    }

    public static int getRotation(Context context){
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return ROTATION_PORTRAIT;
            case Surface.ROTATION_90:
                return ROTATION_LANDSCAPE;
            case Surface.ROTATION_180:
                return ROTATION_REVERSE_PORTRAIT;
            default:
                return ROTATION_REVERSE_LANDSCAPE;
        }
    }

    public static boolean isInLandscapeMode(FragmentActivity activity) {
        int orientation = activity.getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int setPortraitMode(FragmentActivity activity) {
        int o;

        switch (getRotation(activity)) {
            case ROTATION_REVERSE_PORTRAIT:
                o = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                break;
            default:
                o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
        }

        return setOrientation(activity, o);
    }

    public static int setOrientation(FragmentActivity activity, int orientation) {
        int ret = activity.getRequestedOrientation();
        if (ret != orientation)
            activity.setRequestedOrientation(orientation);
        return ret;
    }

    public static void setVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public static void preventScreenshots(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }


    public interface GenerateRandomUUIDDupeCheck {
        boolean isDupe(String uuid);
    }

    public static String generateRandomUUID(GenerateRandomUUIDDupeCheck dupeCheck) {
        String id = UUID.randomUUID().toString();
        if (dupeCheck != null) while (dupeCheck.isDupe(id)) id = UUID.randomUUID().toString();
        return id;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return HelperUtils.capitalize(model);
        } else {
            return HelperUtils.capitalize(manufacturer) + " " + model;
        }
    }

    @ColorInt
    public static int getThemeColor (@NonNull final Context context, @AttrRes final int attributeColor) {
        final TypedValue value = new TypedValue();
        context.getTheme ().resolveAttribute (attributeColor, value, true);
        return value.data;
    }

    public static boolean isServiceRunningInForeground(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return service.foreground;
            }
        }
        return false;
    }
}