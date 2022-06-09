package com.scorpio.bsjg.ui

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceClient
import com.amap.api.fence.GeoFenceClient.*
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.location.DPoint
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.navi.AmapPageType
import com.google.gson.Gson
import com.moufans.lib_base.base.activity.BaseActivity
import com.moufans.lib_base.ext.convertReqExecute
import com.moufans.lib_base.utils.LogUtil
import com.moufans.lib_base.utils.StatusBarUtil
import com.moufans.lib_base.utils.ToastUtil
import com.moufans.lib_base.utils.span.AndroidSpan
import com.ruffian.library.widget.RLinearLayout
import com.ruffian.library.widget.RTextView
import com.ruffian.library.widget.RView
import com.scorpio.bsjg.R
import com.scorpio.bsjg.adapter.ProjectAddressListAdapter
import com.scorpio.bsjg.bean.FindAdminInfoDataBean
import com.scorpio.bsjg.bean.ProjectListDataBean
import com.scorpio.bsjg.bean.RequestParamJsonBean
import com.scorpio.bsjg.bean.TransportDataBean
import com.scorpio.bsjg.constants.AppConstants
import com.scorpio.bsjg.databinding.ActivityMapBinding
import com.scorpio.bsjg.event.LoginEvent
import com.scorpio.bsjg.ext.appApi
import com.scorpio.bsjg.utils.CommonDialogUtil
import com.scorpio.bsjg.utils.DeviceIdUtil
import com.scorpio.bsjg.utils.DialogBuilder
import com.scorpio.bsjg.utils.SharedPrefUtil
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.concurrent.Task
import org.greenrobot.eventbus.EventBus
import java.util.*


class MapActivity : BaseActivity<ActivityMapBinding>(), AMap.OnMapLoadedListener {
    private var user: FindAdminInfoDataBean = Gson().fromJson(SharedPrefUtil.get(AppConstants.USER, ""), FindAdminInfoDataBean::class.java)
    private val mStartProjectAddressListAdapter by lazy {
        ProjectAddressListAdapter()
    }
    private val mEndProjectAddressListAdapter by lazy {
        ProjectAddressListAdapter()
    }
    private val mBuLuStartProjectAddressListAdapter by lazy {
        ProjectAddressListAdapter()
    }
    private val mBuLuEndProjectAddressListAdapter by lazy {
        ProjectAddressListAdapter()
    }

