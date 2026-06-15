package com.example.data.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private const val TAG = "SocketManager"
    private var socket: Socket? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Live state flows for real-time events to handle in the UI / view model
    val globalChatMessages = MutableStateFlow<JSONObject?>(null)
    val privateChatMessages = MutableStateFlow<JSONObject?>(null)
    val onlineFriends = MutableStateFlow<JSONObject?>(null)
    val pvpMatches = MutableStateFlow<JSONObject?>(null)
    val pvpInvites = MutableStateFlow<JSONObject?>(null)
    val rankingUpdates = MutableStateFlow<JSONObject?>(null)
    val notifications = MutableStateFlow<JSONObject?>(null)
    val friendRequests = MutableStateFlow<JSONObject?>(null)
    val friendsAccepted = MutableStateFlow<JSONObject?>(null)

    enum class ConnectionState {
        CONNECTED, DISCONNECTED, CONNECTING, ERROR
    }

    fun connect(url: String, token: String? = null) {
        if (socket != null && socket!!.connected()) {
            Log.d(TAG, "Already connected to Socket.IO")
            return
        }

        try {
            _connectionState.value = ConnectionState.CONNECTING
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = Int.MAX_VALUE
                timeout = 10000
                if (token != null) {
                    query = "token=$token"
                }
            }

            socket = IO.socket(url, options)

            socket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d(TAG, "Connected to Socket.IO")
                _connectionState.value = ConnectionState.CONNECTED
                
                // Automatically join-room when connected or reconnected
                token?.let {
                    joinRoom(it)
                }
            })

            socket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
                Log.d(TAG, "Disconnected from Socket.IO")
                _connectionState.value = ConnectionState.DISCONNECTED
            })

            socket?.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "Unknown error"
                Log.e(TAG, "Connect error: $error")
                _connectionState.value = ConnectionState.ERROR
            })

            // Event mappings
            socket?.on("chat-message", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        globalChatMessages.value = data
                    }
                }
            })

            socket?.on("private-message", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        privateChatMessages.value = data
                    }
                }
            })

            socket?.on("user-online", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        onlineFriends.value = data
                    }
                }
            })

            socket?.on("match-found", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        pvpMatches.value = data
                    }
                }
            })

            socket?.on("pvp-invite", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        pvpInvites.value = data
                    }
                }
            })

            socket?.on("ranking-update", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        rankingUpdates.value = data
                    }
                }
            })

            socket?.on("notification", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        notifications.value = data
                    }
                }
            })

            socket?.on("friend-request", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        friendRequests.value = data
                    }
                }
            })

            socket?.on("friend-accepted", Emitter.Listener { args ->
                if (args.isNotEmpty()) {
                    val data = args[0]
                    if (data is JSONObject) {
                        friendsAccepted.value = data
                    }
                }
            })

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket.IO URI Exception", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }

    fun disconnect() {
        socket?.disconnect()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun emitEvent(eventName: String, data: Any) {
        socket?.emit(eventName, data)
    }

    private fun joinRoom(token: String) {
        val data = JSONObject().apply {
            put("token", token)
        }
        emitEvent("join-room", data)
    }
}
