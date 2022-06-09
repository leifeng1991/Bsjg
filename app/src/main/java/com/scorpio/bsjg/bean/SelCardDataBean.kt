package com.scorpio.bsjg.bean

class SelCardDataBean {
    // 总记录数
    var total: String? = null

    // 当前页
    var current: String? = null

    // 页码大小
    var size: String? = null
    var records: List<RecordsBean>? = null

    class RecordsBean {
        // 主键id
        var id: String? = null

        // 项目名称
        var prname: String? = null

        // 项目点位名称
        var prsiteName: String? = null

        // 倒土点名称
        var poname: String? = null

        // 倒土点点位名称
        var positeName: String? = null

        // 0/未打卡，1/已打卡，2/申请中
        var deleted: String? = null

        // 创建时间
        var createTime: String? = null

        // 金额
        var money: String? = null

    }
}