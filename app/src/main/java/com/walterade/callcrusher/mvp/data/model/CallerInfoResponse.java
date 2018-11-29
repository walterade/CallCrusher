package com.walterade.callcrusher.mvp.data.model;

import com.google.gson.annotations.SerializedName;

public class CallerInfoResponse {

    public final int TYPE_NORMAL = 0;
    public final int TYPE_SUSPICIOUS = 1;
    public final int TYPE_SCAM = 2;

    public int type;

    public String name;

    @SerializedName("phone_number")
    public String phoneNumber;

}
