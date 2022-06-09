package com.scorpio.bsjg.adapter

import android.view.View
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.moufans.lib_base.base.adapter.BaseAdapter
import com.scorpio.bsjg.R
import com.scorpio.bsjg.bean.RoleDataBean
import com.scorpio.bsjg.bean.SelCardDataBean
import com.scorpio.bsjg.databinding.ItemRoleListBinding
import com.scorpio.bsjg.databinding.ItemTaskRecordListBinding

class TaskRecordListAdapter(layoutResId: Int = R.layout.item_task_record_list, data: MutableList<SelCardDataBean.RecordsBean> = mutableListOf()) : BaseAdapter<SelCardDataBean.RecordsBean, ItemTaskRecordListBinding>(layoutResId, data) {

    override fun convert(holder: BaseDataBindingHolder<ItemTaskRecordListBinding>, position: Int, item: SelCardDataBean.RecordsBean) {
        holder.dataBinding?.apply {
            // 时间
            mTimeTextView.text = item.createTime
            // 小计
            mSubtotalTextView.text = "小计：¥${item.money}"
            mAddressTextView.text = "起始项目： ${item.prname}       ——>           终点：${item.poname}"
            mDesOneTextView.visibility = if (item.deleted == "0") View.VISIBLE else View.GONE
            mApplyTextView.visibility = if (item.deleted == "0") View.VISIBLE else View.GONE
            mDesTwoTextView.visibility = if (item.deleted == "0") View.GONE else View.VISIBLE
        }
    }
}