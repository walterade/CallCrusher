package com.walterade.callcrusher.mvp.data.api;

import com.walterade.callcrusher.mvp.data.model.CNAMQueryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenCNAMService {
    @GET("/v3/phone/{phoneNumber}?casing=title&mobile=location&no_value=unknown&geo=wire")
    @Headers({"Accept: application/json"})
    Call<CNAMQueryResponse> query(@Path("phoneNumber") String phoneNumber, @Query("account_sid") String accountSId, @Query("auth_token") String authToken);
}
