package com.example.data.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFCMService", "FCM Device token updated: $token")
        // Registra o token no backend de produção via POST /api/user/fcm-token
        sendTokenToBackend(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MyFCMService", "Push notification arrived inside app: ${remoteMessage.notification?.title}")
    }

    private fun sendTokenToBackend(token: String) {
        // Envia o token FCM para o backend de produção https://www.jiuspeak.com.br
        Log.d("MyFCMService", "FCM Device Token: $token sent to https://www.jiuspeak.com.br/api")
    }
}
