package dev.jtsalva.cloudmare

import android.os.Bundle
import dev.jtsalva.cloudmare.api.user.UserRequest
import kotlinx.android.synthetic.main.activity_app_settings.*

class AppSettingsActivity : CloudMareActivity() {

    override val TAG = "AppSettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLayout(R.layout.activity_app_settings)
        setToolbarTitle(R.string.title_app_settings)

        Auth.load(this)
        if (Auth.isSet) {
            email_input.setText(Auth.email)
            api_key_input.setText(Auth.apiKey)
        }

        done_button.setOnClickListener {
            Auth.email = email_input.text.toString()
            Auth.apiKey = api_key_input.text.toString()
            Auth.save(this)

            launch {
                val response = UserRequest(this@AppSettingsActivity).getDetails()
                if (response.success) finish()
                else longToast(response.firstErrorMessage)
            }

        }
    }

}