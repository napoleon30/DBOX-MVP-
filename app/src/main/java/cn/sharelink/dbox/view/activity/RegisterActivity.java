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
import cn.sharelink.dbox.presenter.IPresenter.IRegisterPresenter;
import cn.sharelink.dbox.presenter.presenterCompl.RegisterPresenter;
import cn.sharelink.dbox.view.interfaceview.IRegisterView;

public class RegisterActivity extends AppCompatActivity implements IRegisterView, View.OnClickListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private EditText editPhone;
    private EditText editEmail;
    private EditText editPwd;
    private EditText editRePwd;
    private EditText editVCode;
    private Button vCodeBtn;
    private Button registerBtn;
    private TextView back;
    private TextView areaCode;
    private RelativeLayout rl_phone;
    private RelativeLayout rl_email;

    private Button registerPhone;
    private Button registerEmail;
    private LinearLayout layout_tab;

    String phone;
    String email;
    String pwd;
    String rePwd;
    String vcode;
    String countryNumber = "+86";

    int num;
    ACAccountMgr accountMgr;
    IRegisterPresenter presenter;
    MyBroadCastRecevice myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initData();
        initView();
    }

    @Override
    public void initData() {
        num = PreferencesUtils.getInt(RegisterActivity.this, "num", 0);
        presenter = new RegisterPresenter(RegisterActivity.this);
        myReceiver = new MyBroadCastRecevice();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.registerSuccess);
        registerReceiver(myReceiver, filter);
    }

    @Override
    public void initView() {
        rl_phone = (RelativeLayout) findViewById(R.id.rl_phone);
        rl_email = (RelativeLayout) findViewById(R.id.rl_emali);
        editPhone = (EditText) findViewById(R.id.register_edit_tel);
        editEmail = (EditText) findViewById(R.id.register_edit_email);
        editPwd = (EditText) findViewById(R.id.register_edit_pwd);
        editRePwd = (EditText) findViewById(R.id.register_edit_repwd);
        editRePwd = (EditText) findViewById(R.id.register_edit_repwd);
        editVCode = (EditText) findViewById(R.id.register_edit_vcode);
        vCodeBtn = (Button) findViewById(R.id.register_vcode);
        registerBtn = (Button) findViewById(R.id.register);
        back = (TextView) findViewById(R.id.register_back);
        areaCode = (TextView) findViewById(R.id.area_code);

        registerPhone = (Button) findViewById(R.id.btnTab001);
        registerEmail = (Button) findViewById(R.id.btnTab002);
        layout_tab = (LinearLayout) findViewById(R.id.layout_tab);

        if (num == 0) {
            layout_tab.setBackgroundResource(R.mipmap.choice_left);
            registerPhone.setTextColor(Color.WHITE);
            registerEmail.setTextColor(Color.BLUE);
            rl_phone.setVisibility(View.VISIBLE);
            rl_email.setVisibility(View.GONE);
            setTabSelected(layout_tab, registerPhone);
        } else if (num == 1) {
            layout_tab.setBackgroundResource(R.mipmap.choice_right);
            registerPhone.setTextColor(Color.BLUE);
            registerEmail.setTextColor(Color.WHITE);
            rl_phone.setVisibility(View.GONE);
            rl_email.setVisibility(View.VISIBLE);
            setTabSelected(layout_tab, registerEmail);
        }
        registerPhone.setOnClickListener(this);
        registerEmail.setOnClickListener(this);

        // 通过AC获取账号管理�?
        accountMgr = AC.accountMgr();

        vCodeBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        back.setOnClickListener(this);
        areaCode.setOnClickListener(this);
        registerPhone.setOnClickListener(this);
        registerEmail.setOnClickListener(this);

    }

    @Override
    public void register(ACAccountMgr accountMgr, String email, String phone, String pwd, String name, String vcode) {
        if (num==0 && TextUtils.isEmpty(phone)){
            Toast.makeText(RegisterActivity.this,getString(R.string.phone_not_empty),Toast.LENGTH_LONG).show();
            return;
        }
        if (num==1 && TextUtils.isEmpty(email)){
            Toast.makeText(RegisterActivity.this,getString(R.string.email_not_empty),Toast.LENGTH_LONG).show();
            return;
        }
        if (!pwd.equals(rePwd)) {
            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.rePwd_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(editVCode.getText().toString())) {
            Toast.makeText(RegisterActivity.this, getString(R.string.vcode_not_empty), Toast.LENGTH_LONG).show();
            return;
        }
        if (num == 0 && pwd.equals(rePwd) && !TextUtils.isEmpty(editVCode.getText().toString())) {
            email = "";
            presenter.register(accountMgr, email, phone, pwd, name, editVCode.getText().toString());

        } else if (num == 1 && pwd.equals(rePwd) && !TextUtils.isEmpty(editVCode.getText().toString())) {
            phone = "";
            presenter.register(accountMgr, email, phone, pwd, name, editVCode.getText().toString());
        }
    }

    @Override
    public void getData() {
        phone = editPhone.getText().toString();
        if (!countryNumber.equals("+86")) {
            phone = countryNumber.replace("+", "00") + phone;
        }
        email = editEmail.getText().toString();
        pwd = editPwd.getText().toString();
        rePwd = editRePwd.getText().toString();
    }

    @Override
    public void setTabSelected(LinearLayout ll, Button btn) {
        presenter.setTabSelected(ll, btn);
    }

    @Override
    public void getVcode(ACAccountMgr accountMgr, Button btn, String account, int num) { //获取验证码
        if (pwd.equals(rePwd)) {
            presenter.getVCode(accountMgr, btn, account, num);
        } else {
            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.rePwd_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register_back:
                RegisterActivity.this.finish();
                break;
            case R.id.btnTab001:
                layout_tab.setBackgroundResource(R.mipmap.choice_left);
                registerPhone.setTextColor(Color.WHITE);
                registerEmail.setTextColor(Color.BLUE);
                rl_phone.setVisibility(View.VISIBLE);
                rl_email.setVisibility(View.GONE);
                num = 0;
                PreferencesUtils.putInt(RegisterActivity.this, "num", num);
                setTabSelected(layout_tab, registerPhone);
                break;
            case R.id.btnTab002:
                layout_tab.setBackgroundResource(R.mipmap.choice_right);
                registerPhone.setTextColor(Color.BLUE);
                registerEmail.setTextColor(Color.WHITE);
                rl_phone.setVisibility(View.GONE);
                rl_email.setVisibility(View.VISIBLE);
                num = 1;
                PreferencesUtils.putInt(RegisterActivity.this, "num", num);
                setTabSelected(layout_tab, registerEmail);
                break;
            case R.id.register_vcode:
                String account;
                getData();
                if (num == 0) {
                    if (!TextUtils.isEmpty(phone)) {
                        if (countryNumber.equals("+86")) {
                            account = editPhone.getText().toString();
                        } else {
                            account = countryNumber.replace("+", "00") + editPhone.getText().toString();
                        }
                        getVcode(accountMgr, vCodeBtn, account, num);
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.phone_not_empty), Toast.LENGTH_LONG).show();
                    }
                } else if (num == 1) {
                    if (!TextUtils.isEmpty(email)) {
                        account = editEmail.getText().toString();
                        getVcode(accountMgr, vCodeBtn, account, num);
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.email_not_empty), Toast.LENGTH_LONG).show();
                    }
                }

                break;
            case R.id.register:
                getData();
                vcode = editVCode.getText().toString();
                    register(accountMgr, email, phone, pwd, "", vcode);
                break;
            case R.id.area_code:
                Intent intent = new Intent(RegisterActivity.this,
                        CountryActivity.class);
                startActivityForResult(intent, 14);

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 14:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String countryName = bundle.getString("countryName");
                    countryNumber = bundle.getString("countryNumber");
                    PreferencesUtils.putString(RegisterActivity.this, "areacode",
                            countryNumber);// 保存区号
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

    class MyBroadCastRecevice extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.registerSuccess)) {
                RegisterActivity.this.finish();
            }
        }
    }
}
