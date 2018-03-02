package cn.sharelink.dbox.presenter.IPresenter;

import android.widget.TextView;

import com.accloud.service.Receiver;
import com.accloud.service.TopicData;

/**
 * Created by WangLei on 2018/2/26.
 */

public interface IDatePresenter {

    void subscribe(Receiver<TopicData> receiver,String subDomain, String topic_type, long deviceId,TextView textView);
    void unRegister(Receiver<TopicData> receiver);
    void sendDate(String subDomain,String physicalDeviceId,String message);
}
