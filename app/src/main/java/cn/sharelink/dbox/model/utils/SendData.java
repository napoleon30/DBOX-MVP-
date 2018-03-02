package cn.sharelink.dbox.model.utils;

import android.util.Log;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACDeviceMsg;
import com.accloud.service.ACException;
import com.accloud.utils.PreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.sharelink.dbox.application.MainApplication;

/**
 * Created by WangLei on 2018/2/27.
 */

public class SendData {

    String subDomain, physicalDeviceId;

    public SendData(String subDomain, String physicalDeviceId) {
        super();
        this.subDomain = subDomain;
        this.physicalDeviceId = physicalDeviceId;
    }

    /**
     * 发送命令
     */
    public void sendData(String message) {
        Log.e("send_message", message);
        byte[] midbytes = message.getBytes();

//		Log.e("发送的数据byte[]（十进制）", "*********" + Arrays.toString(midbytes));
        byte[] b = new byte[midbytes.length / 2];
        for (int i = 0; i < midbytes.length / 2; i++) {
            b[i] = uniteBytes(midbytes[i * 2], midbytes[i * 2 + 1]);
        }

        AC.bindMgr().sendToDeviceWithOption(subDomain, physicalDeviceId,
                getDeviceMsg(b), AC.ONLY_CLOUD,// ///////////////////////////////////////////////////
                new PayloadCallback<ACDeviceMsg>() {
                    @Override
                    public void success(ACDeviceMsg msg) {
                        if (parseDeviceMsg(msg)) {
                            String returnedValue = ItonAdecimalConver
                                    .byte2hex(msg.getContent());
                            Log.e("callBack返回的消息十六进制转换", returnedValue);
                        }
                    }

                    @Override
                    public void error(ACException arg0) {
                        // TODO Auto-generated method stub
                    }

                });
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    private ACDeviceMsg getDeviceMsg(byte[] b) {
        // 注意：实际开发的时候请选择其中的一种消息格式即可
        switch (getFormatType()) {
            case 0://BINARY

                return new ACDeviceMsg(Config.LIGHT_MSGCODE, b);
            case 1://JSON
                JSONObject object = new JSONObject();
                try {
                    object.put("switch", b);
                } catch (JSONException e) {
                }
                return new ACDeviceMsg(70, object.toString().getBytes());
        }
        return null;
    }

    private int getFormatType() {
        return PreferencesUtils.getInt(MainApplication.getInstance(),
                "formatType", 0); //0表示BINARY
    }

    protected boolean parseDeviceMsg(ACDeviceMsg msg) {
        // 注意：实际开发的时候请选择其中的一种消息格式即可
        switch (getFormatType()) {
            case 0://BINARY
                byte[] bytes = msg.getContent();
                if (bytes != null)
                    // return bytes[0] == 0x69 ? true : false;//
                    return true;
            case 1://JSON
                try {
                    JSONObject object = new JSONObject(new String(msg.getContent()));
                    return object.optBoolean("result");
                } catch (Exception e) {
                }
        }
        return false;
    }

}
