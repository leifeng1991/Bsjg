package com.scorpio.bsjg.constants

interface AppURLConstants {
    companion object {
        // 获取扫码登录key
        const val CAR_USER_PAD_KEY = "car/user/padKey"

        // 获取扫码登录结果
        const val CAR_USER_GET_RESULT = "car/user/getResult"

        // 获取用户信息
        const val CAR_USER_FIND_ADMIN_INFO = "car/user/findAdminInfo"

        // 账号密码登录
        const val CAR_USER_LOGIN = "car/user/login"

        // 获取验证码
        const val CAR_USER_GET_VERIFY_CODE = "car/user/getVerifyCode"

        // 验证码登录
        const val CAR_USER_GET_LOGIN_CODE = "car/user/loginCode"

        // 退出登录
        const val CAR_USER_GET_LOGIN_OUT = "car/user/loginOut"

        // 验证码注册
        const val CAR_USER_GET_LOGON_CODE = "car/user/logonCode"

        // 查询角色列表
        const val CAR_USER_SELIDENTITY = "car/user/selidentity"

        // 绑定角色实名
        const val CAR_USER_UPDATE = "car/user/update"

        // 查询当前设备信息
        const val CAR_TRANSPORT_SELECT_ONE = "car/transport/selectOne"

        // 车辆与设备绑定
        const val CAR_TRANSPORT_UP_CAR = "car/transport/upcar"

        // 车辆与设备解绑
        const val CAR_TRANSPORT_UP_CAR_NO = "car/transport/upcarNo"

        // 查询项目列表
        const val CAR_TRANSPORT_GET_PROJECT = "car/transport/getProject"

        // 查询倒土点列表
        const val CAR_TRANSPORT_GET_POINT = "car/transport/getPoint"

        // 新增运输
        const val CAR_TRANSPORT_INSERT = "car/transport/insert"

        // 起点打卡
        const val CAR_TRANSPORT_START_CARD = "car/transport/startcard"

        // 终点打卡
        const val CAR_TRANSPORT_END_CARD = "car/transport/endcard"

        // 分页查询记录
        const val CAR_TRANSPORT_SEL_CARD = "car/transport/selcard"

        // 根据主键查询单条记录
        const val CAR_TRANSPORT_FIND_BY_ID = "car/transport/findById/{id}"

        // 查询用户最近一条记录情况
        const val CAR_TRANSPORT_FIND_DATA = "car/transport/findData"

        // 补卡
        const val CAR_TRANSPORT_UP_CARD = "car/transport/upcard/{id}"

        // 补漏
        const val CAR_TRANSPORT_ADD_CARD = "car/transport/addcard"

        // 分个人单日缺卡次数查询
        const val CAR_TRANSPORT_GET_COUNT = "car/transport/getCount"

        // 修改任务起点终点
        const val CAR_TRANSPORT_UPDATE = "car/transport/update"

        // 查询是否有能使用该模块
        const val CAR_MODEL_SELECT_ONE = "car/model/selectOne/{code}"
        // 版本检查
        const val PHONE_MODEL_SELECT_ONE_NEW_VERSION = "car/edition/selectOne/{newversion}"

    }
}