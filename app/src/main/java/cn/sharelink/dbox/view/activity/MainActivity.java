package cn.sharelink.dbox.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.accloud.cloudservice.AC;
import com.accloud.cloudservice.PayloadCallback;
import com.accloud.service.ACBindMgr;
import com.accloud.service.ACException;
import com.accloud.service.ACUserDevice;
import com.accloud.utils.PreferencesUtils;
import com.zbar.lib.CaptureActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.customView.xlistView.XListView;
import cn.sharelink.dbox.model.utils.Config;
import cn.sharelink.dbox.model.utils.DensityUtils;
import cn.sharelink.dbox.model.utils.MainAdapter;
import cn.sharelink.dbox.presenter.IPresenter.IMainPresenter;
import cn.sharelink.dbox.presenter.presenterCompl.MainPresenter;
import cn.sharelink.dbox.view.interfaceview.IMainView;

public class MainActivity extends AppCompatActivity implements IMainView,View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView logOut;
    private TextView noDevice;
    private TextView addDevice;
    private XListView listView;
    private MainAdapter adapter;
    // 设备管理器
    ACBindMgr bindMgr;
    List<ACUserDevice> userDevices;

    private String subDomain;

    Timer timer;
    boolean isRunning = false;

    IMainPresenter presenter;
    private PopupWindow mPopupWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        subDomain = PreferencesUtils.getString(this, "subDomain",
                Config.SUBDOMAIN);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");

        if (!AC.accountMgr().isLogin()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            getDeviceList();
        }
    }

    @Override
    public void initView() {
        // 获取设备管理器
        bindMgr = AC.bindMgr();
        userDevices = new ArrayList<ACUserDevice>();
        adapter = new MainAdapter(MainActivity.this,userDevices);

        logOut = findViewById(R.id.log_out);
        noDevice = findViewById(R.id.no_device);
        addDevice = findViewById(R.id.right_add_device);
        listView = findViewById(R.id.device_list);
        logOut.setOnClickListener(this);
        addDevice.setOnClickListener(this);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClick);
        listView.setOnItemLongClickListener(onItemLongClick);
        listView.setXListViewListener(new XListView.IXListViewListener() {
            @Override
            public void onRefresh() {
                getDeviceList();
            }
        });
        presenter = new MainPresenter(MainActivity.this,bindMgr,adapter,subDomain,noDevice,listView,userDevices);
    }

    /**
     * 获取设备列表
     */
    @Override
    public void getDeviceList() {
        presenter.getDeviceList();
    }

    @Override
    public void showConfigurationDialog(ACUserDevice device) {
        presenter.showConfigurationDialog(device);
    }

    /**
     * 短按跳转
     */
    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //点击的位置为i-1
            Intent intent = new Intent(MainActivity.this,
                    DateSendAndReceiveActivity.class);
            intent.putExtra("device",userDevices.get(i-1));
            startActivity(intent);
        }
    };
    /**
     * 长按弹框
     */
    private AdapterView.OnItemLongClickListener onItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            showConfigurationDialog(userDevices.get(i-1));
            return true;
        }
    };


    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     *            屏幕透明度0.0-1.0 1表示完全不透明
     */
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        this.getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.log_out:
                presenter.logout();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                MainActivity.this.finish();
                break;
            case R.id.right_add_device:
                showPopupWindow(addDevice);
                break;
            case R.id.add_for_wifi:
                intent = new Intent(MainActivity.this, AddDeviceActivity.class);
                startActivity(intent);
                mPopupWindow.dismiss();
                break;
            case R.id.add_for_scan:
                intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 1234);
                mPopupWindow.dismiss();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234) {
            switch (resultCode) {
                case 555:
                    String shareCode = data.getStringExtra("scan_result");
                    Log.e(TAG, "result :" + shareCode);
                    //普通用户通过扫分享码绑定设备
                    bindMgr.bindDeviceWithShareCode(shareCode, new PayloadCallback<ACUserDevice>() {
                        @Override
                        public void success(ACUserDevice userDevice) {
                            //成功绑定管理员分享的设备
                            Log.e(TAG, "SUCCESS ");
//			    	Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
                            adapter.deviceList.add(userDevice);
                            adapter.notifyDataSetChanged();
                            getDeviceList();
                        }
                        @Override
                        public void error(ACException e) {
                            //网络错误或其他，根据e.getErrorCode()做不同的提示或处理
                        }
                    });
                    break;

                case 554:
                    break;
            }
        }
    }

    private void showPopupWindow(TextView addDevice) {
        View popupView = getLayoutInflater().inflate(
                R.layout.mian_popupwindow, null);
        mPopupWindow = new PopupWindow(popupView, DensityUtils.dipTopx(MainActivity.this,
                113), DensityUtils.dipTopx(MainActivity.this, 100), true);
        TextView wifi_add = (TextView) popupView
                .findViewById(R.id.add_for_wifi);
        TextView scan_add = (TextView) popupView
                .findViewById(R.id.add_for_scan);
        wifi_add.setOnClickListener(this);
        scan_add.setOnClickListener(this);
        setBackgroundAlpha(0.5f);// 设置屏幕透明度
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(
                getResources(), (Bitmap) null));

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // popupWindow隐藏时恢复屏幕正常透明度
                setBackgroundAlpha(1.0f);
            }
        });
        mPopupWindow.showAsDropDown(addDevice, DensityUtils.dipTopx(MainActivity.this, -80),
                DensityUtils.dipTopx(MainActivity.this, 5));
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.stopTimer();
    }
}
