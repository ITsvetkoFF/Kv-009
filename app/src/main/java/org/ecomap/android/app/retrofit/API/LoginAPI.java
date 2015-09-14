package org.ecomap.android.app.retrofit.API;

import org.ecomap.android.app.User;
import org.ecomap.android.app.retrofit.model.LoginModel;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by Tatiana on 14.09.15.
 */
public interface LoginAPI {

    @FormUrlEncoded
    @POST("/login")

    void getUser(@Field("email") String email, @Field("password") String password, Callback<LoginModel> response);
}
