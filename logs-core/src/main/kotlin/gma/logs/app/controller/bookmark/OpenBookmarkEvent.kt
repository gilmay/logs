package gma.logs.app.controller.bookmark

import gma.logs.app.model.bookmark.LogsBookmark
import tornadofx.*

class OpenBookmarkEvent(val bookmark: LogsBookmark) : FXEvent()
