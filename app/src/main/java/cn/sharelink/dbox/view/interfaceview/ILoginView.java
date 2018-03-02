package cn.sharelink.dbox.view.interfaceview;

import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by WangLei on 2018/2/1.
 */

public interface ILoginView {
    void initView();
    void login();
    void getData();
    void setTabSelected(LinearLayout layout_tab,Button btn);
}
