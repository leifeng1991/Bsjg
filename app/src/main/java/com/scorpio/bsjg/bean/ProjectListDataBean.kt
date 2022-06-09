package com.scorpio.bsjg.bean

class ProjectListDataBean {
    // 项目id
    var id: String? = null

    // 项目名称
    var name: String? = null

    // 位置
    var siteName: String? = null

    // 创建时间
    var createTime: String? = null

    // 编号
    var sort: String? = null

    // 打卡范围
    var extent: String? = null
    var coordinate: Coordinate? = null

    class Coordinate {
        // 经度
        var x: String? = null

        // 纬度
        var y: String? = null
    }

}