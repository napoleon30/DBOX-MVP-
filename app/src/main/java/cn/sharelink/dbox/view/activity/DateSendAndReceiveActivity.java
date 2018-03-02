package cn.sharelink.dbox.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.accloud.service.ACUserDevice;
import com.accloud.service.Receiver;
import com.accloud.service.TopicData;
import com.accloud.utils.PreferencesUtils;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.utils.Config;
import cn.sharelink.dbox.presenter.IPresenter.IDatePresenter;
import cn.sharelink.dbox.presenter.presenterCompl.DatePresenter;
import cn.sharelink.dbox.view.interfaceview.IDateView;

public class DateSendAndReceiveActivity extends AppCompatActivity implements IDateView, View.OnClickListener {
    private EditText etSend;
    private TextView tvName, tvRece;
    private Button send, clear,back;
    IDatePresenter presenter;
    ACUserDevice device;
    private String subDomain;
    Receiver<TopicData> receiver;// 订阅

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_send_and_receive);
        device = (ACUserDevice) getIntent().getSerializableExtra("device");
        subDomain = PreferencesUtils.getString(this, "subDomain",
                Config.SUBDOMAIN);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.subscribe(receiver, "xinlian01", "topic_type", device.getDeviceId(), tvRece);// 订阅，可获取到返回值
    }

    @Override
    public void initView() {
        presenter = new DatePresenter(DateSendAndReceiveActivity.this);
        etSend = findViewById(R.id.et);
        tvName = findViewById(R.id.tv_name);
        tvName.setText(device.getName());
        back = findViewById(R.id.btn_back);
        tvRece = findViewById(R.id.tv);
        send = findViewById(R.id.btn_send);
        clear = findViewById(R.id.btn_clear);
        back.setOnClickListener(this);
        send.setOnClickListener(this);
        clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_clear:
                tvRece.setText("");
                break;
            case R.id.btn_send:
                presenter.sendDate(subDomain, device.getPhysicalDeviceId(), etSend.getText().toString().trim());
                break;
            case R.id.btn_back:
                DateSendAndReceiveActivity.this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.unRegister(receiver);
    }
}
