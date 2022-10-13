package com.syncloudsoft.mobserve.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.syncloudsoft.mobserve.MainApplication
import com.syncloudsoft.mobserve.R
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.data.ForwardingRuleDatabase
import com.syncloudsoft.mobserve.databinding.FragmentForwardingRuleBinding
import com.syncloudsoft.mobserve.spy.PhoneCallOrMessageService
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class ForwardingRuleFragment : Fragment() {

    private lateinit var binding: FragmentForwardingRuleBinding

    private var forwardingRule: ForwardingRule? = null
    private var forwardingRuleId: Int = -1

    private lateinit var viewModel: ForwardingRuleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            forwardingRuleId = getInt("forwarding_rule_id", -1)
        }

        val factory = if (forwardingRuleId != -1) {
            val dao = MainApplication.CONTAINER[ForwardingRuleDatabase::class.java]
                .forwardingRuleDao()
            forwardingRule = dao.get(forwardingRuleId)
            ForwardingRuleViewModelFactory(forwardingRule)
        } else {
            ForwardingRuleViewModelFactory(null)
        }

        viewModel = ViewModelProvider(this, factory)[ForwardingRuleViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentForwardingRuleBinding.inflate(inflater, container, false)
        binding.model = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveButton.setOnClickListener {
            if (validateFields()) {
                saveForwardingRule()
            }
        }
    }

    private fun saveForwardingRule() {
        Timber.d("Saving forwarding rule.")
        val event = viewModel.event.value!!
        val name = viewModel.name.value!!
        val direction = viewModel.direction.value!!
        val participantPattern = viewModel.participantPattern.value
        val contentPattern = viewModel.contentPattern.value
        val webhookUrl = viewModel.webhookUrl.value!!
        val dao = MainApplication.CONTAINER[ForwardingRuleDatabase::class.java]
            .forwardingRuleDao()
        val rule = if (forwardingRule != null) {
            Timber.d("Updating existing note with ID %d", forwardingRuleId)
            forwardingRule!!.event = event
            forwardingRule!!.name = name
            forwardingRule!!.direction = direction
            forwardingRule!!.participantPattern = participantPattern
            forwardingRule!!.contentPattern = contentPattern
            forwardingRule!!.webhookUrl = webhookUrl
            dao.update(forwardingRule!!)
            forwardingRule!!
        } else {
            val rule =
                ForwardingRule(
                    event, name, direction, participantPattern, contentPattern, webhookUrl)
            dao.insertAll(rule)
            rule
        }

        Snackbar.make(requireView(), getString(R.string.rule_created_message, rule.name), Snackbar.LENGTH_LONG)
            .show()
        PhoneCallOrMessageService.startSelf(requireContext())
        findNavController().popBackStack()
    }

    private fun validateFields() : Boolean {
        Timber.d("Validating forwarding rule fields.")
        var ok = true
        val name = viewModel.name.value
        if (name.isNullOrEmpty()) {
            binding.nameField.error = getString(R.string.rule_name_error_required)
            if (ok) {
                binding.nameField.requestFocus()
            }

            ok = false
        } else {
            binding.nameField.error = null
        }

        val participantPattern = viewModel.participantPattern.value
        if (!participantPattern.isNullOrEmpty()) {
            try {
                Pattern.compile(participantPattern)
                binding.participantPatternField.error = null
            } catch (ignore: PatternSyntaxException) {
                binding.participantPatternField.error = getString(R.string.rule_regexp_error_invalid)
                if (ok) {
                    binding.participantPatternField.requestFocus()
                }

                ok = false
            }

        } else {
            binding.participantPatternField.error = null
        }

        val contentPattern = viewModel.contentPattern.value
        if (!contentPattern.isNullOrEmpty()) {
            try {
                Pattern.compile(contentPattern)
                binding.contentPatternField.error = null
            } catch (ignore: PatternSyntaxException) {
                binding.contentPatternField.error = getString(R.string.rule_regexp_error_invalid)
                if (ok) {
                    binding.contentPatternField.requestFocus()
                }

                ok = false
            }

        } else {
            binding.contentPatternField.error = null
        }

        val webhookUrl = viewModel.webhookUrl.value
        if (webhookUrl.isNullOrEmpty()) {
            binding.webhookUrlField.error = getString(R.string.rule_webhook_url_error_required)
            if (ok) {
                binding.webhookUrlField.requestFocus()
            }

            ok = false
        } else if (!webhookUrl.startsWith("http://") && !webhookUrl.startsWith("https://")) {
            binding.webhookUrlField.error = getString(R.string.rule_webhook_url_error_invalid)
            if (ok) {
                binding.webhookUrlField.requestFocus()
            }

            ok = false
        } else {
            binding.webhookUrlField.error = null
        }

        return ok
    }
}