    /**
     * 需要进行检测的权限数组
     */
    private var needPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )

    //是否需要检测后台定位权限，设置为true时，如果用户没有给予后台定位权限会弹窗提示
    private val needCheckBackLocation = false

    // 判断是否需要检测，防止不停的弹框
    private var isNeedCheck = true

    // 地图控件
    private var mMapView: MapView? = null
    private var aMap: AMap? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationLatLng: LatLng? = null

    // 实例化地理围栏客户端
    private var mGeoFenceClient: GeoFenceClient? = null

    // 起始點
    private var mStartPointProjectListDataBean: ProjectListDataBean? = null
    private var mEndPointProjectListDataBean: ProjectListDataBean? = null
    private var mBLStartPointProjectListDataBean: ProjectListDataBean? = null
    private var mBLEndPointProjectListDataBean: ProjectListDataBean? = null
    private var mTransportDataBean: TransportDataBean? = null

    // true:更改起始点 false：首次设置的起始点
    private var mIsChangeStartPoint = false

    // true:更改終点 false：首次设置的終点
    private var mIsChangeEndPoint = false

    override fun getDataBindingLayoutResId(): Int {
        return R.layout.activity_map
    }

    override fun setStatusBar() {
        StatusBarUtil.setTransparentForImageViewInFragment(this, null)
        StatusBarUtil.setLightMode(this)
    }

    override fun addHeaderView() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT > 28 && applicationContext.applicationInfo.targetSdkVersion > 28) {
            needPermissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                BACKGROUND_LOCATION_PERMISSION
            )
        }
        AMapLocationClient.updatePrivacyShow(this@MapActivity, true, true)
        AMapLocationClient.updatePrivacyAgree(this@MapActivity, true)
        mMapView = findViewById(R.id.mMapView)
        aMap = mMapView?.map
        aMap!!.setOnMapLoadedListener(this)
        aMap!!.uiSettings.isZoomControlsEnabled = false
        // 地图点击事件
        aMap?.setOnMapClickListener {
            if (mDataBinding.mFirstLayout.visibility == View.VISIBLE) {
                mDataBinding.mCenterLayout.visibility = View.GONE
                mDataBinding.mStartClockInRTextView.visibility = View.VISIBLE
                mDataBinding.mEndClockInRTextView.visibility = View.VISIBLE
                if (mDataBinding.mBuLuOrderLayout.visibility == View.GONE) {
                    mDataBinding.mBuLuOrderLayout.visibility = View.VISIBLE
                }
                if (mStartPointProjectListDataBean == null) {
                    // 如果还没设置起始点，显示设置起始点按钮
                    mDataBinding.mClickSettingStartLayout.visibility = View.VISIBLE
                } else if (mEndPointProjectListDataBean == null) {
                    // 如果还没设置终点，显示设置终点按钮
                    // 显示设置终点按钮
                    mDataBinding.mClickSettingEndLayout.visibility = View.VISIBLE
                    mDataBinding.mBuLuOrderLayout.visibility = View.GONE
                }
            } else if (mDataBinding.mSecondLayout.visibility == View.VISIBLE) {
                mDataBinding.mBuLuCenterLayout.visibility = View.GONE
            }

        }
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView?.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView?.onResume()
        if (Build.VERSION.SDK_INT >= 23 && applicationInfo.targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(*needPermissions)
            }
        }
    }

    override fun initView() {
        mDataBinding.mStarPointTextView.text = AndroidSpan().drawCommonSpan("起始项目：").drawForegroundColor("未设置起始点", Color.parseColor("#D0021B")).spanText
        mDataBinding.mEndPointTextView.text = AndroidSpan().drawCommonSpan("终点：").drawForegroundColor("未设置终点", Color.parseColor("#D0021B")).spanText

        mDataBinding.apply {
            mLeftRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MapActivity)
                adapter = mStartProjectAddressListAdapter
            }
            mRightRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MapActivity)
                adapter = mEndProjectAddressListAdapter
            }
            mBuLuLeftRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MapActivity)
                adapter = mBuLuStartProjectAddressListAdapter
            }
            mBuLuRightRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@MapActivity)
                adapter = mBuLuEndProjectAddressListAdapter
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initListener() {
        mDataBinding.apply {
            // 返回
            mBackTextView.setOnClickListener {

                if (mTaskLayout.visibility == View.GONE) {
                    startActivity(newIntent(this@MapActivity))
                    finish()
                } else {
                    CommonDialogUtil.commonGeneralDialog(this@MapActivity, "", "返回到首页还是退出登录", object : CommonDialogUtil.OnButtonClickListener {
                        override fun onLeftButtonClick(dialog: DialogBuilder) {
                            startActivity(LoginModeChoiceActivity.newIntent(this@MapActivity))
                            finish()
                        }

                        override fun onRightOrCenterButtonClick(dialog: DialogBuilder) {
                            startActivity(HomeActivity.newIntent(this@MapActivity))
                            finish()
                        }
                    }, true, "退出登录", "首页")
                }


            }

            // 放大
            mAddTextView.setOnClickListener {
                aMap?.moveCamera(CameraUpdateFactory.zoomIn())
            }
            // 缩小
            mSubTextView.setOnClickListener {
                aMap?.moveCamera(CameraUpdateFactory.zoomOut())
            }
            // 关闭/显示
            mCloseTextView.setOnClickListener {
                val text = mCloseTextView.text.toString()
                if ("关闭" == text) {
                    mCloseTextView.text = "打开显示"
                    mRightCarLayout.visibility = View.GONE
                    mRightStartLayout.visibility = View.GONE
                    mRightEndLayout.visibility = View.GONE
                    mRightAllLayout.visibility = View.GONE
                } else if ("打开显示" == text) {
                    mCloseTextView.text = "关闭"
                    mRightCarLayout.visibility = View.VISIBLE
                    mRightStartLayout.visibility = View.VISIBLE
                    mRightEndLayout.visibility = View.VISIBLE
                    mRightAllLayout.visibility = View.VISIBLE
                }
            }
            // 车辆
            mRightCarLayout.setOnClickListener {
                dealMapByClickRightButton(1)
            }
            // 起点
            mRightStartLayout.setOnClickListener {
                dealMapByClickRightButton(2)
            }
            // 终点
            mRightEndLayout.setOnClickListener {
                dealMapByClickRightButton(3)
            }
            // 全部
            mRightAllLayout.setOnClickListener {
                dealMapByClickRightButton(4)
            }
            // 继续上次任务
            mContinueLastTaskLayout.setOnClickListener {
                findAdminInfo(true)
            }
            // 新建任务
            mNewTaskLayout.setOnClickListener {
                mTaskLayout.visibility = View.GONE
                mFirstLayout.visibility = View.VISIBLE
            }
            // 查看任务记录
            mCheckTaskLayout.setOnClickListener {
                startActivity(Intent(this@MapActivity, TaskRecordListActivity::class.java))
            }
            // 设置起始点
            mClickSettingStartLayout.setOnClickListener {
                mStartClockInRTextView.visibility = View.GONE
                mCenterLayout.visibility = View.VISIBLE
                mLeftCenterLayout.visibility = View.VISIBLE
                mClickSettingStartLayout.visibility = View.GONE
                mRightCenterLayout.visibility = View.GONE
                mIsChangeStartPoint = false
                getProjectList("", 1)
            }
            // 更改起始点位置
            mLeftTopTextView.setOnClickListener {

                if (mStartPointProjectListDataBean != null && mStartClockInRTextView.text.toString() == "打卡") {
                    if (isClickCardRequesting) {
                        ToastUtil.showShort("正在打卡请求，请稍后操作")
                        return@setOnClickListener
                    }
                    mStartClockInRTextView.visibility = View.GONE
                    mCenterLayout.visibility = View.VISIBLE
                    mLeftCenterLayout.visibility = View.VISIBLE
                    mRightCenterLayout.visibility = View.GONE
                    mIsChangeStartPoint = true
                    getProjectList("", 1)
                } else {
                    if (mStartClockInRTextView.text.toString() == "已打卡") {
                        ToastUtil.showShort("已打卡，不能更改位置")
                    } else {
                        ToastUtil.showShort("未设置起点")
                    }

                }

            }
            // 起始点导航
            mLeftBottomTextView.setOnClickListener {
                // 起点
//                val start = Poi("北京首都机场", LatLng(40.080525, 116.603039), "B000A28DAE")
                // 途经点
//                val poiList: MutableList<Poi> = ArrayList()
//                poiList.add(Poi("故宫", LatLng(39.918058, 116.397026), "B000A8UIN8"))
                //终点
                val end = Poi(mStartPointProjectListDataBean!!.name, LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), "")
                // 组件参数配置
                val params = AmapNaviParams(null, null, end, AmapNaviType.DRIVER, AmapPageType.NAVI)
                // 启动组件
                AmapNaviPage.getInstance().showRouteActivity(applicationContext, params, null)
            }
            mStartProjectAddressListAdapter.setOnItemClickListener { _, _, position ->
                mStartProjectAddressListAdapter.checkedPosition = position
                mStartProjectAddressListAdapter.notifyDataSetChanged()
            }
            mInputStartKeywordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getProjectList(mInputStartKeywordEditText.text.toString(), 1)
                    true
                }
                false
            }
            // 起始点完成选择
            mLeftCompleteChoiceRTextView.setOnClickListener {
                if (mStartProjectAddressListAdapter.checkedPosition == -1) {
                    ToastUtil.showShort("请选择起始点位置")
                    return@setOnClickListener
                }
                mStartClockInRTextView.visibility = View.VISIBLE
                // 隐藏选择
                mCenterLayout.visibility = View.GONE
                // 如果还没设置终点，显示设置终点按钮
                if (mEndPointProjectListDataBean == null) {
                    // 显示设置终点按钮
                    mClickSettingEndLayout.visibility = View.VISIBLE
                    mBuLuOrderLayout.visibility = View.GONE
                }
                // 选择的起始地bean
                mStartPointProjectListDataBean = mStartProjectAddressListAdapter.data[mStartProjectAddressListAdapter.checkedPosition]
                // 设置值
                mDataBinding.mStarPointTextView.text = AndroidSpan().drawCommonSpan("起始项目：${mStartPointProjectListDataBean?.name}\n").drawForegroundColor("地址：${mStartPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText

                if (mIsChangeStartPoint && mTransportDataBean != null) {
                    transportUpdate(true)
                } else {
                    if (mEndPointProjectListDataBean != null && mTransportDataBean == null) {
                        transportInsert()
                    }
                }
            }
            // 选择终点位置
            mClickSettingEndLayout.setOnClickListener {
                mEndClockInRTextView.visibility = View.GONE
                mClickSettingEndLayout.visibility = View.GONE
                mCenterLayout.visibility = View.VISIBLE
                mLeftCenterLayout.visibility = View.GONE
                mBuLuOrderLayout.visibility = View.GONE
                mRightCenterLayout.visibility = View.VISIBLE
                mIsChangeEndPoint = false
                getProjectList("", 2)
            }
            // 更改终点位置
            mRightTopTextView.setOnClickListener {
                if (mEndPointProjectListDataBean != null && mEndClockInRTextView.text.toString() == "打卡") {
                    if (isClickCardRequesting) {
                        ToastUtil.showShort("正在打卡请求，请稍后操作")
                        return@setOnClickListener
                    }
                    mEndClockInRTextView.visibility = View.GONE
                    mCenterLayout.visibility = View.VISIBLE
                    mLeftCenterLayout.visibility = View.GONE
                    mBuLuOrderLayout.visibility = View.GONE
                    mRightCenterLayout.visibility = View.VISIBLE
                    mIsChangeEndPoint = true
                    getProjectList("", 2)
                } else {
                    if (mEndClockInRTextView.text.toString() == "已打卡") {
                        ToastUtil.showShort("已打卡，不能更改位置")
                    } else {
                        ToastUtil.showShort("未设置终点")
                    }
                }

            }
            // 终点导航
            mRightBottomTextView.setOnClickListener {
                // 起点
//                val start = Poi("北京首都机场", LatLng(40.080525, 116.603039), "B000A28DAE")
                // 途经点
//                val poiList: MutableList<Poi> = ArrayList()
//                poiList.add(Poi("故宫", LatLng(39.918058, 116.397026), "B000A8UIN8"))
                //终点
                val end = Poi(mEndPointProjectListDataBean!!.name, LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), "")
                // 组件参数配置
                val params = AmapNaviParams(null, null, end, AmapNaviType.DRIVER, AmapPageType.NAVI)
                // 启动组件
                AmapNaviPage.getInstance().showRouteActivity(applicationContext, params, null)
            }
            mEndProjectAddressListAdapter.setOnItemClickListener { _, _, position ->
                mEndProjectAddressListAdapter.checkedPosition = position
                mEndProjectAddressListAdapter.notifyDataSetChanged()
            }
            mInputEndKeywordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getProjectList(mInputEndKeywordEditText.text.toString(), 2)
                    true
                }
                false
            }
            // 终点完成选择
            mRightCompleteChoiceRTextView.setOnClickListener {
                if (mEndProjectAddressListAdapter.checkedPosition == -1) {
                    ToastUtil.showShort("请选择终点位置")
                    return@setOnClickListener
                }
                mEndClockInRTextView.visibility = View.VISIBLE
                // 选择的终点位置bean
                mEndPointProjectListDataBean = mEndProjectAddressListAdapter.data[mEndProjectAddressListAdapter.checkedPosition]
                // 隐藏列表
                mCenterLayout.visibility = View.GONE
                mBuLuOrderLayout.visibility = View.VISIBLE
                // 设置值
                mDataBinding.mEndPointTextView.text = AndroidSpan().drawCommonSpan("终点：${mEndPointProjectListDataBean?.name}\n").drawForegroundColor("地址：${mEndPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText

                if (mStartPointProjectListDataBean != null) {
                    // 显示起始点导航按钮
                    mLeftBottomTextView.visibility = View.VISIBLE
                }
                // 显示终点导航按钮
                mRightBottomTextView.visibility = View.VISIBLE

                if (mIsChangeEndPoint && mTransportDataBean != null) {
                    transportUpdate(false)
                } else {
                    if (mStartPointProjectListDataBean != null && mTransportDataBean == null) {
                        transportInsert()
                    }

                }
            }
            // 起始点打卡
            mStartClockInRTextView.setOnClickListener {
                if (mStartPointProjectListDataBean != null && mLocationLatLng != null) {
                    val distance = AMapUtils.calculateLineDistance(LatLng(mLocationLatLng!!.latitude, mLocationLatLng!!.longitude),
                        LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                    if (distance <= (mStartPointProjectListDataBean!!.extent ?: "0").toDouble()) {
                        clickCard(true, mLocationLatLng!!.longitude, mLocationLatLng!!.latitude)
                    }
                } else {
                    if (mStartPointProjectListDataBean == null) {
                        ToastUtil.showShort("请选择起始点")
                        return@setOnClickListener
                    }
                    if (mLocationLatLng == null) {
                        ToastUtil.showShort("定位失败")
                        return@setOnClickListener
                    }
                }
            }
            // 终点打卡
            mEndClockInRTextView.setOnClickListener {
                if (mEndPointProjectListDataBean != null && mLocationLatLng != null) {
                    val distance = AMapUtils.calculateLineDistance(LatLng(mLocationLatLng!!.latitude, mLocationLatLng!!.longitude),
                        LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                    if (distance <= (mEndPointProjectListDataBean!!.extent ?: "0").toDouble()) {
                        clickCard(false, mLocationLatLng!!.longitude, mLocationLatLng!!.latitude)
                    }
                } else {
                    if (mEndPointProjectListDataBean == null) {
                        ToastUtil.showShort("请选择终点")
                        return@setOnClickListener
                    }
                    if (mLocationLatLng == null) {
                        ToastUtil.showShort("定位失败")
                        return@setOnClickListener
                    }
                }
            }
            // 补录工单
            mAdditionalRecordingRTextView.setOnClickListener {
                mFirstLayout.visibility = View.GONE
                mBuLuCenterLayout.visibility = View.GONE
                mSecondLayout.visibility = View.VISIBLE
            }
            // 打卡情况
            mDaKaLayout.setOnClickListener {
                startActivity(Intent(this@MapActivity, TaskRecordListActivity::class.java))
            }
            // 补录起始项目
            mBuLuStartTextView.setOnClickListener {
                mFirstLayout.visibility = View.GONE
                mSecondLayout.visibility = View.VISIBLE
                mBuLuCenterLayout.visibility = View.VISIBLE
                mBuLuRightCenterLayout.visibility = View.GONE
                mBuLuLeftCenterLayout.visibility = View.VISIBLE

                getProjectList("", 3)
            }
            mBuLuInputStartKeywordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getProjectList(mBuLuInputStartKeywordEditText.text.toString(), 3)
                    true
                }
                false
            }
            mBuLuStartProjectAddressListAdapter.setOnItemClickListener { adapter, view, position ->
                mBuLuCenterLayout.visibility = View.GONE
                mBLStartPointProjectListDataBean = mBuLuStartProjectAddressListAdapter.data[position]
                mBuLuStartTextView.text = mBLStartPointProjectListDataBean?.name
            }
            // 补录终点项目
            mBuLuEndTextView.setOnClickListener {
                mFirstLayout.visibility = View.GONE
                mSecondLayout.visibility = View.VISIBLE
                mBuLuCenterLayout.visibility = View.VISIBLE
                mBuLuRightCenterLayout.visibility = View.VISIBLE
                mBuLuLeftCenterLayout.visibility = View.GONE
                getProjectList("", 4)
            }
            mBuLuInputEndKeywordEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    getProjectList(mBuLuInputEndKeywordEditText.text.toString(), 4)
                    true
                }
                false
            }
            mBuLuEndProjectAddressListAdapter.setOnItemClickListener { adapter, view, position ->
                mBuLuCenterLayout.visibility = View.GONE
                mBLEndPointProjectListDataBean = mBuLuEndProjectAddressListAdapter.data[position]
                mBuLuEndTextView.text = mBLEndPointProjectListDataBean?.name
            }
            // 选择年月日
            mBuLuSelectDayTextView.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    DatePickerDialog(this@MapActivity, 0, { _, year, month, dayOfMonth ->
                        val m = month + 1
                        val mStr = if (m < 10) "0$m" else m
                        val d = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth
                        mBuLuSelectDayTextView.text = "$year-$mStr-$d"
                    }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show()
                }
            }
            // 选择年月日
            mBuLuSelectTimeTextView.setOnClickListener {
                TimePickerDialog(this@MapActivity, { _, hourOfDay, minute ->
                    val h = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay
                    val m = if (minute < 10) "0$minute" else minute
                    mBuLuSelectTimeTextView.text = "$h:$m"
                }, 0, 0, true).show()
            }
            // 提交审核
            mSubmitTextView.setOnClickListener {
                mFirstLayout.visibility = View.VISIBLE
                mSecondLayout.visibility = View.GONE

                transportAddCard()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun processingLogic() {
        registerGeoFenceBroadcast()
        transportFData()
        findAdminInfo()
    }

    /**
     * 查询用户最近一条记录情况
     */
    private fun transportFData() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.transportFData() }, onSuccess = {
                mDataBinding.mLastTaskContentTextView.apply {
                    visibility = if (it != null) View.VISIBLE else View.GONE
                    text = "辛苦了！你已完成：${it.prname} 至  ${it.poname}  运输任务     请选择后续工作"
                }
            }, onFailure = { _, _, _ ->
                mDataBinding.mLastTaskContentTextView.visibility = View.GONE
            }, baseView = this@MapActivity)
        }
    }

    private fun findAdminInfo(isStartTask: Boolean = false) {
        lifecycleScope.launch {
            val mac = DeviceIdUtil.getDeviceId(this@MapActivity)
            convertReqExecute({ appApi.findAdminInfo(mac) }, onSuccess = {
                EventBus.getDefault().post(LoginEvent())
                // 保存用户信息
                SharedPrefUtil.put(AppConstants.USER, Gson().toJson(it))
                user = it
                setUserInfo()
                if (isStartTask) {
                    if (TextUtils.isEmpty(it.taskId)) {
                        ToastUtil.showShort("暂无继续的任务")
                    } else {
                        transportFindById()
                    }
                }

            }, onFailure = { _, _, _ ->
                setUserInfo()
            }, isShowToast = false)
        }

    }

    private fun setUserInfo() {
        mDataBinding.mNumRedTextView.text = if (TextUtils.isEmpty(user.count.toString()) || user.count == "0") "0" else user.count
        mDataBinding.mNumRedTextView.visibility = if (TextUtils.isEmpty(user.count.toString()) || user.count == "0") View.GONE else View.VISIBLE
        mDataBinding.mLackCardTimesTextView.text = "缺卡 ${user.count ?: "0"} 次"
        mDataBinding.mDriverRTextView.text = "司机：${user.name}  ${user.carPlate}"
        // 有未完成的任务
        mDataBinding.mContinueLastTaskLayout.visibility = View.VISIBLE
        mDataBinding.mCheckTaskLayout.visibility = View.VISIBLE
    }

    var isCarMax = false

    /**
     * @param type 1:车辆 2:起点 3:终点 4:全部
     */
    private fun dealMapByClickRightButton(type: Int) {
        isCarMax = type == 1
        mDataBinding.apply {
            setIsSelected(mRightCarLayout, mRightCarNameTextView, mRightCarLineView, mRightCarTipTextView, type == 1)
            setIsSelected(mRightStartLayout, mRightStartNameTextView, mRightStartLineView, mRightStartTipTextView, type == 2)
            setIsSelected(mRightEndLayout, mRightEndNameTextView, mRightEndLineView, mRightEndTipTextView, type == 3)
            setIsSelected(mRightAllLayout, mRightAllNameTextView, mRightAllLineView, mRightAllTipTextView, type == 4)
            when (type) {
                // 车辆
                1 -> {
                    if (mLocationLatLng != null)
                        zoomToSpanWithCenter(mutableListOf<LatLng>().apply {
                            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocationLatLng, 18f))
                        })
                }
                // 起点
                2 -> {
                    // 起始点经纬度
                    if (mStartPointProjectListDataBean != null && mStartPointProjectListDataBean!!.coordinate != null) {
                        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), 18f))
                    }
                }
                // 终点
                3 -> {
                    // 终点经纬度
                    if (mEndPointProjectListDataBean != null && mEndPointProjectListDataBean!!.coordinate != null) {
                        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), 18f))
                    }

                }
                // 全部
                4 -> {
                    if (mStartPointProjectListDataBean != null && mEndPointProjectListDataBean != null && mLocationLatLng != null) {
                        zoomToSpanWithCenter(mutableListOf<LatLng>().apply {
                            // 起始点经纬度
                            if (mStartPointProjectListDataBean != null && mStartPointProjectListDataBean!!.coordinate != null) {
                                add(LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                            }
                            // 终点经纬度
                            if (mEndPointProjectListDataBean != null && mEndPointProjectListDataBean!!.coordinate != null) {
                                add(LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                            }
                            // 定位经纬度
                            mLocationLatLng?.let { add(it) }
                        })
                    }

                }
            }
        }
    }

    /**
     * 设置选中和非选中
     */
    private fun setIsSelected(layout: RLinearLayout, nameTextView: RTextView, lineView: RView, tipTextView: RTextView, isSelected: Boolean) {
        layout.isSelected = isSelected
        nameTextView.isSelected = isSelected
        lineView.isSelected = isSelected
        tipTextView.isSelected = isSelected
    }

    /**
     * 根据主键查询单条记录
     */
    private fun transportFindById() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.transportFindById(user?.taskId ?: "") }, onSuccess = {
                mDataBinding.apply {
                    mTaskLayout.visibility = View.GONE
                    mFirstLayout.visibility = View.VISIBLE
                    mBuLuOrderLayout.visibility = View.VISIBLE
                    mClickSettingStartLayout.visibility = View.GONE
                    // 任务bean
                    mTransportDataBean = it
                    // 构建起始点bean
                    mStartPointProjectListDataBean = ProjectListDataBean().apply {
                        id = "起始点打卡id"
                        name = it.prname
                        siteName = it.prsiteName
                        extent = it.prExtent
                        coordinate = ProjectListDataBean.Coordinate().apply {
                            x = it.prCoordinate?.x
                            y = it.prCoordinate?.y
                        }
                    }
                    // 构建终点bean
                    mEndPointProjectListDataBean = ProjectListDataBean().apply {
                        id = "终点打卡id"
                        name = it.poname
                        extent = it.poExtent
                        siteName = it.positeName
                        coordinate = ProjectListDataBean.Coordinate().apply {
                            x = it.poCoordinate?.x
                            y = it.poCoordinate?.y
                        }
                    }
                    // 显示导航按钮
                    mLeftBottomTextView.visibility = View.VISIBLE
                    mRightBottomTextView.visibility = View.VISIBLE
                    // 设置起始点和终点名称和地址
                    mDataBinding.mStarPointTextView.text = AndroidSpan().drawCommonSpan("起始项目：${mStartPointProjectListDataBean?.name}\n").drawForegroundColor("地址：${mStartPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText
                    mDataBinding.mEndPointTextView.text = AndroidSpan().drawCommonSpan("终点：${mEndPointProjectListDataBean?.name}\n").drawForegroundColor("地址：${mEndPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText
                    addStartAndEndMarker()
                    if (TextUtils.isEmpty(it.startCoordinate?.x) && TextUtils.isEmpty(it.startCoordinate?.y)) {
                        mStartClockInRTextView.text = "打卡"
                        initGeoFenceClient(mStartPointProjectListDataBean!!)
                    } else {
                        mStartClockInRTextView.apply {
                            text = "已打卡"
                            helper.apply {
                                backgroundColorNormal = Color.parseColor("#65B651")
                            }
                        }
                        initGeoFenceClient(mEndPointProjectListDataBean!!)
                    }
                }
            }, baseView = this@MapActivity)
        }
    }

    /**
     * @param isProject true:起始点 false:终点
     * @param keyword 搜索关键字
     * @param type 1:起始点 2终点 3补录起始点 4补录终点
     */
    private fun getProjectList(keyword: String, type: Int) {
        lifecycleScope.launch {
            convertReqExecute({ if (1 == type || 3 == type) appApi.getProject(keyword) else appApi.getPoint(keyword) }, onSuccess = {
                when (type) {
                    1 -> {
                        mStartProjectAddressListAdapter.setList(it)
                    }
                    2 -> {
                        mEndProjectAddressListAdapter.setList(it)
                    }
                    3 -> {
                        mBuLuStartProjectAddressListAdapter.setList(it)
                    }
                    4 -> {
                        mBuLuEndProjectAddressListAdapter.setList(it)
                    }
                }
            }, baseView = this@MapActivity)
        }
    }

    /**
     * 新增运输
     */
    private fun transportInsert() {
        if (mStartPointProjectListDataBean != null && mEndPointProjectListDataBean != null) {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                // 项目id（起点）
                startId = mStartPointProjectListDataBean!!.id
                // 倒土点id（终点）
                endId = mEndPointProjectListDataBean!!.id
                // 车辆id
//                cid = "1522127502686863361"
                cid = user.carId
            }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            lifecycleScope.launch {
                convertReqExecute({ appApi.transportInsert(body) }, onSuccess = {
                    mTransportDataBean = it
                    initGeoFenceClient(mStartPointProjectListDataBean!!)
                    addStartAndEndMarker()
                }, baseView = this@MapActivity)
            }
        }
    }

    private fun addStartAndEndMarker() {
        mStartCircle?.remove()
        mEndCircle?.remove()
        mStartMarker?.remove()
        mEndMarker?.remove()
        val startView = BitmapDescriptorFactory.fromView(View.inflate(this, R.layout.layout_marker, null).apply {
            this.findViewById<ImageView>(R.id.mMarkerImageView).setImageResource(R.mipmap.ic_start_point)
        })
        val endView = BitmapDescriptorFactory.fromView(View.inflate(this, R.layout.layout_marker, null).apply {
            this.findViewById<ImageView>(R.id.mMarkerImageView).setImageResource(R.mipmap.ic_end_point)
        })
        val startLatLng = LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble())
        mStartCircle = aMap!!.addCircle(CircleOptions().center(startLatLng).radius(mStartPointProjectListDataBean!!.extent!!.toDouble()).fillColor(Color.parseColor("#6665B651")).strokeColor(Color.parseColor("#65B651")).strokeWidth(5f))

        val endLatLng = LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble())
        mEndCircle = aMap!!.addCircle(CircleOptions().center(endLatLng).radius(mEndPointProjectListDataBean!!.extent!!.toDouble()).fillColor(Color.parseColor("#6665B651")).strokeColor(Color.parseColor("#65B651")).strokeWidth(5f))

        mStartMarker = aMap?.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(startLatLng).icon(startView))
        mEndMarker = aMap?.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(endLatLng).icon(endView))
    }

    /**
     * 修改任务起点终点
     */
    private fun transportUpdate(isUpdateStart: Boolean) {
        if (mStartPointProjectListDataBean != null && mEndPointProjectListDataBean != null && mTransportDataBean != null) {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                if (isUpdateStart)
                // 项目id（起点）
                    startId = mStartPointProjectListDataBean!!.id
                else
                // 倒土点id（终点）
                    endId = mEndPointProjectListDataBean!!.id
                // 任务id
                id = mTransportDataBean?.id
            }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            lifecycleScope.launch {
                convertReqExecute({ appApi.transportUpdate(body) }, onSuccess = {
                    addStartAndEndMarker()
                    if (isUpdateStart) {
                        initGeoFenceClient(mStartPointProjectListDataBean!!)
                    } else {
                        if (mDataBinding.mStartClockInRTextView.text.toString() == "打卡") {
                            initGeoFenceClient(mEndPointProjectListDataBean!!)
                        }
                    }

                }, baseView = this@MapActivity)
            }
        }
    }

    /**
     * 补漏
     */
    private fun transportAddCard() {
        if (mBLStartPointProjectListDataBean == null) {
            ToastUtil.showShort("请选择起始点")
            return
        }
        if (mBLEndPointProjectListDataBean == null) {
            ToastUtil.showShort("请选择终点")
            return
        }
        val times = mDataBinding.mBuLuTransportTimesTextView.text.toString()
        if (TextUtils.isEmpty(times)) {
            ToastUtil.showShort("请输入车次")
            return
        }
        val yearMonthDay = mDataBinding.mBuLuSelectDayTextView.text.toString()
        if (TextUtils.isEmpty(yearMonthDay)) {
            ToastUtil.showShort("请选择年月日")
            return
        }
        val time = mDataBinding.mBuLuSelectTimeTextView.text.toString()
        if (TextUtils.isEmpty(yearMonthDay)) {
            ToastUtil.showShort("请选择时间")
            return
        }
        lifecycleScope.launch {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                // 项目id（起点）
                startId = mBLStartPointProjectListDataBean?.id
                // 倒土点id（终点）
                endId = mBLEndPointProjectListDataBean?.id
                // 次数
                number = times
                // 运输时间（回传的时间格式：“YYYY-MM-dd HH:mm:ss”）
                createTime = "$yearMonthDay $time:00"
                // 车辆id
                carId = user.carId

                ToastUtil.showShort("补录成功")
            }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            convertReqExecute({ appApi.transportAddCard(body) }, onSuccess = {

            })
        }
    }

    /**
     *
     * @param permissions
     * @since 2.5.0
     */
    private fun checkPermissions(vararg permissions: String) {
        try {
            if (Build.VERSION.SDK_INT >= 23 && applicationInfo.targetSdkVersion >= 23) {
                val needRequestPermissonList = findDeniedPermissions(permissions as Array<String>)
                if (null != needRequestPermissonList && needRequestPermissonList.isNotEmpty()) {
                    val array = needRequestPermissonList.toTypedArray()
                    val method = javaClass.getMethod("requestPermissions", *arrayOf<Class<*>?>(Array<String>::class.java, Int::class.javaPrimitiveType))
                    method.invoke(this, array, PERMISSON_REQUESTCODE)
                } else {
                    setupLocationStyle()
                    initLocation()
                    //开始定位
                    startLocation()
                }
            }
        } catch (e: Throwable) {
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private fun findDeniedPermissions(permissions: Array<String>): List<String> {
        val needRequestPermissonList: MutableList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= 23 && applicationInfo.targetSdkVersion >= 23) {
            try {
                for (perm in permissions) {
                    val checkSelfMethod = javaClass.getMethod("checkSelfPermission", String::class.java)
                    val shouldShowRequestPermissionRationaleMethod = javaClass.getMethod("shouldShowRequestPermissionRationale",
                        String::class.java)
                    if (checkSelfMethod.invoke(this, perm) as Int != PackageManager.PERMISSION_GRANTED
                        || shouldShowRequestPermissionRationaleMethod.invoke(this, perm) as Boolean) {
                        if (!needCheckBackLocation && BACKGROUND_LOCATION_PERMISSION == perm) {
                            continue
                        }
                        needRequestPermissonList.add(perm)
                    }
                }
            } catch (e: Throwable) {
            }
        }
        return needRequestPermissonList
    }

    /**
     * 检测是否所有的权限都已经授权
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    private fun verifyPermissions(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @TargetApi(23) override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, paramArrayOfInt: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, paramArrayOfInt)
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog()
                isNeedCheck = false
            } else {
                setupLocationStyle()
                initLocation()
                //开始定位
                startLocation()
            }
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     */
    private fun showMissingPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("提示")
        builder.setMessage("当前应用缺少必要权限。\\n\\n请点击\\\"设置\\\"-\\\"权限\\\"-打开所需权限。")

        // 拒绝, 退出应用
        builder.setNegativeButton("取消") { _, _ -> finish() }
        builder.setPositiveButton("设置") { _, _ -> startAppSettings() }
        builder.setCancelable(false)
        builder.show()
    }

    /**
     * 启动应用的设置
     *
     * @since 2.5.0
     */
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    /**
     * 设置自定义定位蓝点
     */
    private fun setupLocationStyle() {
        // 设置默认定位按钮是否显示
        aMap!!.uiSettings.isMyLocationButtonEnabled = false
        aMap!!.uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_LEFT
        aMap!!.uiSettings.setLogoBottomMargin(-200)
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap!!.isMyLocationEnabled = true
        // 自定义系统定位蓝点
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.showMyLocation(false)
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.shop_gps_point))
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.TRANSPARENT)
        myLocationStyle.strokeColor(Color.parseColor("#4A90E2"))
        myLocationStyle.strokeWidth(resources.getDimension(com.moufans.lib_base.R.dimen.base_dp0))
        // 将自定义的 myLocationStyle 对象添加到地图上
        aMap!!.myLocationStyle = myLocationStyle
    }

    var mIsFirst = false
    var mLocationMarker: Marker? = null
    var mStartMarker: Marker? = null
    var mEndMarker: Marker? = null

    /**
     * 定位监听
     */
    private var locationListener = AMapLocationListener { aMapLocation ->
        if (null != aMapLocation) {
            mLocationLatLng = LatLng(aMapLocation.latitude, aMapLocation.longitude)
            mLocationMarker?.remove()
            val view = BitmapDescriptorFactory.fromView(View.inflate(this, R.layout.layout_marker, null).apply {
                this.findViewById<ImageView>(R.id.mMarkerImageView).setImageResource(R.mipmap.ic_car_point)
            })
            mLocationMarker = aMap?.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(mLocationLatLng).icon(view))

            if (!mIsFirst || isCarMax) {
                mIsFirst = true
                aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocationLatLng, if (isCarMax) 18f else 15f))
            }

            LogUtil.e("定位成功==========${aMapLocation.latitude}====${aMapLocation.longitude}", "location")
        } else {
            LogUtil.e("location", "定位失败，loc is null")
        }
    }

    /**
     * 初始化定位，设置回调监听
     */
    private fun initLocation() {
        //初始化client
        mLocationClient = AMapLocationClient(this.applicationContext)
        // 设置定位监听
        mLocationClient!!.setLocationListener(locationListener)
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun startLocation() {
        //设置定位参数
        mLocationClient!!.setLocationOption(getOption())
        // 启动定位
        mLocationClient!!.startLocation()
    }

    /**
     * 停止定位
     */
    open fun deactivate() {
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
        mLocationClient = null
    }

    /**
     * 设置定位参数
     *
     * @return 定位参数类
     */
    private fun getOption(): AMapLocationClientOption? {
        val mOption = AMapLocationClientOption()
        mOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式

        mOption.isGpsFirst = false //可选，设置是否gps优先，只在高精度模式下有效。默认关闭

        mOption.httpTimeOut = 30000 //可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效

        mOption.interval = 3000 //可选，设置定位间隔。默认为2秒

        mOption.isNeedAddress = true //可选，设置是否返回逆地理地址信息。默认是true

        mOption.isOnceLocation = false //可选，设置是否单次定位。默认是false

        mOption.isOnceLocationLatest = true //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用

        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP) //可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP

        mOption.isSensorEnable = false //可选，设置是否使用传感器。默认是false

        mOption.isWifiScan = true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差

        mOption.isLocationCacheEnable = true //可选，设置是否使用缓存定位，默认为true

        mOption.geoLanguage = AMapLocationClientOption.GeoLanguage.DEFAULT //可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）


        return mOption
    }

    override fun onMapLoaded() {

    }

    private val mGeoFenceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            LogUtil.e("=============================onReceive====${intent.action}")
            if (intent.action == GEOFENCE_BROADCAST_ACTION) {
                //解析广播内容
                //获取Bundle
                val bundle = intent.extras
                //获取围栏行为：
                val status = bundle!!.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS)
                //获取自定义的围栏标识：
                val customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID)
                //获取围栏ID:
                val fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID)
                //获取当前有触发的围栏对象：
                val fence = bundle.getParcelable<GeoFence>(GeoFence.BUNDLE_KEY_FENCE)

                if (mStartPointProjectListDataBean?.id == customId) {
                    // 起始点打卡
                    clickCard(true, mLocationLatLng?.longitude ?: 0.0, mLocationLatLng?.latitude ?: 0.0)
                } else if (mEndPointProjectListDataBean?.id == customId) {
                    // 终点点打卡
                    clickCard(false, mLocationLatLng?.longitude ?: 0.0, mLocationLatLng?.latitude ?: 0.0)
                }

