package com.zecqtwallet.wormhole

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.json
import io.javalin.Javalin
import io.javalin.websocket.WsSession
import java.util.concurrent.ConcurrentHashMap


private val usermap = ConcurrentHashMap<WsSession, String>()

// Allow maps to be bidirectional
fun <K, V> Map<K, V>.getKeys(value: V) : List<K> =
    entries.filter { it.value == value } .map { it.key }


fun main(args : Array<String>) {

    Javalin.create().apply {
        ws("/") { ws ->
            ws.onConnect { session ->
                println("Connected Session")
            }

            ws.onClose { session, status, message ->
                println("Closed session")
                usermap.remove(session)
            }

            ws.onMessage { session, message ->
                // Limit message size to 50kb of hex encoded text
                if (message.length > 2 * 50 * 1024) {
                    sendError(session, "Message too big")
                }

                println("Recieved $message")

                // Parse the message as json
                try {
                    val j = Parser.default().parse(StringBuilder(message)) as JsonObject

                    if (j.contains("register")) {
                        doRegister(session, j["register"].toString())
                        return@onMessage
                    }

                    if (j.contains("to")) {
                        val s = usermap.getKeys(j["to"].toString()).filter { it != session }
                        if (s.isEmpty()) {
                            // Not connected
                            sendError(session, "Peer is not connected")
                            return@onMessage
                        }

                        s[0].send(message)
                        return@onMessage
                    } else {
                        println("There was no 'to' in the message")
                        sendError(session,"Missing 'to' field")
                        return@onMessage
                    }
                } catch (e: Throwable) {
                    println("Exception: ${e.localizedMessage}")
                    session.close(1000, "Invalid json")
                }
            }
            
            ws.onError { session, t ->
                println("Something went wrong with session ${t.toString()}")
                usermap.remove(session)
            }
        }
    }.start(7070)

}

fun doRegister(session: WsSession, id: String) {
    if (usermap.containsKey(session)) {
        println("Already registered a session")
    }

    println("Registered $id")
    usermap[session] = id
}

fun sendError(session: WsSession, err: String) {
    if (session.isOpen) {
        session.send(json { obj("error" to err) }.toJsonString())
    }
}