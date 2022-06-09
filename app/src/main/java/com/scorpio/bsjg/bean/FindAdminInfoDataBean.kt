package com.scorpio.bsjg.bean

class FindAdminInfoDataBean {
    //用户id
    var id: String? = null

    // 秘钥身份信息
    var token: String? = null

    //车牌号
    var carPlate: String? = null

    //角色编码
    var code: String? = null

    //手机号
    var phone: String? = null

    //用户名称
    var name: String? = null

    //角色昵称
    var codeName: String? = null

    //车辆id
    var carId: String? = null

    //缺卡次数
    var count: String? = null

    //今日第一条运输记录（不是缺卡返回空值）
    var taskId: String? = null
}