package com.scorpio.bsjg.adapter

import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.moufans.lib_base.base.adapter.BaseAdapter
import com.scorpio.bsjg.R
import com.scorpio.bsjg.bean.ProjectListDataBean
import com.scorpio.bsjg.bean.RoleDataBean
import com.scorpio.bsjg.databinding.ItemProjectAddressListBinding
import com.scorpio.bsjg.databinding.ItemRoleListBinding

class ProjectAddressListAdapter(layoutResId: Int = R.layout.item_project_address_list, data: MutableList<ProjectListDataBean> = mutableListOf()) : BaseAdapter<ProjectListDataBean, ItemProjectAddressListBinding>(layoutResId, data) {
    var checkedPosition = -1

    override fun convert(holder: BaseDataBindingHolder<ItemProjectAddressListBinding>, position: Int, item: ProjectListDataBean) {
        holder.dataBinding?.apply {
            mAddressTextView.isSelected = position == checkedPosition
            mAddressTextView.text = "${item.name}:${item.siteName}"
        }
    }
}