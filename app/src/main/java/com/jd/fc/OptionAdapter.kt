package com.jd.fc

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jd.fc.databinding.EmptyLayoutBinding
import com.jd.fc.databinding.ViewAnserOptionsBinding
import com.jd.fc.model.CountriesItem


class OptionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var setCorrectAnswerId: Int? = null
    var showAnswer: Boolean = false
    lateinit var mContext: Activity
    private var mList: List<CountriesItem>? = null


    fun setData(activity: Activity, data: List<CountriesItem>) {
        mList = data
        mContext = activity
    }


    internal inner class AnswerOptionViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val binding: ViewAnserOptionsBinding? = DataBindingUtil.bind(itemView!!)

    }

    internal inner class EmptyHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val binding: EmptyLayoutBinding? = DataBindingUtil.bind(itemView!!)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {

            VIEW_TYPES.ANSWER_OPTIONS -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_anser_options, parent, false)
                AnswerOptionViewHolder(view)
            }

            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.empty_layout, parent, false)
                EmptyHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPES.ANSWER_OPTIONS) {
            val tabHolder = holder as AnswerOptionViewHolder
            var mData = mList?.get(position)

            tabHolder.binding?.tvAnsOpt?.text = mData?.countryName

            if(mData?.isSelected == true){
                tabHolder.binding?.cvAnsOption?.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.colorPrimary))
                tabHolder.binding?.tvAnsOpt?.setTextColor(ContextCompat.getColor(mContext,R.color.white))
            }
            else{
                tabHolder.binding?.cvAnsOption?.setCardBackgroundColor(ContextCompat.getColor(mContext,R.color.white))
                tabHolder.binding?.tvAnsOpt?.setTextColor(ContextCompat.getColor(mContext,R.color.greyTextColor))
            }


            if(showAnswer){
                tabHolder.binding?.cvAnsOption?.isClickable = false
                if(setCorrectAnswerId != null && setCorrectAnswerId == mData?.id){
                    tabHolder.binding?.tvAnsRes?.text= "CORRECT"
                    tabHolder.binding?.tvAnsRes?.setTextColor(ContextCompat.getColor(mContext,R.color.greenTextColor))
                }

                if(mData?.isSelected == true){
                    if(setCorrectAnswerId != null && setCorrectAnswerId != mData?.id){
                        tabHolder.binding?.tvAnsRes?.text= "WRONG"
                        tabHolder.binding?.tvAnsRes?.setTextColor(ContextCompat.getColor(mContext,R.color.redTextColor))
                    }
                }
            }
            else{
                tabHolder.binding?.cvAnsOption?.isClickable = true
            }


            tabHolder.binding?.cvAnsOption?.setOnClickListener {
                if(!showAnswer){
                mList?.forEachIndexed { index, _ ->

                    if(position == index){
                        mList!![index].isSelected =true
                    }
                    else{
                        mList!![index].isSelected =false
                    }
                }
                notifyDataSetChanged()
            }}




        }
    }


    override fun getItemCount(): Int {
        return mList?.size!!
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPES.ANSWER_OPTIONS
    }


    object VIEW_TYPES {
        const val ANSWER_OPTIONS = 0

    }


}