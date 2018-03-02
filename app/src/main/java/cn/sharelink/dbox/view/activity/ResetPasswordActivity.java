package cn.sharelink.dbox.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accloud.cloudservice.AC;
import com.accloud.service.ACAccountMgr;
import com.accloud.utils.PreferencesUtils;

import cn.sharelink.dbox.R;
import cn.sharelink.dbox.model.customView.country.CountryActivity;
import cn.sharelink.dbox.model.utils.BroadcastAction;
import cn.sharelink.dbox.presenter.IPresenter.IResetPasswordPresenter;
import cn.sharelink.dbox.presenter.presenterCompl.ResetPasswordPresenter;
import cn.sharelink.dbox.view.interfaceview.IResetPasswordView;

public class ResetPasswordActivity extends AppCompatActivity implements IResetPasswordView, View.OnClickListener {
    private static final String TAG = ResetPasswordActivity.class.getSimpleName();
    private TextView back;
    private TextView areaCode;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etPwd;
    private EditText etVCode;
    private Button vcodeBtn;
    private Button reset;

    private Button resetPhone;
    private Button resetEmail;
    private LinearLayout layout_tab;
    private RelativeLayout rl_phone;
    private RelativeLayout rl_email;

    String phone, pwd, vcode, email;
    String r_phone, r_email;
    String account = null;

    String countryNumber = "+86";

    // 账号管理�?
    ACAccountMgr accountMgr;
    int num = 0;
    IResetPasswordPresenter presenter;
    MyBroadCastReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        myReceiver = new MyBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.resetPasswordSuccess);
        registerReceiver(myReceiver, filter);

        initData();
        initView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reset_back:
                ResetPasswordActivity.this.finish();
                break;
            case R.id.btnTab001:
                num = 0;
                PreferencesUtils.putInt(ResetPasswordActivity.this, "num", num);
                setTabSelected(layout_tab, resetPhone);
                layout_tab.setBackgroundResource(R.mipmap.choice_left);
                resetPhone.setTextColor(Color.WHITE);
                resetEmail.setTextColor(Color.BLUE);
                rl_email.setVisibility(View.GONE);
                rl_phone.setVisibility(View.VISIBLE);
                break;
            case R.id.btnTab002:
                num = 1;
                PreferencesUtils.putInt(ResetPasswordActivity.this, "num", num);
                setTabSelected(layout_tab, resetEmail);
                layout_tab.setBackgroundResource(R.mipmap.choice_right);
                resetPhone.setTextColor(Color.BLUE);
                resetEmail.setTextColor(Color.WHITE);
                rl_email.setVisibility(View.VISIBLE);
                rl_phone.setVisibility(View.GONE);
                break;
            case R.id.reset_vcode:
                getData();
                getVcode(accountMgr,vcodeBtn,account,num);

                break;
            case R.id.reset:
                getData();
                vcode = etVCode.getText().toString();
                resetPassword(accountMgr, account, pwd, vcode);
                break;
            case R.id.area_code:
                Intent intent = new Intent(ResetPasswordActivity.this,
                        CountryActivity.class);
                startActivityForResult(intent, 13);
                break;
        }
    }

    @Override
    public void initData() {
        num = PreferencesUtils.getInt(ResetPasswordActivity.this, "num", 0);
        countryNumber = PreferencesUtils.getString(ResetPasswordActivity.this,
                "areacode", "+86");
        r_phone = PreferencesUtils.getString(ResetPasswordActivity.this,
                "phone", "");
        r_email = PreferencesUtils.getString(ResetPasswordActivity.this,
                "email", "");
    }

    @Override
    public void initView() {
        accountMgr = AC.accountMgr();
        presenter = new ResetPasswordPresenter(ResetPasswordActivity.this);
        back = (TextView) findViewById(R.id.reset_back);
        etPhone = (EditText) findViewById(R.id.reset_edit_tel);
        etEmail = (EditText) findViewById(R.id.reset_edit_email);
        etPwd = (EditText) findViewById(R.id.reset_edit_pwd);
        vcodeBtn = (Button) findViewById(R.id.reset_vcode);
        etVCode = (EditText) findViewById(R.id.reset_edit_vcode);
        reset = (Button) findViewById(R.id.reset);
        areaCode = (TextView) findViewById(R.id.area_code);
        areaCode.setText(countryNumber);
        rl_phone = (RelativeLayout) findViewById(R.id.rl_phone);
        rl_email = (RelativeLayout) findViewById(R.id.rl_email);
        resetPhone = (Button) findViewById(R.id.btnTab001);
        resetEmail = (Button) findViewById(R.id.btnTab002);
        layout_tab = (LinearLayout) findViewById(R.id.layout_tab);

        resetPhone.setOnClickListener(this);
        resetEmail.setOnClickListener(this);
        back.setOnClickListener(this);
        vcodeBtn.setOnClickListener(this);
        reset.setOnClickListener(this);
        areaCode.setOnClickListener(this);

        if (num == 0) {
            etPhone.setText(r_phone);
            layout_tab.setBackgroundResource(R.mipmap.choice_left);
            resetPhone.setTextColor(Color.WHITE);
            resetEmail.setTextColor(Color.BLUE);
            rl_phone.setVisibility(View.VISIBLE);
            rl_email.setVisibility(View.GONE);
            setTabSelected(layout_tab, resetPhone);
        } else if (num == 1) {
            etEmail.setText(r_email);
            layout_tab.setBackgroundResource(R.mipmap.choice_right);
            resetPhone.setTextColor(Color.BLUE);
            resetEmail.setTextColor(Color.WHITE);
            rl_phone.setVisibility(View.GONE);
            rl_email.setVisibility(View.VISIBLE);
            setTabSelected(layout_tab, resetEmail);
        }

    }

    @Override
    public void setTabSelected(LinearLayout ll, Button btn) {
        presenter.setTabSelected(ll, btn);
    }

    @Override
    public void getData() {
        phone = etPhone.getText().toString();
        email = etEmail.getText().toString();
        pwd = etPwd.getText().toString();
        if (num == 0) {
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.enter_phone_number_first), Toast.LENGTH_LONG).show();
            } else {
                if (countryNumber.equals("+86")) {
                    account = phone;
                } else {
                    account = countryNumber.replace("+", "00") + phone;
                }
            }
        } else if (num == 1) {
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.enter_email_first), Toast.LENGTH_LONG).show();
            } else {
                account = email;
            }
        }
    }

    @Override
    public void getVcode(ACAccountMgr accountMgr, Button btn, String account, int num) {
        presenter.getVcode(accountMgr, btn, account, num);
    }

    @Override
    public void resetPassword(ACAccountMgr accountMgr, String account, String pwd, String vcode) {
        if (TextUtils.isEmpty(account)) {
            if (num == 0) {
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.phone_not_empty), Toast.LENGTH_LONG).show();
                return;
            } else if (num == 1) {
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.email_not_empty), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (TextUtils.isEmpty(vcode)){
            Toast.makeText(ResetPasswordActivity.this,getString(R.string.vcode_not_empty),Toast.LENGTH_LONG).show();
            return;
        }

        presenter.resetPassword(accountMgr,account,pwd,vcode);
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 13:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String countryName = bundle.getString("countryName");
                    countryNumber = bundle.getString("countryNumber");
                    PreferencesUtils.putString(ResetPasswordActivity.this,
                            "areacode", countryNumber);// 保存区号

                    areaCode.setText(countryNumber);

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

class MyBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BroadcastAction.resetPasswordSuccess)) {
            ResetPasswordActivity.this.finish();
        }
    }
}
}
