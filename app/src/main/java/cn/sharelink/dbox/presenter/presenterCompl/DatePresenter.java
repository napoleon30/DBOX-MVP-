package cn.sharelink.dbox.presenter.presenterCompl;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACException;
import com.accloud.service.Receiver;
import com.accloud.service.Topic;
import com.accloud.service.TopicData;

import cn.sharelink.dbox.model.utils.GsonUtil;
import cn.sharelink.dbox.model.utils.ItonAdecimalConver;
import cn.sharelink.dbox.model.utils.OnReceive;
import cn.sharelink.dbox.model.utils.SendData;
import cn.sharelink.dbox.presenter.IPresenter.IDatePresenter;

/**
 * Created by WangLei on 2018/2/26.
 */

public class DatePresenter implements IDatePresenter {
    Context context;

    public DatePresenter(Context context) {
        this.context = context;
    }

    /**
     * 订阅方法和获取返回值
     * @param subDomain
     * @param topic_type
     * @param deviceId
     */
    @Override
    public void subscribe(Receiver<TopicData> receiver, String subDomain, String topic_type, long deviceId, final TextView textView) {
        AC.customDataMgr().subscribe(Topic.customTopic(subDomain, topic_type, deviceId + ""), new VoidCallback() {
            @Override
            public void success() {
                Log.e("/*/*/*", "订阅成功");
            }

            @Override
            public void error(ACException e) {
                Log.e("/*/*/*", "订阅失败");
            }
        });
        receiver = new Receiver<TopicData>() {
            @Override
            public void onReceive(TopicData topicData) {
                Log.e("/*/*/*", "订阅返回值：" + topicData.getValue());
                String jsonData = topicData.getValue();
                OnReceive onRece = GsonUtil.parseJsonWithGson(jsonData,
                        OnReceive.class);
                String[] pay = onRece.getPayload();
                byte[] arraysPay = Base64.decode(pay[0], 0);
                String payload = ItonAdecimalConver.byte2hex(arraysPay)
                        .replace(" ", "");
                Log.e("/*/*/*", "接收到的值:" + payload);
                textView.append(payload + "\n");

            }
        };
        AC.customDataMgr().registerDataReceiver(receiver);
    }

    @Override
    public void unRegister(Receiver<TopicData> receiver) {
        AC.customDataMgr().unregisterDataReceiver(receiver);
    }

    @Override
    public void sendDate(String subDomain, String physicalDeviceId, String message) {
        message = message.replace(" ", "");
        if (message.length() % 2 == 0) {
            new SendData(subDomain, physicalDeviceId).sendData(message);
        } else {
            Toast.makeText(context, "请输入偶数长度的字符串", Toast.LENGTH_LONG).show();
        }
    }


}
