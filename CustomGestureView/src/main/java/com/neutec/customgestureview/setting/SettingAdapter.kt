package com.neutec.customgestureview.setting

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neutec.customgestureview.R
import com.neutec.customgestureview.utility.PatternLockUtils

class SettingAdapter(context: Context) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {
    var userIdSet: MutableList<String> = PatternLockUtils.getUserIdSet(context)
    var userInfoList: MutableList<UserData> = mutableListOf()
    var checkedUserIdList: ArrayList<String?> = arrayListOf()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //把layout檔的元件們拉進來，指派給當地變數
//        val photo: ImageView = itemView.findViewById(R.id.iv_photo)
        val name: TextView = itemView.findViewById(R.id.tv_name)
        val photoName: TextView = itemView.findViewById(R.id.tv_photo)

        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val switch: Switch = itemView.findViewById(R.id.custom_switch)

        @SuppressLint("SetTextI18n")
        fun bind(item: UserData?) {
            name.text = getName(item?.firstName, item?.lastName)

            switch.apply {
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        addValueToMutableSet(item?.id.toString())
                    } else {
                        removeValueToMutableSet(item?.id.toString())
                    }
                }

                isChecked = userIdSet.contains(
                    item?.id.toString()
                ) == true

                if (isChecked) {
                    addValueToMutableSet(item?.id.toString())
                }
            }

            photoName.apply {
                text = getNameShort(item?.firstName?.first(), item?.lastName?.first())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val example = inflater.inflate(R.layout.item_setting, parent, false)
        return ViewHolder(example)

    }

    override fun getItemCount() = userInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userInfoList[position])
    }

    //更新資料用
    fun updateList(list: MutableList<UserData>) {
        if (list.size > 0)
            userInfoList = list
    }

//    fun getCheckedList(): MutableList<String?> {
//        return checkedUserIdList
//    }

    private fun addValueToMutableSet(value: String) {
        if (!checkedUserIdList.contains(value)) {
            checkedUserIdList.add(value)
        }
    }

    private fun removeValueToMutableSet(value: String) {
        if (checkedUserIdList.contains(value)) {
            checkedUserIdList.remove(value)
        }
    }

    private fun getName(firstName: String?, lastName: String?): String {
        return when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null && lastName == null -> "$firstName"
            firstName == null && lastName != null -> "$lastName"
            else -> ""
        }
    }

    private fun getNameShort(firstName: Char?, lastName: Char?): String {
        return when {
            firstName != null && lastName != null -> "$firstName$lastName"
            firstName != null && lastName == null -> "$firstName"
            firstName == null && lastName != null -> "$lastName"
            else -> ""
        }
    }
}