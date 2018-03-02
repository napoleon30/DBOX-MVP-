package cn.sharelink.dbox.model.utils;

/**
 * Created by WangLei on 2018/2/27.
 */

public class OnReceive {
    String deviceId;
    String[] payload;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String[] getPayload() {
        return payload;
    }

    public void setPayload(String[] payload) {
        this.payload = payload;
    }
}
