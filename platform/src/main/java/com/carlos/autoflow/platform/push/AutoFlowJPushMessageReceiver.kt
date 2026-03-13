package com.carlos.autoflow.platform.push

import android.content.Context
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver

class AutoFlowJPushMessageReceiver : JPushMessageReceiver() {
    override fun onRegister(context: Context, registrationId: String) {
        Log.d("JPushReceiver", "onRegister registrationId=$registrationId")
    }

    override fun onConnected(context: Context, isConnected: Boolean) {
        val registrationId = JPushInterface.getRegistrationID(context)
        Log.d(
            "JPushReceiver",
            "onConnected isConnected=$isConnected registrationId=$registrationId"
        )
    }

    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Log.d("JPushReceiver", "onMessage title=${customMessage.title} message=${customMessage.message} extra=${customMessage.extra}")
    }

    override fun onNotifyMessageArrived(context: Context, notificationMessage: NotificationMessage) {
        Log.d(
            "JPushReceiver",
            "onNotifyMessageArrived title=${notificationMessage.notificationTitle} content=${notificationMessage.notificationContent} extras=${notificationMessage.notificationExtras}"
        )
    }

    override fun onNotifyMessageOpened(context: Context, notificationMessage: NotificationMessage) {
        Log.d(
            "JPushReceiver",
            "onNotifyMessageOpened title=${notificationMessage.notificationTitle} content=${notificationMessage.notificationContent} extras=${notificationMessage.notificationExtras}"
        )
    }
}
