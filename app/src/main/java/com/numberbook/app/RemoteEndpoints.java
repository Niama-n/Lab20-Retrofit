package com.numberbook.app;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RemoteEndpoints {

    @POST("insertContact.php")
    Call<ServerResult> pushEntry(@Body PhoneEntry entry);

    @GET("getAllContacts.php")
    Call<List<PhoneEntry>> fetchAllEntries();

    @GET("searchContact.php")
    Call<List<PhoneEntry>> lookupEntries(@Query("keyword") String searchTerm);
}
