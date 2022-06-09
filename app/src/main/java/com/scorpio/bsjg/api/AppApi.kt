package com.scorpio.bsjg.api

import com.moufans.lib_base.request.BaseResp
import com.moufans.update.VersionDataBean
import com.scorpio.bsjg.bean.*
import com.scorpio.bsjg.constants.AppURLConstants
import com.scorpio.bsjg.constants.RequestParamConstants
import com.scorpio.bsjg.constants.RequestParamConstants.Companion.KEY
import com.scorpio.bsjg.constants.RequestParamConstants.Companion.MAC
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.*


interface AppApi {

    @GET(AppURLConstants.CAR_USER_PAD_KEY)
    suspend fun padKey(): BaseResp<String>

    @GET(AppURLConstants.CAR_USER_GET_RESULT)
    suspend fun getResult(@Query(KEY) key: String): BaseResp<FindAdminInfoDataBean>

    /**
     * 获取用户信息
     */
    @GET(AppURLConstants.CAR_USER_FIND_ADMIN_INFO)
    suspend fun findAdminInfo(@Query(MAC) mac: String): BaseResp<FindAdminInfoDataBean>

    /**
     * 账号密码登录
     */
    @POST(AppURLConstants.CAR_USER_LOGIN)
    suspend fun login(@Body requestBody: RequestBody): BaseResp<UserToken>

    /**
     * 获取验证码
     */
    @POST(AppURLConstants.CAR_USER_GET_VERIFY_CODE)
    suspend fun getVerifyCode(@Body requestBody: RequestBody): BaseResp<Unit>

    /**
     * 验证码登录
     */
    @POST(AppURLConstants.CAR_USER_GET_LOGIN_CODE)
    suspend fun loginCode(@Body requestBody: RequestBody): BaseResp<UserToken>

    /**
     * 退出登录
     */
    @POST(AppURLConstants.CAR_USER_GET_LOGIN_OUT)
    suspend fun loginOut(): BaseResp<Unit>

    /**
     * 查询角色列表
     */
    @GET(AppURLConstants.CAR_USER_SELIDENTITY)
    suspend fun selIdentity(): BaseResp<List<RoleDataBean>>

    /**
     * 验证码注册
     */
    @POST(AppURLConstants.CAR_USER_GET_LOGON_CODE)
    suspend fun logonCode(@Body requestBody: RequestBody): BaseResp<UserToken>

    /**
     * 绑定角色实名
     */
    @POST(AppURLConstants.CAR_USER_UPDATE)
    suspend fun userUpdate(@Body requestBody: RequestBody): BaseResp<UserToken>

    /**
     * 查询当前设备信息
     */
    @GET(AppURLConstants.CAR_TRANSPORT_SELECT_ONE)
    suspend fun transportSelectOne(@Query(MAC) mac: String): BaseResp<TransportSelectOneDataBean>

    /**
     * 车辆与设备绑定
     *  参数id：      设备id
     *  参数carPlate：车牌号
     */
    @POST(AppURLConstants.CAR_TRANSPORT_UP_CAR)
    suspend fun upCar(@Body requestBody: RequestBody): BaseResp<UpCarDataBean>

    /**
     * 车辆与设备解绑
     *  参数id：设备id
     */
    @POST(AppURLConstants.CAR_TRANSPORT_UP_CAR_NO)
    suspend fun upCarNo(@Body requestBody: RequestBody): BaseResp<Unit>

    /**
     * 查询项目列表
     */
    @GET(AppURLConstants.CAR_TRANSPORT_GET_PROJECT)
    suspend fun getProject(@Query(RequestParamConstants.KEYWORD) keyWord: String): BaseResp<List<ProjectListDataBean>>

    /**
     * 查询倒土点列表
     */
    @GET(AppURLConstants.CAR_TRANSPORT_GET_POINT)
    suspend fun getPoint(@Query(RequestParamConstants.KEYWORD) keyWord: String): BaseResp<List<ProjectListDataBean>>

    /**
     * 新增运输
     *  参数startId：项目id（起点）
     *  参数endId：  倒土点id（终点）
     *  参数cid：    车辆id
     */
    @POST(AppURLConstants.CAR_TRANSPORT_INSERT)
    suspend fun transportInsert(@Body requestBody: RequestBody): BaseResp<TransportDataBean>


    /**
     * 修改任务起点终点
     *  参数startId：项目id（起点）
     *  参数endId：  倒土点id（终点）
     *  参数cid：    车辆id
     */
    @POST(AppURLConstants.CAR_TRANSPORT_UPDATE)
    suspend fun transportUpdate(@Body requestBody: RequestBody): BaseResp<String>

    /**
     * 起点打卡
     *  参数id：运输单号id
     *  参数x： 经度
     *  参数y： 纬度
     * {"id":"1456158793516498946","startCoordinate":{"x":"12.22","y":"23.33"}}
     */
    @POST(AppURLConstants.CAR_TRANSPORT_START_CARD)
    suspend fun transportStartCard(@Body requestBody: RequestBody): BaseResp<String>

    /**
     * 终点打卡
     *  参数id：运输单号id
     *  参数x： 经度
     *  参数y： 纬度
     * { "id":"1456158793516498946", "endCoordinate":{ "x":"12.22", "y":"23.33" } }
     */
    @POST(AppURLConstants.CAR_TRANSPORT_END_CARD)
    suspend fun transportEndCard(@Body requestBody: RequestBody): BaseResp<String>


    /**
     * 补漏
     */
    @POST(AppURLConstants.CAR_TRANSPORT_ADD_CARD)
    suspend fun transportAddCard(@Body requestBody: RequestBody): BaseResp<String>


    /**
     * 根据主键查询单条记录
     */
    @GET(AppURLConstants.CAR_TRANSPORT_FIND_BY_ID)
    suspend fun transportFindById(@Path("id") id: String): BaseResp<TransportDataBean>

    /**
     * 查询用户最近一条记录情况
     */
    @GET(AppURLConstants.CAR_TRANSPORT_FIND_DATA)
    suspend fun transportFData(): BaseResp<TransportDataBean>

    /**
     * 分页查询记录
     */
    @GET(AppURLConstants.CAR_TRANSPORT_SEL_CARD)
    suspend fun selCard(@Query(RequestParamConstants.YEAR_MONTH) yearMonth: String, @Query(RequestParamConstants.CID) cid: String, @Query(RequestParamConstants.TYPE) type: String, @Query(RequestParamConstants.PAGE_NUM) pageNum: String, @Query(RequestParamConstants.PAGE_SIZE) pageSize: String = "10"): BaseResp<SelCardDataBean>

    /**
     * 检查版本
     */
    @GET(AppURLConstants.PHONE_MODEL_SELECT_ONE_NEW_VERSION)
    fun checkVersion(@Path("newversion") newversion: String): Observable<BaseResp<VersionDataBean>>

    /**
     * 查询是否有能使用该模块
     */
    @GET(AppURLConstants.CAR_MODEL_SELECT_ONE)
    suspend fun selectOneCode(@Path("code") code: String): BaseResp<Boolean>

    /**
     * 查询是否有能使用该模块
     */
    @POST(AppURLConstants.CAR_TRANSPORT_UP_CARD)
    suspend fun upcard(@Path("id") id: String): BaseResp<Boolean>
}

