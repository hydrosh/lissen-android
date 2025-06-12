package org.grakovne.lissen.automotive

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class LissenCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator {
        // Per il debugging, usa ALLOW_ALL_HOSTS_VALIDATOR
        // In produzione, dovresti usare un validator più restrittivo
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    override fun onCreateSession(): Session {
        return LissenCarAppSession()
    }
}
