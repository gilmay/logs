package gma.logs.app.controller.openconfig

import gma.logs.app.model.config.LogsConfiguration
import tornadofx.*

class OpenConfigurationEvent(val configuration: LogsConfiguration) : FXEvent()
