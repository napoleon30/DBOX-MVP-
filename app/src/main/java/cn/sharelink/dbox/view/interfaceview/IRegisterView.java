package cn.sharelink.dbox.view.interfaceview;

import android.widget.Button;
import android.widget.LinearLayout;

import com.accloud.service.ACAccountMgr;

/**
 * Created by WangLei on 2018/2/1.
 */

public interface IRegisterView {
    void initData();
    void initView();
    void register(ACAccountMgr accountMgr,String email,String phone,String pwd,String name,String vcode);
    void getData();
    void setTabSelected(LinearLayout ll, Button btn);
    void getVcode(ACAccountMgr accountMgr,Button btn,String account, int num);
}
