package cn.sharelink.dbox.presenter.IPresenter;

import com.accloud.service.ACUserDevice;

/**
 * Created by WangLei on 2018/2/2.
 */

public interface IMainPresenter {
    void getDeviceList();
    void startTimer();
    void stopTimer();
    void refreshDeviceStatus();
    void showConfigurationDialog(ACUserDevice device);
    void unbindDevice(ACUserDevice device);
    void showSetDialog(ACUserDevice device);
    void createQRImage(String shareCode);

    void logout();
}
