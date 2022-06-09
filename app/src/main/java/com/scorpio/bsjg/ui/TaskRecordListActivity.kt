package com.scorpio.bsjg.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.scorpio.bsjg.R
import com.scorpio.bsjg.adapter.TaskRecordListAdapter
import com.scorpio.bsjg.bean.FindAdminInfoDataBean
import com.scorpio.bsjg.constants.AppConstants
import com.scorpio.bsjg.databinding.ActivityTaskRecordListBinding
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.CommonDialogUtil
import com.scorpio.bsjg.utils.DateTimeUtil
import com.scorpio.bsjg.utils.DialogBuilder
import com.scorpio.bsjg.utils.SharedPrefUtil
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

class TaskRecordListActivity : BaseActivity<ActivityTaskRecordListBinding>() {
    private var user: FindAdminInfoDataBean = Gson().fromJson(SharedPrefUtil.get(AppConstants.USER, ""), FindAdminInfoDataBean::class.java)

    private val mTaskRecordListAdapter by lazy {
        TaskRecordListAdapter()
    }

    private var mYearMonth = ""

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_task_record_list
    }

    override fun addHeaderView() {

    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setDarkMode(this)
    }

    override fun initView() {
        mDataBinding.mTaskRecyclerView.setPullRefreshAndLoadingMoreEnabled(false, loadingMoreEnabled = false)
        mDataBinding.mTaskRecyclerView.setLayoutManager(LinearLayoutManager(this))
        mDataBinding.mTaskRecyclerView.setAdapter(mTaskRecordListAdapter)
    }

    override fun initListener() {
        mDataBinding.apply {
            // 返回
            mBackTextView.setOnClickListener {
                finish()
            }
            // 首页
            mHomeTextView.setOnClickListener {
                startActivity(HomeActivity.newIntent(this@TaskRecordListActivity))
            }
            mTaskRecyclerView.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    getListData()
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getListData()
                }

            })
            mTaskRecordListAdapter.addChildClickViewIds(R.id.mApplyTextView)
            mTaskRecordListAdapter.setOnItemChildClickListener { adapter, view, position ->
                CommonDialogUtil.commonGeneralDialog(this@TaskRecordListActivity, "提示", "是否申请补卡?", object : CommonDialogUtil.OnButtonClickListener {
                    override fun onLeftButtonClick(dialog: DialogBuilder) {
                        val bean = mTaskRecordListAdapter.data[position]
                        lifecycleScope.launch {
                            convertReqExecute({ appApi.upcard(bean.id ?: "") }, onSuccess = {
                                bean.deleted = "2"
                                mTaskRecordListAdapter.notifyDataSetChanged()
                            })
                        }
                    }

                    override fun onRightOrCenterButtonClick(dialog: DialogBuilder) {
                    }

                }, false, "确定", "取消")
            }
            // 缺卡查询
            mLackCardQueriesTextView.setOnClickListener {

            }
        }
    }

    override fun processingLogic() {
        val mYear = DateTimeUtil.getCurrentTime().split("-")[0]
        val month = DateTimeUtil.getCurrentTime().split("-")[1]
        for (i in 0..month.toInt()) {
            mDataBinding.mMonthLayout.addView(LayoutInflater.from(this).inflate(R.layout.item_task_record_month, null).apply {
                findViewById<TextView>(R.id.mMonthTextView).apply {
                    text = if (i == 0) "全部" else "${i}月"
                    setOnClickListener {
                        mDataBinding.mTaskRecyclerView.currentPage = 1
                        if (i == 0) {
                            mYearMonth = ""
                            getListData()
                        } else {
                            val monthStr = if (i < 10) "0$i" else "$i"
                            mYearMonth = "$mYear-$monthStr"
                            getListData()
                        }
                    }
                }
            })
        }

        getListData()
    }

    private fun getListData() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.selCard(mYearMonth, user.carId ?: "", "2", "${mDataBinding.mTaskRecyclerView.currentPage}") }, onSuccess = {
                mDataBinding.mTaskRecyclerView.handlerSuccess(mTaskRecordListAdapter, it.records)
            }, onFailure = { _, status, _ ->
                mDataBinding.mTaskRecyclerView.handlerError(mTaskRecordListAdapter, status)
            }, baseView = this@TaskRecordListActivity)
        }
    }
}