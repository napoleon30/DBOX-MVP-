package cn.sharelink.dbox.view.interfaceview;

import android.widget.Button;
import android.widget.LinearLayout;

import com.accloud.service.ACAccountMgr;

/**
 * Created by WangLei on 2018/2/2.
 */

public interface IResetPasswordView {
    void initData();
    void initView();
    void setTabSelected(LinearLayout ll,Button btn);
    void getData();
    void getVcode(ACAccountMgr accountMgr,Button btn,String account,int num);
    void resetPassword(ACAccountMgr accountMgr,String account,String pwd,String vcode);
}
