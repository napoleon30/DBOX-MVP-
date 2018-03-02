package cn.sharelink.dbox.presenter.presenterCompl;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.cloudservice.VoidCallback;
import com.accloud.service.ACBindMgr;
import com.accloud.service.ACDeviceFind;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.accloud.utils.PreferencesUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.customView.dialog.ActionSheetDialog;
import cn.sharelink.dbox.model.customView.xlistView.XListView;
import cn.sharelink.dbox.model.utils.DBOXException;
import cn.sharelink.dbox.model.utils.MainAdapter;
import cn.sharelink.dbox.model.utils.Pop;
import cn.sharelink.dbox.presenter.IPresenter.IMainPresenter;

/**
 * Created by WangLei on 2018/2/2.
 */

public class MainPresenter implements IMainPresenter {
    Context context;
    MainAdapter adapter;
    String subDomain;
    ACBindMgr bindMgr;
    TextView noDevice;
    XListView listView;
    List<ACUserDevice> userDevices;

    Timer timer;
    boolean isRunning = false;

    public MainPresenter(Context context, ACBindMgr bindMgr,MainAdapter adapter,String subDomain,TextView noDevice,XListView listView,List<ACUserDevice> userDevices) {
        this.context = context;
        this.bindMgr = bindMgr;
        this.adapter = adapter;
        this.subDomain = subDomain;
        this.noDevice = noDevice;
        this.listView = listView;
        this.userDevices = userDevices;
    }

    /**
     * 获取设备列表
     */
    @Override
    public void getDeviceList() {
        userDevices.clear();
        bindMgr.listDevicesWithStatus(new PayloadCallback<List<ACUserDevice>>() {
            @Override
            public void success(List<ACUserDevice> deviceList) {
                for (ACUserDevice device : deviceList) {
                    device.getStatus();
                    userDevices.add(device);
                }
                if (deviceList.size() == 0) {
                    noDevice.setVisibility(View.VISIBLE);
                    listView.setPullRefreshEnable(false);
                } else {
                    listView.setPullRefreshEnable(true);
                }
                adapter.deviceList = deviceList;
                adapter.notifyDataSetChanged();
                listView.stopRefresh();
                // 启动定时器,定时更新局域网状态
                startTimer();
            }

            @Override
            public void error(ACException e) {
                DBOXException.errorCode(context, e.getErrorCode());
            }
        });
    }