//                ToastUtil.showShort("$status===$customId=====$fenceId========$fence")

//                mDataBinding.mContentTextView.append("$status===$customId=====$fenceId========$fence\n")

                LogUtil.e("=============================$status===$customId=====$fenceId========${fence?.fenceId}======${fence?.center?.latitude}===${fence.toString()}")
            }
        }
    }

    // true:正在打卡网络请求
    private var isClickCardRequesting = false

    private fun clickCard(isStart: Boolean, longitude: Double, latitude: Double) {
        if (isClickCardRequesting) {
            return
        }
        isClickCardRequesting = true
        lifecycleScope.launch {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                // 运输单号id
                id = mTransportDataBean?.id
                if (isStart) {
                    startCoordinate = RequestParamJsonBean.Coordinate().apply {
                        x = "$longitude"
                        y = "$latitude"
                    }
                } else {
                    endCoordinate = RequestParamJsonBean.Coordinate().apply {
                        x = "$longitude"
                        y = "$latitude"
                    }
                }
            }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            convertReqExecute({ if (isStart) appApi.transportStartCard(body) else appApi.transportEndCard(body) }, onSuccess = {
                isClickCardRequesting = false
//                mGeoFenceClient?.removeGeoFence()
                if (isStart) {
                    mDataBinding.mStartClockInRTextView.apply {
                        text = "已打卡"
                        helper.apply {
                            backgroundColorNormal = Color.parseColor("#65B651")
                        }
                    }
                    mEndPointProjectListDataBean?.let { it1 -> initGeoFenceClient(it1) }
                } else {
                    mDataBinding.mEndClockInRTextView.apply {
                        text = "已打卡"
                        helper.apply {
                            backgroundColorNormal = Color.parseColor("#65B651")
                        }

                        transportFData()
                    }
                }
            }, onFailure = { _, _, _ ->
                isClickCardRequesting = false
            }, baseView = this@MapActivity)
        }
    }

    private var mStartCircle: Circle? = null
    private var mEndCircle: Circle? = null

    private fun initGeoFenceClient(bean: ProjectListDataBean) {
        val id = bean.id ?: ""
        val radius = (bean.extent ?: "0").toDouble()
        val latitude = (bean.coordinate?.y ?: "0").toDouble()
        val longitude = (bean.coordinate!!.x ?: "0").toDouble()

        mGeoFenceClient?.removeGeoFence()
        mGeoFenceClient = GeoFenceClient(applicationContext)
        mGeoFenceClient?.apply {
            // 设置回调监听
            setGeoFenceListener { geoFenceList, errorCode, errorMessage ->
                if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
                    // 判断围栏是否创建成功
                    LogUtil.e("==============创建成功====")
                } else {
                    // 创建失败
                    LogUtil.e("==============创建失败====")
                }
            }
            // 设置希望侦测的围栏触发行为，默认只侦测用户进入围栏的行为
            // GEOFENCE_IN 进入地理围栏 GEOFENCE_OUT 退出地理围栏 GEOFENCE_STAYED 停留在地理围栏内10分钟
            setActivateAction(GEOFENCE_IN)
            // 创建一个中心点坐标
            val centerPoint = DPoint()
            // 设置中心点纬度
            centerPoint.latitude = latitude
            // 设置中心点经度
            centerPoint.longitude = longitude
            // 执行添加围栏的操作
            addGeoFence(centerPoint, radius.toFloat(), id)
            // 创建并设置PendingIntent
            createPendingIntent(GEOFENCE_BROADCAST_ACTION)

            zoomToSpanWithCenter(mutableListOf<LatLng>().apply {
                add(LatLng(bean.coordinate!!.y!!.toDouble(), bean.coordinate!!.x!!.toDouble()))
                mLocationLatLng?.let { add(it) }
            })
        }


    }

    /**
     * 注册广播
     */
    private fun registerGeoFenceBroadcast() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(GEOFENCE_BROADCAST_ACTION)
        registerReceiver(mGeoFenceReceiver, filter)
    }

    /**
     * 反注册广播
     */
    private fun unRegisterGeoFenceBroadcast() {
        unregisterReceiver(mGeoFenceReceiver)
    }

    /**
     * 缩放移动地图，保证所有自定义marker在可视范围中，且地图中心点不变。
     */
    private fun zoomToSpanWithCenter(pointList: List<LatLng>) {
        if (pointList.isNotEmpty()) {
            val bounds = getLatLngBounds(pointList)
            aMap?.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds, resources.getDimension(R.dimen.sw_500dp).toInt(),
                resources.getDimension(R.dimen.sw_500dp).toInt(), resources.getDimension(R.dimen.sw_200dp).toInt(), resources.getDimension(R.dimen.sw_652dp).toInt()))
        }
    }

    /**
     * 根据自定义内容获取缩放bounds
     */
    private fun getLatLngBounds(pointList: List<LatLng>): LatLngBounds {
        val b = LatLngBounds.builder()
        for (i in pointList.indices) {
            val p = pointList[i]
            b.include(p)
        }
        return b.build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView?.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止定位
        deactivate()
        unRegisterGeoFenceBroadcast()
        mGeoFenceClient?.removeGeoFence()
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView?.onDestroy()
        AmapNaviPage.getInstance().onRouteActivityDestroyed()
    }

    companion object {
        //如果设置了target > 28，需要增加这个权限，否则不会弹出"始终允许"这个选择框
        private const val BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"

        // 权限请求码
        private const val PERMISSON_REQUESTCODE = 0

        // 定义接收广播的action字符串
        private const val GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast"

        fun newIntent(context: Context): Intent {
            return Intent(context, MapActivity::class.java)
        }
    }
}