package cn.sharelink.dbox.presenter.IPresenter;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;

import com.accloud.service.ACAccountMgr;

/**
 * Created by WangLei on 2018/2/1.
 */

public interface ILoginPresenter {
    void login(Context context, ACAccountMgr accountMgr, String phoneOrEmail, String password);
    void setTabSelected(LinearLayout ll, Button btn);
}
