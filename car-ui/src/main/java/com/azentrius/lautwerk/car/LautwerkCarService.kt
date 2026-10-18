// SPDX-License-Identifier: Apache-2.0
package com.azentrius.lautwerk.car

import android.util.Log
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class LautwerkCarService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        Log.i(TAG, "car.createHostValidator (signature allowlist enabled)")
        // Keep host identity checks enabled, including in the DHU debug build.
        // This is separate from Android Auto deciding whether this app is eligible.
        return HostValidator.Builder(this)
            .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample)
            .build()
    }

    override fun onCreateSession(): Session {
        Log.i(TAG, "car.onCreateSession")
        return LautwerkSession()
    }

    companion object { const val TAG = "Lautwerk" }
}
