package com.example.villactiva.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class SimpleGenericRecyclerAdapter<T: Any, VB: ViewBinding>(
    private val dataSet: List<T>,
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB,
    private val bindingInterface: GenericSimpleRecyclerBindingInterface<T, VB>
) : RecyclerView.Adapter<SimpleGenericRecyclerAdapter.ViewHolder<VB>>() {

    class ViewHolder<VB: ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root) {
        fun <T: Any> bind(
            item: T,
            bindingInterface: GenericSimpleRecyclerBindingInterface<T, VB>,
            position: Int,
            adapter: SimpleGenericRecyclerAdapter<T, VB>
        ) = bindingInterface.bindData(item, binding, position, adapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<VB> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = bindingInflater(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<VB>, position: Int) {
        val item = dataSet[position]
        holder.bind(item, bindingInterface, position, this)
    }

    override fun getItemCount(): Int = dataSet.size
}

interface GenericSimpleRecyclerBindingInterface<T: Any, VB: ViewBinding> {
    fun bindData(item: T, binding: VB, position: Int, adapter: SimpleGenericRecyclerAdapter<T, VB>)
}