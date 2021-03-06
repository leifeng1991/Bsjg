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
     * ?????????????????????????????????
     */
    private var needPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE
    )

    //????????????????????????????????????????????????true???????????????????????????????????????????????????????????????
    private val needCheckBackLocation = false

    // ????????????????????????????????????????????????
    private var isNeedCheck = true

    // ????????????
    private var mMapView: MapView? = null
    private var aMap: AMap? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationLatLng: LatLng? = null

    // ??????????????????????????????
    private var mGeoFenceClient: GeoFenceClient? = null

    // ?????????
    private var mStartPointProjectListDataBean: ProjectListDataBean? = null
    private var mEndPointProjectListDataBean: ProjectListDataBean? = null
    private var mBLStartPointProjectListDataBean: ProjectListDataBean? = null
    private var mBLEndPointProjectListDataBean: ProjectListDataBean? = null
    private var mTransportDataBean: TransportDataBean? = null

    // true:??????????????? false???????????????????????????
    private var mIsChangeStartPoint = false

    // true:???????????? false????????????????????????
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
        // ??????????????????
        aMap?.setOnMapClickListener {
            if (mDataBinding.mFirstLayout.visibility == View.VISIBLE) {
                mDataBinding.mCenterLayout.visibility = View.GONE
                mDataBinding.mStartClockInRTextView.visibility = View.VISIBLE
                mDataBinding.mEndClockInRTextView.visibility = View.VISIBLE
                if (mDataBinding.mBuLuOrderLayout.visibility == View.GONE) {
                    mDataBinding.mBuLuOrderLayout.visibility = View.VISIBLE
                }
                if (mStartPointProjectListDataBean == null) {
                    // ?????????????????????????????????????????????????????????
                    mDataBinding.mClickSettingStartLayout.visibility = View.VISIBLE
                } else if (mEndPointProjectListDataBean == null) {
                    // ???????????????????????????????????????????????????
                    // ????????????????????????
                    mDataBinding.mClickSettingEndLayout.visibility = View.VISIBLE
                    mDataBinding.mBuLuOrderLayout.visibility = View.GONE
                }
            } else if (mDataBinding.mSecondLayout.visibility == View.VISIBLE) {
                mDataBinding.mBuLuCenterLayout.visibility = View.GONE
            }

        }
        //???activity??????onCreate?????????mMapView.onCreate(savedInstanceState)???????????????
        mMapView?.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        //???activity??????onResume?????????mMapView.onResume ()???????????????????????????
        mMapView?.onResume()
        if (Build.VERSION.SDK_INT >= 23 && applicationInfo.targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(*needPermissions)
            }
        }
    }

    override fun initView() {
        mDataBinding.mStarPointTextView.text = AndroidSpan().drawCommonSpan("???????????????").drawForegroundColor("??????????????????", Color.parseColor("#D0021B")).spanText
        mDataBinding.mEndPointTextView.text = AndroidSpan().drawCommonSpan("?????????").drawForegroundColor("???????????????", Color.parseColor("#D0021B")).spanText

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
            // ??????
            mBackTextView.setOnClickListener {

                if (mTaskLayout.visibility == View.GONE) {
                    startActivity(newIntent(this@MapActivity))
                    finish()
                } else {
                    CommonDialogUtil.commonGeneralDialog(this@MapActivity, "", "?????????????????????????????????", object : CommonDialogUtil.OnButtonClickListener {
                        override fun onLeftButtonClick(dialog: DialogBuilder) {
                            startActivity(LoginModeChoiceActivity.newIntent(this@MapActivity))
                            finish()
                        }

                        override fun onRightOrCenterButtonClick(dialog: DialogBuilder) {
                            startActivity(HomeActivity.newIntent(this@MapActivity))
                            finish()
                        }
                    }, true, "????????????", "??????")
                }


            }

            // ??????
            mAddTextView.setOnClickListener {
                aMap?.moveCamera(CameraUpdateFactory.zoomIn())
            }
            // ??????
            mSubTextView.setOnClickListener {
                aMap?.moveCamera(CameraUpdateFactory.zoomOut())
            }
            // ??????/??????
            mCloseTextView.setOnClickListener {
                val text = mCloseTextView.text.toString()
                if ("??????" == text) {
                    mCloseTextView.text = "????????????"
                    mRightCarLayout.visibility = View.GONE
                    mRightStartLayout.visibility = View.GONE
                    mRightEndLayout.visibility = View.GONE
                    mRightAllLayout.visibility = View.GONE
                } else if ("????????????" == text) {
                    mCloseTextView.text = "??????"
                    mRightCarLayout.visibility = View.VISIBLE
                    mRightStartLayout.visibility = View.VISIBLE
                    mRightEndLayout.visibility = View.VISIBLE
                    mRightAllLayout.visibility = View.VISIBLE
                }
            }
            // ??????
            mRightCarLayout.setOnClickListener {
                dealMapByClickRightButton(1)
            }
            // ??????
            mRightStartLayout.setOnClickListener {
                dealMapByClickRightButton(2)
            }
            // ??????
            mRightEndLayout.setOnClickListener {
                dealMapByClickRightButton(3)
            }
            // ??????
            mRightAllLayout.setOnClickListener {
                dealMapByClickRightButton(4)
            }
            // ??????????????????
            mContinueLastTaskLayout.setOnClickListener {
                findAdminInfo(true)
            }
            // ????????????
            mNewTaskLayout.setOnClickListener {
                mTaskLayout.visibility = View.GONE
                mFirstLayout.visibility = View.VISIBLE
            }
            // ??????????????????
            mCheckTaskLayout.setOnClickListener {
                startActivity(Intent(this@MapActivity, TaskRecordListActivity::class.java))
            }
            // ???????????????
            mClickSettingStartLayout.setOnClickListener {
                mStartClockInRTextView.visibility = View.GONE
                mCenterLayout.visibility = View.VISIBLE
                mLeftCenterLayout.visibility = View.VISIBLE
                mClickSettingStartLayout.visibility = View.GONE
                mRightCenterLayout.visibility = View.GONE
                mIsChangeStartPoint = false
                getProjectList("", 1)
            }
            // ?????????????????????
            mLeftTopTextView.setOnClickListener {

                if (mStartPointProjectListDataBean != null && mStartClockInRTextView.text.toString() == "??????") {
                    if (isClickCardRequesting) {
                        ToastUtil.showShort("????????????????????????????????????")
                        return@setOnClickListener
                    }
                    mStartClockInRTextView.visibility = View.GONE
                    mCenterLayout.visibility = View.VISIBLE
                    mLeftCenterLayout.visibility = View.VISIBLE
                    mRightCenterLayout.visibility = View.GONE
                    mIsChangeStartPoint = true
                    getProjectList("", 1)
                } else {
                    if (mStartClockInRTextView.text.toString() == "?????????") {
                        ToastUtil.showShort("??????????????????????????????")
                    } else {
                        ToastUtil.showShort("???????????????")
                    }

                }

            }
            // ???????????????
            mLeftBottomTextView.setOnClickListener {
                // ??????
//                val start = Poi("??????????????????", LatLng(40.080525, 116.603039), "B000A28DAE")
                // ?????????
//                val poiList: MutableList<Poi> = ArrayList()
//                poiList.add(Poi("??????", LatLng(39.918058, 116.397026), "B000A8UIN8"))
                //??????
                val end = Poi(mStartPointProjectListDataBean!!.name, LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), "")
                // ??????????????????
                val params = AmapNaviParams(null, null, end, AmapNaviType.DRIVER, AmapPageType.NAVI)
                // ????????????
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
            // ?????????????????????
            mLeftCompleteChoiceRTextView.setOnClickListener {
                if (mStartProjectAddressListAdapter.checkedPosition == -1) {
                    ToastUtil.showShort("????????????????????????")
                    return@setOnClickListener
                }
                mStartClockInRTextView.visibility = View.VISIBLE
                // ????????????
                mCenterLayout.visibility = View.GONE
                // ???????????????????????????????????????????????????
                if (mEndPointProjectListDataBean == null) {
                    // ????????????????????????
                    mClickSettingEndLayout.visibility = View.VISIBLE
                    mBuLuOrderLayout.visibility = View.GONE
                }
                // ??????????????????bean
                mStartPointProjectListDataBean = mStartProjectAddressListAdapter.data[mStartProjectAddressListAdapter.checkedPosition]
                // ?????????
                mDataBinding.mStarPointTextView.text = AndroidSpan().drawCommonSpan("???????????????${mStartPointProjectListDataBean?.name}\n").drawForegroundColor("?????????${mStartPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText

                if (mIsChangeStartPoint && mTransportDataBean != null) {
                    transportUpdate(true)
                } else {
                    if (mEndPointProjectListDataBean != null && mTransportDataBean == null) {
                        transportInsert()
                    }
                }
            }
            // ??????????????????
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
            // ??????????????????
            mRightTopTextView.setOnClickListener {
                if (mEndPointProjectListDataBean != null && mEndClockInRTextView.text.toString() == "??????") {
                    if (isClickCardRequesting) {
                        ToastUtil.showShort("????????????????????????????????????")
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
                    if (mEndClockInRTextView.text.toString() == "?????????") {
                        ToastUtil.showShort("??????????????????????????????")
                    } else {
                        ToastUtil.showShort("???????????????")
                    }
                }

            }
            // ????????????
            mRightBottomTextView.setOnClickListener {
                // ??????
//                val start = Poi("??????????????????", LatLng(40.080525, 116.603039), "B000A28DAE")
                // ?????????
//                val poiList: MutableList<Poi> = ArrayList()
//                poiList.add(Poi("??????", LatLng(39.918058, 116.397026), "B000A8UIN8"))
                //??????
                val end = Poi(mEndPointProjectListDataBean!!.name, LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), "")
                // ??????????????????
                val params = AmapNaviParams(null, null, end, AmapNaviType.DRIVER, AmapPageType.NAVI)
                // ????????????
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
            // ??????????????????
            mRightCompleteChoiceRTextView.setOnClickListener {
                if (mEndProjectAddressListAdapter.checkedPosition == -1) {
                    ToastUtil.showShort("?????????????????????")
                    return@setOnClickListener
                }
                mEndClockInRTextView.visibility = View.VISIBLE
                // ?????????????????????bean
                mEndPointProjectListDataBean = mEndProjectAddressListAdapter.data[mEndProjectAddressListAdapter.checkedPosition]
                // ????????????
                mCenterLayout.visibility = View.GONE
                mBuLuOrderLayout.visibility = View.VISIBLE
                // ?????????
                mDataBinding.mEndPointTextView.text = AndroidSpan().drawCommonSpan("?????????${mEndPointProjectListDataBean?.name}\n").drawForegroundColor("?????????${mEndPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText

                if (mStartPointProjectListDataBean != null) {
                    // ???????????????????????????
                    mLeftBottomTextView.visibility = View.VISIBLE
                }
                // ????????????????????????
                mRightBottomTextView.visibility = View.VISIBLE

                if (mIsChangeEndPoint && mTransportDataBean != null) {
                    transportUpdate(false)
                } else {
                    if (mStartPointProjectListDataBean != null && mTransportDataBean == null) {
                        transportInsert()
                    }

                }
            }
            // ???????????????
            mStartClockInRTextView.setOnClickListener {
                if (mStartPointProjectListDataBean != null && mLocationLatLng != null) {
                    val distance = AMapUtils.calculateLineDistance(LatLng(mLocationLatLng!!.latitude, mLocationLatLng!!.longitude),
                        LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                    if (distance <= (mStartPointProjectListDataBean!!.extent ?: "0").toDouble()) {
                        clickCard(true, mLocationLatLng!!.longitude, mLocationLatLng!!.latitude)
                    }
                } else {
                    if (mStartPointProjectListDataBean == null) {
                        ToastUtil.showShort("??????????????????")
                        return@setOnClickListener
                    }
                    if (mLocationLatLng == null) {
                        ToastUtil.showShort("????????????")
                        return@setOnClickListener
                    }
                }
            }
            // ????????????
            mEndClockInRTextView.setOnClickListener {
                if (mEndPointProjectListDataBean != null && mLocationLatLng != null) {
                    val distance = AMapUtils.calculateLineDistance(LatLng(mLocationLatLng!!.latitude, mLocationLatLng!!.longitude),
                        LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                    if (distance <= (mEndPointProjectListDataBean!!.extent ?: "0").toDouble()) {
                        clickCard(false, mLocationLatLng!!.longitude, mLocationLatLng!!.latitude)
                    }
                } else {
                    if (mEndPointProjectListDataBean == null) {
                        ToastUtil.showShort("???????????????")
                        return@setOnClickListener
                    }
                    if (mLocationLatLng == null) {
                        ToastUtil.showShort("????????????")
                        return@setOnClickListener
                    }
                }
            }
            // ????????????
            mAdditionalRecordingRTextView.setOnClickListener {
                mFirstLayout.visibility = View.GONE
                mBuLuCenterLayout.visibility = View.GONE
                mSecondLayout.visibility = View.VISIBLE
            }
            // ????????????
            mDaKaLayout.setOnClickListener {
                startActivity(Intent(this@MapActivity, TaskRecordListActivity::class.java))
            }
            // ??????????????????
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
            // ??????????????????
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
            // ???????????????
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
            // ???????????????
            mBuLuSelectTimeTextView.setOnClickListener {
                TimePickerDialog(this@MapActivity, { _, hourOfDay, minute ->
                    val h = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay
                    val m = if (minute < 10) "0$minute" else minute
                    mBuLuSelectTimeTextView.text = "$h:$m"
                }, 0, 0, true).show()
            }
            // ????????????
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
     * ????????????????????????????????????
     */
    private fun transportFData() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.transportFData() }, onSuccess = {
                mDataBinding.mLastTaskContentTextView.apply {
                    visibility = if (it != null) View.VISIBLE else View.GONE
                    text = "???????????????????????????${it.prname} ???  ${it.poname}  ????????????     ?????????????????????"
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
                // ??????????????????
                SharedPrefUtil.put(AppConstants.USER, Gson().toJson(it))
                user = it
                setUserInfo()
                if (isStartTask) {
                    if (TextUtils.isEmpty(it.taskId)) {
                        ToastUtil.showShort("?????????????????????")
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
        mDataBinding.mLackCardTimesTextView.text = "?????? ${user.count ?: "0"} ???"
        mDataBinding.mDriverRTextView.text = "?????????${user.name}  ${user.carPlate}"
        // ?????????????????????
        mDataBinding.mContinueLastTaskLayout.visibility = View.VISIBLE
        mDataBinding.mCheckTaskLayout.visibility = View.VISIBLE
    }

    var isCarMax = false

    /**
     * @param type 1:?????? 2:?????? 3:?????? 4:??????
     */
    private fun dealMapByClickRightButton(type: Int) {
        isCarMax = type == 1
        mDataBinding.apply {
            setIsSelected(mRightCarLayout, mRightCarNameTextView, mRightCarLineView, mRightCarTipTextView, type == 1)
            setIsSelected(mRightStartLayout, mRightStartNameTextView, mRightStartLineView, mRightStartTipTextView, type == 2)
            setIsSelected(mRightEndLayout, mRightEndNameTextView, mRightEndLineView, mRightEndTipTextView, type == 3)
            setIsSelected(mRightAllLayout, mRightAllNameTextView, mRightAllLineView, mRightAllTipTextView, type == 4)
            when (type) {
                // ??????
                1 -> {
                    if (mLocationLatLng != null)
                        zoomToSpanWithCenter(mutableListOf<LatLng>().apply {
                            aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocationLatLng, 18f))
                        })
                }
                // ??????
                2 -> {
                    // ??????????????????
                    if (mStartPointProjectListDataBean != null && mStartPointProjectListDataBean!!.coordinate != null) {
                        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), 18f))
                    }
                }
                // ??????
                3 -> {
                    // ???????????????
                    if (mEndPointProjectListDataBean != null && mEndPointProjectListDataBean!!.coordinate != null) {
                        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()), 18f))
                    }

                }
                // ??????
                4 -> {
                    if (mStartPointProjectListDataBean != null && mEndPointProjectListDataBean != null && mLocationLatLng != null) {
                        zoomToSpanWithCenter(mutableListOf<LatLng>().apply {
                            // ??????????????????
                            if (mStartPointProjectListDataBean != null && mStartPointProjectListDataBean!!.coordinate != null) {
                                add(LatLng(mStartPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mStartPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                            }
                            // ???????????????
                            if (mEndPointProjectListDataBean != null && mEndPointProjectListDataBean!!.coordinate != null) {
                                add(LatLng(mEndPointProjectListDataBean!!.coordinate!!.y!!.toDouble(), mEndPointProjectListDataBean!!.coordinate!!.x!!.toDouble()))
                            }
                            // ???????????????
                            mLocationLatLng?.let { add(it) }
                        })
                    }

                }
            }
        }
    }

    /**
     * ????????????????????????
     */
    private fun setIsSelected(layout: RLinearLayout, nameTextView: RTextView, lineView: RView, tipTextView: RTextView, isSelected: Boolean) {
        layout.isSelected = isSelected
        nameTextView.isSelected = isSelected
        lineView.isSelected = isSelected
        tipTextView.isSelected = isSelected
    }

    /**
     * ??????????????????????????????
     */
    private fun transportFindById() {
        lifecycleScope.launch {
            convertReqExecute({ appApi.transportFindById(user?.taskId ?: "") }, onSuccess = {
                mDataBinding.apply {
                    mTaskLayout.visibility = View.GONE
                    mFirstLayout.visibility = View.VISIBLE
                    mBuLuOrderLayout.visibility = View.VISIBLE
                    mClickSettingStartLayout.visibility = View.GONE
                    // ??????bean
                    mTransportDataBean = it
                    // ???????????????bean
                    mStartPointProjectListDataBean = ProjectListDataBean().apply {
                        id = "???????????????id"
                        name = it.prname
                        siteName = it.prsiteName
                        extent = it.prExtent
                        coordinate = ProjectListDataBean.Coordinate().apply {
                            x = it.prCoordinate?.x
                            y = it.prCoordinate?.y
                        }
                    }
                    // ????????????bean
                    mEndPointProjectListDataBean = ProjectListDataBean().apply {
                        id = "????????????id"
                        name = it.poname
                        extent = it.poExtent
                        siteName = it.positeName
                        coordinate = ProjectListDataBean.Coordinate().apply {
                            x = it.poCoordinate?.x
                            y = it.poCoordinate?.y
                        }
                    }
                    // ??????????????????
                    mLeftBottomTextView.visibility = View.VISIBLE
                    mRightBottomTextView.visibility = View.VISIBLE
                    // ???????????????????????????????????????
                    mDataBinding.mStarPointTextView.text = AndroidSpan().drawCommonSpan("???????????????${mStartPointProjectListDataBean?.name}\n").drawForegroundColor("?????????${mStartPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText
                    mDataBinding.mEndPointTextView.text = AndroidSpan().drawCommonSpan("?????????${mEndPointProjectListDataBean?.name}\n").drawForegroundColor("?????????${mEndPointProjectListDataBean?.siteName}", Color.parseColor("#9B9B9B")).spanText
                    addStartAndEndMarker()
                    if (TextUtils.isEmpty(it.startCoordinate?.x) && TextUtils.isEmpty(it.startCoordinate?.y)) {
                        mStartClockInRTextView.text = "??????"
                        initGeoFenceClient(mStartPointProjectListDataBean!!)
                    } else {
                        mStartClockInRTextView.apply {
                            text = "?????????"
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
     * @param isProject true:????????? false:??????
     * @param keyword ???????????????
     * @param type 1:????????? 2?????? 3??????????????? 4????????????
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
     * ????????????
     */
    private fun transportInsert() {
        if (mStartPointProjectListDataBean != null && mEndPointProjectListDataBean != null) {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                // ??????id????????????
                startId = mStartPointProjectListDataBean!!.id
                // ?????????id????????????
                endId = mEndPointProjectListDataBean!!.id
                // ??????id
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
     * ????????????????????????
     */
    private fun transportUpdate(isUpdateStart: Boolean) {
        if (mStartPointProjectListDataBean != null && mEndPointProjectListDataBean != null && mTransportDataBean != null) {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                if (isUpdateStart)
                // ??????id????????????
                    startId = mStartPointProjectListDataBean!!.id
                else
                // ?????????id????????????
                    endId = mEndPointProjectListDataBean!!.id
                // ??????id
                id = mTransportDataBean?.id
            }).toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            lifecycleScope.launch {
                convertReqExecute({ appApi.transportUpdate(body) }, onSuccess = {
                    addStartAndEndMarker()
                    if (isUpdateStart) {
                        initGeoFenceClient(mStartPointProjectListDataBean!!)
                    } else {
                        if (mDataBinding.mStartClockInRTextView.text.toString() == "??????") {
                            initGeoFenceClient(mEndPointProjectListDataBean!!)
                        }
                    }

                }, baseView = this@MapActivity)
            }
        }
    }

    /**
     * ??????
     */
    private fun transportAddCard() {
        if (mBLStartPointProjectListDataBean == null) {
            ToastUtil.showShort("??????????????????")
            return
        }
        if (mBLEndPointProjectListDataBean == null) {
            ToastUtil.showShort("???????????????")
            return
        }
        val times = mDataBinding.mBuLuTransportTimesTextView.text.toString()
        if (TextUtils.isEmpty(times)) {
            ToastUtil.showShort("???????????????")
            return
        }
        val yearMonthDay = mDataBinding.mBuLuSelectDayTextView.text.toString()
        if (TextUtils.isEmpty(yearMonthDay)) {
            ToastUtil.showShort("??????????????????")
            return
        }
        val time = mDataBinding.mBuLuSelectTimeTextView.text.toString()
        if (TextUtils.isEmpty(yearMonthDay)) {
            ToastUtil.showShort("???????????????")
            return
        }
        lifecycleScope.launch {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                // ??????id????????????
                startId = mBLStartPointProjectListDataBean?.id
                // ?????????id????????????
                endId = mBLEndPointProjectListDataBean?.id
                // ??????
                number = times
                // ??????????????????????????????????????????YYYY-MM-dd HH:mm:ss??????
                createTime = "$yearMonthDay $time:00"
                // ??????id
                carId = user.carId

                ToastUtil.showShort("????????????")
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
                    //????????????
                    startLocation()
                }
            }
        } catch (e: Throwable) {
        }
    }

    /**
     * ?????????????????????????????????????????????
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
     * ??????????????????????????????????????????
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
                //????????????
                startLocation()
            }
        }
    }

    /**
     * ??????????????????
     *
     * @since 2.5.0
     */
    private fun showMissingPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("??????")
        builder.setMessage("?????????????????????????????????\\n\\n?????????\\\"??????\\\"-\\\"??????\\\"-?????????????????????")

        // ??????, ????????????
        builder.setNegativeButton("??????") { _, _ -> finish() }
        builder.setPositiveButton("??????") { _, _ -> startAppSettings() }
        builder.setCancelable(false)
        builder.show()
    }

    /**
     * ?????????????????????
     *
     * @since 2.5.0
     */
    private fun startAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    /**
     * ???????????????????????????
     */
    private fun setupLocationStyle() {
        // ????????????????????????????????????
        aMap!!.uiSettings.isMyLocationButtonEnabled = false
        aMap!!.uiSettings.logoPosition = AMapOptions.LOGO_POSITION_BOTTOM_LEFT
        aMap!!.uiSettings.setLogoBottomMargin(-200)
        // ?????????true??????????????????????????????????????????false??????????????????????????????????????????????????????false
        aMap!!.isMyLocationEnabled = true
        // ???????????????????????????
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.showMyLocation(false)
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
        // ???????????????????????????
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.shop_gps_point))
        // ???????????????????????????
        myLocationStyle.radiusFillColor(Color.TRANSPARENT)
        myLocationStyle.strokeColor(Color.parseColor("#4A90E2"))
        myLocationStyle.strokeWidth(resources.getDimension(com.moufans.lib_base.R.dimen.base_dp0))
        // ??????????????? myLocationStyle ????????????????????????
        aMap!!.myLocationStyle = myLocationStyle
    }

    var mIsFirst = false
    var mLocationMarker: Marker? = null
    var mStartMarker: Marker? = null
    var mEndMarker: Marker? = null

    /**
     * ????????????
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

            LogUtil.e("????????????==========${aMapLocation.latitude}====${aMapLocation.longitude}", "location")
        } else {
            LogUtil.e("location", "???????????????loc is null")
        }
    }

    /**
     * ????????????????????????????????????
     */
    private fun initLocation() {
        //?????????client
        mLocationClient = AMapLocationClient(this.applicationContext)
        // ??????????????????
        mLocationClient!!.setLocationListener(locationListener)
    }

    /**
     * ????????????
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun startLocation() {
        //??????????????????
        mLocationClient!!.setLocationOption(getOption())
        // ????????????
        mLocationClient!!.startLocation()
    }

    /**
     * ????????????
     */
    open fun deactivate() {
        mLocationClient?.stopLocation()
        mLocationClient?.onDestroy()
        mLocationClient = null
    }

    /**
     * ??????????????????
     *
     * @return ???????????????
     */
    private fun getOption(): AMapLocationClientOption? {
        val mOption = AMapLocationClientOption()
        mOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //????????????????????????????????????????????????????????????????????????????????????????????????????????????

        mOption.isGpsFirst = false //?????????????????????gps??????????????????????????????????????????????????????

        mOption.httpTimeOut = 30000 //???????????????????????????????????????????????????30?????????????????????????????????

        mOption.interval = 3000 //???????????????????????????????????????2???

        mOption.isNeedAddress = true //????????????????????????????????????????????????????????????true

        mOption.isOnceLocation = false //?????????????????????????????????????????????false

        mOption.isOnceLocationLatest = true //???????????????????????????wifi??????????????????false.???????????????true,?????????????????????????????????????????????????????????

        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP) //????????? ????????????????????????????????????HTTP??????HTTPS????????????HTTP

        mOption.isSensorEnable = false //????????????????????????????????????????????????false

        mOption.isWifiScan = true //???????????????????????????wifi??????????????????true??????????????????false??????????????????????????????????????????????????????????????????????????????????????????????????????

        mOption.isLocationCacheEnable = true //???????????????????????????????????????????????????true

        mOption.geoLanguage = AMapLocationClientOption.GeoLanguage.DEFAULT //??????????????????????????????????????????????????????????????????????????????????????????????????????


        return mOption
    }

    override fun onMapLoaded() {

    }

    private val mGeoFenceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            LogUtil.e("=============================onReceive====${intent.action}")
            if (intent.action == GEOFENCE_BROADCAST_ACTION) {
                //??????????????????
                //??????Bundle
                val bundle = intent.extras
                //?????????????????????
                val status = bundle!!.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS)
                //?????????????????????????????????
                val customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID)
                //????????????ID:
                val fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID)
                //???????????????????????????????????????
                val fence = bundle.getParcelable<GeoFence>(GeoFence.BUNDLE_KEY_FENCE)

                if (mStartPointProjectListDataBean?.id == customId) {
                    // ???????????????
                    clickCard(true, mLocationLatLng?.longitude ?: 0.0, mLocationLatLng?.latitude ?: 0.0)
                } else if (mEndPointProjectListDataBean?.id == customId) {
                    // ???????????????
                    clickCard(false, mLocationLatLng?.longitude ?: 0.0, mLocationLatLng?.latitude ?: 0.0)
                }

//                ToastUtil.showShort("$status===$customId=====$fenceId========$fence")

//                mDataBinding.mContentTextView.append("$status===$customId=====$fenceId========$fence\n")

                LogUtil.e("=============================$status===$customId=====$fenceId========${fence?.fenceId}======${fence?.center?.latitude}===${fence.toString()}")
            }
        }
    }

    // true:????????????????????????
    private var isClickCardRequesting = false

    private fun clickCard(isStart: Boolean, longitude: Double, latitude: Double) {
        if (isClickCardRequesting) {
            return
        }
        isClickCardRequesting = true
        lifecycleScope.launch {
            val body = Gson().toJson(RequestParamJsonBean().apply {
                // ????????????id
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
                        text = "?????????"
                        helper.apply {
                            backgroundColorNormal = Color.parseColor("#65B651")
                        }
                    }
                    mEndPointProjectListDataBean?.let { it1 -> initGeoFenceClient(it1) }
                } else {
                    mDataBinding.mEndClockInRTextView.apply {
                        text = "?????????"
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
            // ??????????????????
            setGeoFenceListener { geoFenceList, errorCode, errorMessage ->
                if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
                    // ??????????????????????????????
                    LogUtil.e("==============????????????====")
                } else {
                    // ????????????
                    LogUtil.e("==============????????????====")
                }
            }
            // ????????????????????????????????????????????????????????????????????????????????????
            // GEOFENCE_IN ?????????????????? GEOFENCE_OUT ?????????????????? GEOFENCE_STAYED ????????????????????????10??????
            setActivateAction(GEOFENCE_IN)
            // ???????????????????????????
            val centerPoint = DPoint()
            // ?????????????????????
            centerPoint.latitude = latitude
            // ?????????????????????
            centerPoint.longitude = longitude
            // ???????????????????????????
            addGeoFence(centerPoint, radius.toFloat(), id)
            // ???????????????PendingIntent
            createPendingIntent(GEOFENCE_BROADCAST_ACTION)

            zoomToSpanWithCenter(mutableListOf<LatLng>().apply {
                add(LatLng(bean.coordinate!!.y!!.toDouble(), bean.coordinate!!.x!!.toDouble()))
                mLocationLatLng?.let { add(it) }
            })
        }


    }

    /**
     * ????????????
     */
    private fun registerGeoFenceBroadcast() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        filter.addAction(GEOFENCE_BROADCAST_ACTION)
        registerReceiver(mGeoFenceReceiver, filter)
    }

    /**
     * ???????????????
     */
    private fun unRegisterGeoFenceBroadcast() {
        unregisterReceiver(mGeoFenceReceiver)
    }

    /**
     * ??????????????????????????????????????????marker????????????????????????????????????????????????
     */
    private fun zoomToSpanWithCenter(pointList: List<LatLng>) {
        if (pointList.isNotEmpty()) {
            val bounds = getLatLngBounds(pointList)
            aMap?.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds, resources.getDimension(R.dimen.sw_500dp).toInt(),
                resources.getDimension(R.dimen.sw_500dp).toInt(), resources.getDimension(R.dimen.sw_200dp).toInt(), resources.getDimension(R.dimen.sw_652dp).toInt()))
        }
    }

    /**
     * ?????????????????????????????????bounds
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
        //???activity??????onSaveInstanceState?????????mMapView.onSaveInstanceState (outState)??????????????????????????????
        mMapView?.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        //???activity??????onPause?????????mMapView.onPause ()????????????????????????
        mMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ????????????
        deactivate()
        unRegisterGeoFenceBroadcast()
        mGeoFenceClient?.removeGeoFence()
        //???activity??????onDestroy?????????mMapView.onDestroy()???????????????
        mMapView?.onDestroy()
        AmapNaviPage.getInstance().onRouteActivityDestroyed()
    }

    companion object {
        //???????????????target > 28????????????????????????????????????????????????"????????????"???????????????
        private const val BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"

        // ???????????????
        private const val PERMISSON_REQUESTCODE = 0

        // ?????????????????????action?????????
        private const val GEOFENCE_BROADCAST_ACTION = "com.location.apis.geofencedemo.broadcast"

        fun newIntent(context: Context): Intent {
            return Intent(context, MapActivity::class.java)
        }
    }
}