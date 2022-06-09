package com.moufans.update.update;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.moufans.update.R;
import com.moufans.update.event.ProgressEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;


/**
 * 描述： 版本更新的Dialog
 */

public class UpdateVersionDialog extends Dialog implements View.OnClickListener {
    Activity context;
    private TextView tvCancle;
    private TextView tvOk;
    private TextView tvContent;
    private TextView tv_title;
    private ConstraintLayout mProgressLayout;
    private ProgressBar mProgressBar;
    private TextView mProgressTipTextView;
    private TextView mProgressNumberTextView;
    private View mCenterLineView;
    String url;
    String content;
    String title;
    private boolean isForceUpdate;
    UpdateVersionListener listener;

    public void setOnUpdateNickListener(UpdateVersionListener listener) {
        this.listener = listener;
    }

    public UpdateVersionDialog(@NonNull Activity context, String title, String content, String url, boolean isForceUpdate) {
        super(context, R.style.dialog_reward);
        this.context = context;
        this.url = url;
        this.content = content;
        this.title = title;
        this.isForceUpdate = isForceUpdate;
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_update_version);
        initView();
        setViewLocation();
    }

    private void initView() {
        tvCancle = findViewById(R.id.tv_cancle);
        mCenterLineView = findViewById(R.id.mCenterLineView);
        tvOk = findViewById(R.id.tv_ok);
        tvContent = findViewById(R.id.tv_content);
        tv_title = findViewById(R.id.tv_title);
        mProgressBar = findViewById(R.id.mProgressBar);
        mProgressLayout = findViewById(R.id.mProgressLayout);
        mProgressTipTextView = findViewById(R.id.mProgressTipTextView);
        mProgressNumberTextView = findViewById(R.id.mProgressNumberTextView);
        if (TextUtils.isEmpty(content)) {
            tvContent.setText("新版本更新了!!!");
        } else {
            tvContent.setText(content);
        }
        if (TextUtils.isEmpty(title)) {
            tv_title.setText("版本更新");
        } else {
            tv_title.setText(title);
        }
        if (isForceUpdate) tvCancle.setVisibility(View.GONE);
        if (isForceUpdate) mCenterLineView.setVisibility(View.GONE);
        tvOk.setOnClickListener(this);
        tvOk.setOnClickListener(this);
        tvCancle.setOnClickListener(this);

        if (isForceUpdate) setCancelable(false);// 强制更新不能返回取消
        if (isForceUpdate) setCanceledOnTouchOutside(false);// 强制更新不能外面区域取消
    }

    /**
     * 设置dialog位于屏幕底部
     */
    private void setViewLocation() {

        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams attr = window.getAttributes();
            if (attr != null) {
                attr.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.gravity = Gravity.CENTER;//设置dialog 在布局中的位置
            }
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_ok) {//修改名字
            listener.clickUpdate(url, true);
            tvCancle.setVisibility(View.GONE);
            mCenterLineView.setVisibility(View.GONE);
//            if (!isForceUpdate){
//                dismiss();
//            }

            mProgressLayout.setVisibility(View.VISIBLE);
        } else if (i == R.id.tv_cancle) {
            listener.clickUpdate(url, false);
            dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateProgress(ProgressEvent event) {
        mProgressBar.setProgress(event.getProgress());
        mProgressNumberTextView.setText(String.format(Locale.CHINA, "%d%%", event.getProgress()));
        if (event.getProgress() == 100){
            mProgressTipTextView.setText("下载完成，安装中...");
        }
    }

    public interface UpdateVersionListener {
        void clickUpdate(String url, boolean isUpdata);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }
}
