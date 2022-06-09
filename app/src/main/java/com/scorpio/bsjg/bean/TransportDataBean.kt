package com.scorpio.bsjg.bean

class TransportDataBean {
    // 主键id
    var id: String? = null

    // 项目名称
    var prname: String? = null

    // 项目地址
    var prsiteName: String? = null

    // 项目点位经纬度
    var prCoordinate: Coordinate? = null

    // 项目打卡经纬度
    var startCoordinate: Coordinate? = null


    class Coordinate {
        // 经度
        var x: String? = null

        // 纬度
        var y: String? = null

    }

    // 项目打卡时间
    var startTime: String? = null

    // 倒土点名称
    var poname: String? = null

    // 项目打卡范围
    var prExtent: String? = null

    // 倒土点打卡范围
    var poExtent: String? = null

    // 倒土点地址
    var positeName: String? = null

    // 倒土点经纬度
    var poCoordinate: Coordinate? = null

    // 倒土点打卡经纬度
    var endCoordinate: Coordinate? = null

    // 倒土点打卡时间
    var endTime: String? = null

    // 0/未打卡，1/两个点已打卡
    var deleted: String? = null

    // 创建时间
    var createTime: String? = null
}
