package com.walterade.callcrusher.mvp.data.api;

import com.walterade.callcrusher.mvp.data.model.CallerInfoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CallerInfoService {

    @GET("/rest/caller/{phoneNumber}")
    @Headers({"Accept: application/json"})
    Call<CallerInfoResponse> getCallerInfo(@Path("phoneNumber") String phoneNumber, @Query("api_key") String apiKey);
}
