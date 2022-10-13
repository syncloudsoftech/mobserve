package com.syncloudsoft.mobserve.ui

import androidx.lifecycle.ViewModel
import com.syncloudsoft.mobserve.MainApplication
import com.syncloudsoft.mobserve.data.ForwardingRuleDatabase

class ForwardingRulesListViewModel : ViewModel() {

    val forwardingRules = MainApplication.CONTAINER[ForwardingRuleDatabase::class.java]
        .forwardingRuleDao()
        .getAllAsLiveData()
}
