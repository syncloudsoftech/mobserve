package com.syncloudsoft.mobserve

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.syncloudsoft.mobserve.databinding.ActivityMainBinding
import com.syncloudsoft.mobserve.spy.PhoneCallOrMessageService
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setUpNavigation()
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_SMS
        )
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            startService()
        } else {
            EasyPermissions.requestPermissions(this,
                getString(R.string.sms_permission_rationale), REQUEST_CODE_CALL_SMS_PERMISSION,
                *permissions);
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_source -> {
                openLinkToSourceCode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun openLinkToSourceCode() {
        val intent = Intent(Intent.ACTION_VIEW, SOURCE_CODE_LINK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(applicationContext, intent, null)
    }

    private fun setUpNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.fab.setOnClickListener {
            navController.navigate(R.id.action_RulesList_to_New_Rule)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.fab.isVisible = destination.id != R.id.ForwardingRuleFragment
        }
    }

    @AfterPermissionGranted(REQUEST_CODE_CALL_SMS_PERMISSION)
    private fun startService() {
        PhoneCallOrMessageService.startSelf(applicationContext)
    }

    companion object {

        private const val REQUEST_CODE_CALL_SMS_PERMISSION = 60600
        private val SOURCE_CODE_LINK = Uri.parse("https://github.com/syncloudsoftech/mobserve")
    }
}
