package cn.sharelink.dbox.view.interfaceview;


import com.accloud.service.ACUserDevice;

/**
 * Created by WangLei on 2018/2/2.
 */

public interface IMainView {
    void initView();

    void getDeviceList();

    void showConfigurationDialog(ACUserDevice device);


}
