package com.syncloudsoft.mobserve.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syncloudsoft.mobserve.data.ForwardingRule

class ForwardingRuleViewModelFactory(
    private var forwardingRule: ForwardingRule?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ForwardingRule::class.java)
            .newInstance(forwardingRule)
    }
}
