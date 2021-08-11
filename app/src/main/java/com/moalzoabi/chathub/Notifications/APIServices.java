package com.moalzoabi.chathub.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIServices {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAf2C9f_k:APA91bGu8fBF0jhTqjucnBRCvdSBOOhwNOPfa06HQTMb69dIF1U7cIXCKnayOyz2d13YEb_jatRyBTm-Dcz02heFlYFk-zZ8gV-jNdQuOQOf1707mq9GHNut_6oMYJgbEzV3scGK8ojX"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
