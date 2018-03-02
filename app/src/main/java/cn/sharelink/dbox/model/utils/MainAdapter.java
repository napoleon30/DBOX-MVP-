package cn.sharelink.dbox.model.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accloud.service.ACUserDevice;
import com.accloud.utils.PreferencesUtils;

import java.util.List;

import cn.sharelink.dbox.R;

/**
 * Created by WangLei on 2018/2/2.
 */

public class MainAdapter extends BaseAdapter{
    public List<ACUserDevice> deviceList;
    private Context context;

    public MainAdapter(Context context,List<ACUserDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return deviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(
                R.layout.adapter_list_device, null);
        LinearLayout rl = ViewHolder.get(view, R.id.list_item);
        ImageView pic = ViewHolder.get(view, R.id.iv_pic);
        final TextView deviceName = ViewHolder
                .get(view, R.id.tv_deviceName);
        TextView physical = ViewHolder.get(view, R.id.tv_physicalDeviceId);

        final ACUserDevice device = deviceList.get(i);

      String physicalDeviceId = device.getPhysicalDeviceId();

       String deviceId = device.getDeviceId() + "";

        physical.setText(physicalDeviceId);
        deviceName.setText(PreferencesUtils.getString(context,
                physicalDeviceId + "deviceName", device.getName()));
        switch (device.getStatus()) {

            case ACUserDevice.OFFLINE:
                deviceName.setTextColor(Color.GRAY);
                physical.setTextColor(Color.GRAY);

                pic.setBackgroundResource(R.mipmap.cloud_off);
                Log.e("LINE", "OFFLINE");

                break;
            case ACUserDevice.NETWORK_ONLINE:
                deviceName.setTextColor(Color.GREEN);
                physical.setTextColor(Color.GREEN);
                pic.setBackgroundResource(R.mipmap.cloud_on);
                Log.e("LINE", "NETWORK_ONLINE");
                break;
            case ACUserDevice.LOCAL_ONLINE:
                deviceName.setTextColor(Color.GREEN);
                physical.setTextColor(Color.GREEN);
                pic.setBackgroundResource(R.mipmap.cloud_on);
                Log.e("LINE", "LOCAL_ONLINE");
                break;
            case ACUserDevice.BOTH_ONLINE:
                deviceName.setTextColor(Color.GREEN);
                physical.setTextColor(Color.GREEN);
                pic.setBackgroundResource(R.mipmap.cloud_on);
                Log.e("LINE", "BOTH_ONLINE");
                break;
        }

        return view;
    }
}
