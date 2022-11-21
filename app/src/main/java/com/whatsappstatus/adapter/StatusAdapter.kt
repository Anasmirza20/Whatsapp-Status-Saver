package com.whatsappstatus.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whatsapp.reelsfiz.databinding.StatusImageViewBinding
import com.whatsappstatus.models.StatusData
import com.whatsappstatus.util.CommonMethods
import com.whatsappstatus.util.Extensions.setOnClickListenerWithDebounce

class StatusAdapter(private val callback: StatusCallbacks, private val fromDownloads: Boolean = false) : ListAdapter<StatusData, StatusAdapter.WalletViewHolder>(DiffCallback()) {
    inner class WalletViewHolder(private val binding: StatusImageViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListenerWithDebounce {
                    callback.onClick(absoluteAdapterPosition)
                }
                if (!fromDownloads)
                    download.setOnClickListenerWithDebounce {
                        callback.onDownloadClick(absoluteAdapterPosition)
                    }
            }
        }

        fun bind(data: StatusData?) {
            if (fromDownloads)
                binding.download.isVisible = false
            CommonMethods.loadImage(binding.root.context, data?.uri.toString(), binding.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WalletViewHolder(
        StatusImageViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<StatusData>() {
        override fun areItemsTheSame(oldItem: StatusData, newItem: StatusData) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: StatusData, newItem: StatusData) = oldItem.equals(newItem)
    }
}

interface StatusCallbacks {
    fun onClick(position: Int)
    fun onDownloadClick(position: Int)
}