package com.carlos.autoflow.foundation.network

object AppHosts {
    const val API = "http://xbdcc.cn"
    const val WEB = "https://autoflow.xbdcc.cn"
}

object ApiRoutes {
    const val VERSION_INFO = "${AppHosts.WEB}/version"
    const val AD_CONFIG = "${AppHosts.WEB}/ad-config"
    const val CHECKIN_CONFIG = "${AppHosts.WEB}/checkin-config.json"
}

object WebRoutes {
    const val PRIVACY_POLICY = "https://autoflow.xbdcc.cn/AutoFlow-privacy-policy.html"
    const val USER_AGREEMENT = "https://autoflow.xbdcc.cn/AutoFlow-user-agreement.html"
    const val HELP = "${AppHosts.WEB}/help.html"
}
