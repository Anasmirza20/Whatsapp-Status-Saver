package com.whatsappstatus.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.whatsapp.reelsfiz.databinding.ShimmerStatusViewBinding

class ShimmerAdapter(private val list: List<Int>) : RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {
    inner class ShimmerViewHolder(val binding: ShimmerStatusViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ShimmerViewHolder(
        ShimmerStatusViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) =
        holder.binding.image.startShimmer()
}