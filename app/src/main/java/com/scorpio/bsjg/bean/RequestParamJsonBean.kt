package com.scorpio.bsjg.bean

class RequestParamJsonBean {
    var phone: String? = null
    var username: String? = null
    var password: String? = null
    var code: String? = null
    var mac: String? = null
    var companyId: String? = null
    var identityCode: String? = null

    // id(为空时添加，不为空根据id修改)
    var id: String? = null
    var name: String? = null
    var siteName: String? = null
    var coordinate: Coordinate? = null

    class Coordinate {
        // 经度
        var x: String? = null

        // 纬度
        var y: String? = null

    }

    var startCoordinate: Coordinate? = null
    var endCoordinate: Coordinate? = null

    var startId: String? = null
    var endId: String? = null
    var money: String? = null
    var extent: String? = null

    var deleted: String? = null

    var quality: String? = null
    var price: String? = null
    var number: String? = null
    var carId: String? = null


    var pattern: String? = null
    var type: String? = null


    var pid: String? = null
    var period: String? = null
    var clas: String? = null
    var costTypeId: String? = null
    var unitPrice: String? = null
    var sumPrice: String? = null
    var aid: String? = null
    var aname: String? = null
    var content: String? = null

    var picUrl: String? = null

    var cid: String? = null

    var carPlate: String? = null

    var createTime: String? = null

}