package com.moufans.lib_base.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.moufans.lib_base.R
import java.io.File


/**
 * Image加载的工具类
 */
@Suppress("unused")
object ImageLoader {

    /**
     * 设置图片 - imgUrl
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     * [roundingRadius] 圆角半径，单位dp，0：没有圆角，-1:圆形，其它：设置圆角
     */
    @JvmStatic
    @JvmOverloads
    fun setImageRaw(imgUrl: String?, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f, isSetCenterCrop: Boolean = true, transformation: Transformation<Bitmap>? = null, requestListener: RequestListener<Drawable>? = null) {
        setImageAllRaw(imgUrl, imageView, defaultImageResId, roundingRadius, isSetCenterCrop, transformation, requestListener)
    }

    /**
     * 设置图片 - Uri
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     * [roundingRadius] 圆角半径，单位dp，0：没有圆角，-1:圆形，其它：设置圆角
     */
    @JvmStatic
    @JvmOverloads
    fun setImageRaw(uri: Uri?, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f, isSetCenterCrop: Boolean = true, transformation: Transformation<Bitmap>? = null, requestListener: RequestListener<Drawable>? = null) {
        setImageAllRaw(uri, imageView, defaultImageResId, roundingRadius, isSetCenterCrop, transformation, requestListener)
    }

    /**
     * 设置图片 - File
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     * [roundingRadius] 圆角半径，单位dp，0：没有圆角，-1:圆形，其它：设置圆角
     */
    @JvmStatic
    @JvmOverloads
    fun setImageRaw(file: File?, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f, isSetCenterCrop: Boolean = true, transformation: Transformation<Bitmap>? = null, requestListener: RequestListener<Drawable>? = null) {
        setImageAllRaw(file, imageView, defaultImageResId, roundingRadius, isSetCenterCrop, transformation, requestListener)
    }

    /**
     * 设置图片 - resId
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     * [roundingRadius] 圆角半径，单位dp，0：没有圆角，-1:圆形，其它：设置圆角
     */
    @JvmStatic
    @JvmOverloads
    fun setImageRaw(resId: Int, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f, isSetCenterCrop: Boolean = true, transformation: Transformation<Bitmap>? = null, requestListener: RequestListener<Drawable>? = null) {
        setImageAllRaw(resId, imageView, defaultImageResId, roundingRadius, isSetCenterCrop, transformation, requestListener)
    }

    /**
     * 设置图片 - Bitmap
     */
    @JvmStatic
    fun setImageRaw(bitmap: Bitmap, requestOptions: RequestOptions, imageView: ImageView) {
        Glide.with(imageView.context).load(bitmap).apply(requestOptions).into(imageView)
    }

    /**
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     * [roundingRadius] 圆角半径，单位dp，0：没有圆角，-1:圆形，其它：设置圆角
     */
    @JvmStatic
    @JvmOverloads
    fun setImage(imgUrl: String?, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f, isSetCenterCrop: Boolean = true, transformation: Transformation<Bitmap>? = null, requestListener: RequestListener<Drawable>? = null) {
        // 设置图片
        setImageAllRaw(imgUrl, imageView, defaultImageResId, roundingRadius, isSetCenterCrop, transformation, requestListener)
    }


