package dev.jtsalva.cloudmare

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import dev.jtsalva.cloudmare.api.zone.Zone
import dev.jtsalva.cloudmare.api.zonesettings.ZoneSetting
import dev.jtsalva.cloudmare.api.zonesettings.ZoneSettingRequest
import dev.jtsalva.cloudmare.databinding.ActivitySslBindingImpl
import dev.jtsalva.cloudmare.viewmodel.SSLViewModel
import kotlinx.android.synthetic.main.activity_ssl.*

class SSLActivity : CloudMareActivity(), SwipeRefreshable {

    private lateinit var zone: Zone

    private lateinit var binding: ActivitySslBindingImpl

    private lateinit var viewModel: SSLViewModel

    val sslModeAdapter by lazy {
        ArrayAdapter.createFromResource(
            this,
            R.array.entries_ssl_modes,
            R.layout.spinner_item
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        zone = intent.getParcelableExtra("zone")!!

        viewModel = SSLViewModel(this, zone)
        binding = setLayoutBinding(R.layout.activity_ssl)

        setToolbarTitle("${zone.name} | SSL / TLS")
    }

    override fun onStart() {
        super.onStart()

        render()
    }

    override fun onSwipeRefresh() {
        super.onSwipeRefresh()

        viewModel.isFinishedInitializing = false
    }

    override fun render() = launch {
        val response = ZoneSettingRequest(this).list(zone.id)
        if (response.failure || response.result == null)
            dialog.error(message = response.firstErrorMessage, onAcknowledge = ::onStart)

        else response.result.let { settings ->
            viewModel.sslMode = settings.valueAsString(ZoneSetting.ID_SSL)

            sslModeAdapter.let { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

                ssl_mode_spinner.apply {
                    setAdapter(adapter)

                    val currentSslMode = viewModel.run { sslModeTranslator.getReadable(sslMode) }
                    setSelection(adapter.getPosition(currentSslMode))

                    onItemSelectedListener = viewModel
                }
            }

            viewModel.apply {
                alwaysUseHttps =
                    settings.valueAsBoolean(ZoneSetting.ID_ALWAYS_USE_HTTPS)
                opportunisticEncryption =
                    settings.valueAsBoolean(ZoneSetting.ID_OPPORTUNISTIC_ENCRYPTION)
                opportunisticOnion =
                    settings.valueAsBoolean(ZoneSetting.ID_OPPORTUNISTIC_ONION)
                automaticHttpsRewrites =
                    settings.valueAsBoolean(ZoneSetting.ID_AUTOMATIC_HTTPS_REWRITES)
            }

            binding.viewModel = viewModel

            viewModel.isFinishedInitializing = true
            ssl_modes_view_group.visibility = View.VISIBLE
        }

        showProgressBar = false

        swipeRefreshLayout.isRefreshing = false
    }
}
