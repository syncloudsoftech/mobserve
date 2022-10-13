package com.syncloudsoft.mobserve.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.RIGHT
import androidx.recyclerview.widget.RecyclerView
import com.syncloudsoft.mobserve.MainApplication
import com.syncloudsoft.mobserve.R
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.data.ForwardingRuleDatabase
import com.syncloudsoft.mobserve.databinding.FragmentForwardingRulesListBinding
import com.google.android.material.snackbar.Snackbar

class ForwardingRulesListFragment : Fragment() {

    private lateinit var adapter: ForwardingRuleAdapter

    private lateinit var binding: FragmentForwardingRulesListBinding

    private val itemTouchCallback: ItemTouchHelper.Callback =
        object : ItemTouchHelper.SimpleCallback(0, LEFT or RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = adapter.getItemAt(viewHolder.adapterPosition)
                deleteForwardingRule(note)
            }
        }

    private val viewModel: ForwardingRulesListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentForwardingRulesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.forwardingRulesList.setHasFixedSize(true)
        adapter = ForwardingRuleAdapter { editForwardingRule(it) }
        binding.forwardingRulesList.adapter = adapter
        ItemTouchHelper(itemTouchCallback)
            .attachToRecyclerView(binding.forwardingRulesList)
        viewModel.forwardingRules.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    private fun deleteForwardingRule(forwardingRule: ForwardingRule) {
        val dao = MainApplication.CONTAINER[ForwardingRuleDatabase::class.java]
            .forwardingRuleDao()
        dao.delete(forwardingRule)
        Snackbar.make(requireView(), getString(R.string.rule_deleted_message, forwardingRule.name), Snackbar.LENGTH_LONG)
            .setAction(R.string.action_undo) {
                forwardingRule.id = null
                dao.insertAll(forwardingRule)
            }
            .show()
    }

    private fun editForwardingRule(forwardingRule: ForwardingRule) {
        findNavController().navigate(
            R.id.action_RulesList_to_Existing_Rule,
            bundleOf("forwarding_rule_id" to forwardingRule.id))
    }
}