    /**
     * 下载大图并设置图片高度--下载完图片后根据图片宽高比例，设置image的高度
     */
    @JvmStatic
    fun setImageBigAndSetViewHeight(imgUrl: String?, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f) {
        // 不能设置[isSetCenterCrop]为true，图片显示会有问题
        // 设置图片
        setImageAllRaw(imgUrl, imageView, defaultImageResId, roundingRadius, false, requestListener = object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                return false// 是否自己处理不加载到ImageView
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                // resource :BitmapDrawable
                // model :图片地址
                // target :ImageView的Target
                // dataSource :数据的来源
                // isFirstResource :是否是第一次
                // return true// 是否自己处理不加载到ImageView
                if (resource != null && resource.intrinsicWidth > 0 && resource.intrinsicHeight > 0) {
                    // 图片没有问题
                    if (imageView.width > 0) {
                        // 目标View没问题，根据图片宽高比例设置imageView的高
                        // 设置控件高
                        val layoutParams = imageView.layoutParams
                        layoutParams.height = (resource.intrinsicHeight * imageView.width / resource.intrinsicWidth.toFloat()).toInt()
                        imageView.layoutParams = layoutParams
                        // 设置控件内容
                        imageView.setImageDrawable(resource)
                        return true// 已经处理
                    }
                }
                return false// 其它不处理
            }
        })
    }

    /**
     * 设置图片 - 支持所有类型
     * [any] 图片地址等
     * [imageView] 图片控件
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     * [roundingRadius] 圆角半径，单位dp，0：没有圆角，-1:圆形，其它：设置圆角
     * [isSetCenterCrop] 是否设置CenterCrop，默认为true
     * [transformation] 是否transformation转换，默认为null不转换
     * [requestListener] 请求监听，默认为null
     */
    private fun setImageAllRaw(any: Any?, imageView: ImageView, defaultImageResId: Int = 0, roundingRadius: Float = 0f, isSetCenterCrop: Boolean = true, transformation: Transformation<Bitmap>? = null, requestListener: RequestListener<Drawable>? = null) {
        var mDefaultImageResId = defaultImageResId
        if (mDefaultImageResId == 0) {
            // 没设置默认图片，默认给设置一张
            mDefaultImageResId = R.mipmap.common_ic_default_image
        }
        // 设置缩放类型，但是缩放类型会影响Glide的图片加载的大小，所以设置监听时不设置缩放类型，让其自己处理
        if (isSetCenterCrop && imageView.scaleType == ImageView.ScaleType.FIT_CENTER) // imageView默认缩放类型
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP// 不指定则为centerCrop
        // 加载图片
        val context = imageView.context
        if (context != null) {// 防止为null报错
            if ((context is FragmentActivity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && context.isDestroyed)
                    || (context is Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && context.isDestroyed))
                return// 页面已销毁，不加载图片
            val requestBuilder = Glide.with(context).load(any)
            var requestOptions = RequestOptions()
            // 设置默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
            if (mDefaultImageResId != -1 && mDefaultImageResId != 0) {
                // 显示默认图片
                requestOptions = requestOptions
                        .placeholder(mDefaultImageResId)// 请求中展示
                        .error(mDefaultImageResId)// 失败时展示
                        .fallback(mDefaultImageResId)// url为null时展示
            }
            val transformationsList = ArrayList<Transformation<Bitmap>>()
            if (isSetCenterCrop) {
                // 设置CenterCrop
                transformationsList.add(CenterCrop())
            }
            if (roundingRadius == -1f) {
                // 圆形
                transformationsList.add(CircleCrop())
            } else if (roundingRadius > 0) {
                // 设置圆角
//                transformationsList.add(RoundedCorners(ComDeviceUtils.dip2px(context, roundingRadius)))
                transformationsList.add(RoundedCorners(roundingRadius.toInt()))
            }
            if (transformation != null) {
                // 转换不为null，设置转换
                transformationsList.add(transformation)
            }
            if (transformationsList.size > 0) {
                requestOptions = requestOptions.transforms(*transformationsList.toTypedArray())
            }
            // 监听不用判断可以设置为null
            requestBuilder.apply(requestOptions).listener(requestListener).into(imageView)
        }
    }

    /**
     * [defaultImageResId] 默认图片，-1：不显示，0：显示默认的，其它：显示[defaultImageResId]的
     */
    private fun setDefaultImage(imageView: ImageView, defaultImageResId: Int) {
        if (defaultImageResId != -1 && defaultImageResId != 0) {
            // 有默认图片，设置
            imageView.setImageResource(defaultImageResId)
        } else {
            // 没默认图片，不设置
            imageView.setImageDrawable(null)
        }
    }

    interface OnGetImageSignatureListener {
        fun onSuccess(sign: String)

        fun onError(errorCode: Int, errorMsg: String)
    }
}