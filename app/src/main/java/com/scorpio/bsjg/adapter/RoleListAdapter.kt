package com.scorpio.bsjg.adapter

import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.moufans.lib_base.base.adapter.BaseAdapter
import com.scorpio.bsjg.R
import com.scorpio.bsjg.bean.RoleDataBean
import com.scorpio.bsjg.databinding.ItemRoleListBinding

class RoleListAdapter(layoutResId: Int = R.layout.item_role_list, data: MutableList<RoleDataBean> = mutableListOf()) : BaseAdapter<RoleDataBean, ItemRoleListBinding>(layoutResId, data) {

    override fun convert(holder: BaseDataBindingHolder<ItemRoleListBinding>, position: Int, item: RoleDataBean) {
        holder.dataBinding?.apply {
            mRTextView.text = item.name
        }
    }
}