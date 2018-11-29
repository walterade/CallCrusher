package com.walterade.callcrusher.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;
import com.walterade.callcrusher.R;
import com.walterade.callcrusher.mvp.data.api.OpenCNAMService;
import com.walterade.callcrusher.mvp.data.model.CNAMQueryResponse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PhoneUtils {

    public static boolean endCall(Context c) {

        ITelephony telephonyService;
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            Method m = tm.getClass().getDeclaredMethod("getITelephony");

            m.setAccessible(true);
            telephonyService = (ITelephony) m.invoke(tm);
            telephonyService.endCall();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static CNAMQueryResponse getOpenCNAMCallerId(Context c, String accountSid, String authToken, String phoneNumber) {

        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(5, TimeUnit.SECONDS);
        client.readTimeout(5, TimeUnit.SECONDS);
        client.writeTimeout(5, TimeUnit.SECONDS);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(c.getString(R.string.opencnam_api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();

        OpenCNAMService service = retrofit.create(OpenCNAMService.class);

        try {
            return service.query(phoneNumber, accountSid, authToken).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ContactUtils.ContactInfo getCallerId(Context c, String accountSid, String authToken, String phoneNumber) {

        ContactUtils.ContactInfo info = ContactUtils.getContactInfo(c, phoneNumber);

        if (info == null) {
            CNAMQueryResponse cnam = getOpenCNAMCallerId(c, accountSid, authToken, phoneNumber);
            if (cnam != null && TextUtils.isEmpty(cnam.err)) {
                info = new ContactUtils.ContactInfo();
                info.name = cnam.name;
            }
        }

        return info;
    }

}