    // 启动定时器,定时更新局域网状态
    public void startTimer() {
        if (!isRunning) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refreshDeviceStatus();
                }
            }, 0, 3000);
            isRunning = true;
        }
    }

    public void stopTimer(){
        if (timer != null) {
            timer.cancel();
            isRunning = false;
        }
    }

    // 定时更新设备当前的局域网状态
    public void refreshDeviceStatus() {
        // 当设备掉线或网络环境不稳定导致获取局域网显示状态不准确时，需要手动刷新设备列表与局域网状态
        AC.findLocalDevice(AC.FIND_DEVICE_DEFAULT_TIMEOUT,
                new PayloadCallback<List<ACDeviceFind>>() {
                    @Override
                    public void success(List<ACDeviceFind> deviceFinds) {
                        // 局域网状态是否发生改变,是否需要更新界面
                        boolean isRefresh = false;
                        // 遍历当前用户绑定的所有设备列表
                        for (ACUserDevice device : adapter.deviceList) {
                            // 判断当前设备是否局域网本地在线
                            boolean isLocalOnline = false;
                            // 遍历当前发现的局域网在线列表
                            for (ACDeviceFind deviceFind : deviceFinds) {
                                // 通过设备的物理Id进行匹配,若当前设备在发现的局域网列表中,则置为局域网在线
                                if (device.getPhysicalDeviceId().equals(
                                        deviceFind.getPhysicalDeviceId())) {
                                    isLocalOnline = true;
                                }
                            }
                            if (isLocalOnline) {
                                // 当前设备由不在线更新为局域网在线
                                if (device.getStatus() == ACUserDevice.OFFLINE) {
                                    device.setStatus(ACUserDevice.LOCAL_ONLINE);
                                    isRefresh = true;
                                    // 当前设备由云端在线更新为云端局域网同时在线
                                } else if (device.getStatus() == ACUserDevice.NETWORK_ONLINE) {
                                    device.setStatus(ACUserDevice.BOTH_ONLINE);
                                    isRefresh = true;
                                }
                            } else {
                                // 当前设备由局域网在线更新为不在线
                                if (device.getStatus() == ACUserDevice.LOCAL_ONLINE) {
                                    device.setStatus(ACUserDevice.OFFLINE);
                                    AC.SEND_TO_LOCAL_DEVICE_DEFAULT_TIMEOUT = 6000;
                                    isRefresh = true;
                                    // 当前设备由云端局域网同时在线更新为云端在线
                                } else if (device.getStatus() == ACUserDevice.BOTH_ONLINE) {
                                    device.setStatus(ACUserDevice.NETWORK_ONLINE);
                                    isRefresh = true;
                                }
                            }
                        }
                        // 局域网状态需要发生改变,更新列表界面
                        if (isRefresh)
                            adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void error(ACException e) {
                        // 局域网状态是否发生改变,是否需要更新列表界面
                        boolean isRefresh = false;
                        for (ACUserDevice device : adapter.deviceList) {
                            // 没有设备当前局域网在线,所以把所有当前显示局域网在线的设备状态重置
                            if (device.getStatus() == ACUserDevice.LOCAL_ONLINE) {
                                device.setStatus(ACUserDevice.OFFLINE);
                                isRefresh = true;
                            } else if (device.getStatus() == ACUserDevice.BOTH_ONLINE) {
                                device.setStatus(ACUserDevice.NETWORK_ONLINE);
                                isRefresh = true;
                            }
                        }
                        // 局域网状态需要发生改变,更新列表界面
                        if (isRefresh)
                            adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void showConfigurationDialog(final ACUserDevice device) {
        new ActionSheetDialog(context)
                .builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheetItem(context.getResources().getString(R.string.delete),
                        ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                unbindDevice(device);
                            }
                        })
                .addSheetItem(context.getResources().getString(R.string.set),
                        ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                showSetDialog(device);
                            }
                        })
                .addSheetItem(context.getResources().getString(R.string.share),
                        ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {

                            @Override
                            public void onClick(int which) {

                                // 管理员获取分享码
                                AC.bindMgr().fetchShareCode(
                                        device.getDeviceId(), 3600,
                                        new PayloadCallback<String>() {

                                            @Override
                                            public void success(String shareCode) {
                                                createQRImage(shareCode);

                                            }

                                            @Override
                                            public void error(ACException e) {
                                                DBOXException.errorCode(
                                                        context,
                                                        e.getErrorCode());

                                            }
                                        });
                            }
                        }).show();
    }

    /**
     * 设备解绑
     * @param device
     */
    @Override
    public void unbindDevice(ACUserDevice device) {
        bindMgr.unbindDevice(subDomain, device.getDeviceId(),
                new VoidCallback() {
                    @Override
                    public void success() {
                        Pop.popToast(
                                context,
                                context.getResources().getString(R.string.delete_device_success));
                        getDeviceList();
                    }

                    @Override
                    public void error(ACException e) {
                        // Pop.popToast(MainActivity.this, e.getErrorCode()
                        // + "-->" + e.getMessage());
                        DBOXException.errorCode(context,
                                e.getErrorCode());
                    }
                });
    }

    /**
     * 设备设置（重命名）
     * @param device
     */
    @Override
    public void showSetDialog(final ACUserDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view; // 使用view来接入方法写出的dialog，方便相关初始化
        LayoutInflater inflater; // 引用自定义dialog布局
        inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.dialog_set, null);
        builder.setView(view);

        final AlertDialog alert = builder.create();

        final Button cancel = (Button) view.findViewById(R.id.cancel_btn);
        final Button confirm = (Button) view.findViewById(R.id.confirm_btn);
        final EditText edit_name = (EditText) view.findViewById(R.id.edit_name);

        confirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!edit_name.getText().toString().trim().equals("")) {
                    bindMgr.changeName(subDomain, device.getDeviceId(), edit_name.getText()
                            .toString().trim(), new VoidCallback() {

                        @Override
                        public void error(ACException arg0) {
                            Toast.makeText(
                                    context,
                                    context.getResources()
                                            .getString(
                                                    R.string.the_device_name_change_fails_please_try_again),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void success() {
                            PreferencesUtils.putString(context,
                                    device.getPhysicalDeviceId() + "deviceName", edit_name
                                            .getText().toString().trim());

                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                adapter.notifyDataSetChanged();

                alert.dismiss();

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();
    }

    /**
     * 生成二维码
     * @param url
     */
    @Override
    public void createQRImage(String url) {
        int QR_WIDTH = 300;
        int QR_HEIGHT = 300;
        // 判断URL合法性
        if (url == null || "".equals(url) || url.length() < 1) {
            return;
        }
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 图像数据转换，使用了矩阵转换
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT, hints);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
        // 下面这里按照二维码的算法，逐个生成二维码的图片，
        // 两个for循环是图片横列扫描的结果
        for (int y = 0; y < QR_HEIGHT; y++) {
            for (int x = 0; x < QR_WIDTH; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * QR_WIDTH + x] = 0xff000000;
                } else {
                    pixels[y * QR_WIDTH + x] = 0xffffffff;
                }
            }
        }
        // 生成二维码图片的格式，使用ARGB_8888
        Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
        showShareCodeDialog(bitmap);
    }

    public void showShareCodeDialog(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.share_code_dialog, null);
        builder.setView(view);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_share_code);
        iv.setImageBitmap(bitmap);
        builder.create().show();
    }

    /**
     * 退出登录
     */
    @Override
    public void logout() {
        AC.accountMgr().logout();
    
    }


}
