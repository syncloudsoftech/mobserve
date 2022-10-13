package com.syncloudsoft.mobserve.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.syncloudsoft.mobserve.R
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.databinding.ItemForwardingRuleBinding

class ForwardingRuleAdapter(
    private val onClickListener: (ForwardingRule) -> Unit
) : ListAdapter<ForwardingRule, ForwardingRuleViewHolder>(diffCallback) {

    fun getItemAt(position: Int) : ForwardingRule = getItem(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForwardingRuleViewHolder {
        val binding = ItemForwardingRuleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ForwardingRuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForwardingRuleViewHolder, position: Int) {
        val forwardingRule = getItem(position)
        holder.binding.iconImage.setImageResource(when (forwardingRule.direction) {
            ForwardingRuleViewModel.DIRECTION_INCOMING -> R.drawable.ic_baseline_arrow_right_alt_24
            ForwardingRuleViewModel.DIRECTION_OUTGOING -> R.drawable.ic_baseline_arrow_left_alt_24
            else -> R.drawable.ic_baseline_sync_alt_24
        })
        holder.binding.nameText.text = forwardingRule.name
        holder.binding.webhookUrlText.text = forwardingRule.webhookUrl
        holder.itemView.setOnClickListener { onClickListener(forwardingRule) }
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<ForwardingRule>() {

    override fun areItemsTheSame(oldItem: ForwardingRule, newItem: ForwardingRule) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ForwardingRule, newItem: ForwardingRule) =
        oldItem.name == newItem.name
                && oldItem.direction == newItem.direction
                && oldItem.participantPattern == newItem.participantPattern
                && oldItem.contentPattern == newItem.contentPattern
                && oldItem.webhookUrl == newItem.webhookUrl
}
