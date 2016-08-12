package com.dealfaro.luca.HW3_9362;

/**
 * Created by craighu on 4/26/2015.
 */

import com.google.gson.annotations.SerializedName;

public class MsgInfo {
    @SerializedName("msg")
    public String msg;
    @SerializedName("userid")
    public String userid;
    @SerializedName("dest")
    public String dest;
    @SerializedName("ts")
    public String ts;
    @SerializedName("msgid")
    public String msgid;
    @SerializedName("conversation")
    public Boolean conversation;
}
