package io.github.yangentao.modbus.netty


import io.github.yangentao.modbus.service.BusApp
import io.github.yangentao.types.patternText
import io.netty.channel.socket.SocketChannel

class ModbusNettyServer(val app: BusApp, port: Int) : BaseNettyServer(port) {
    // share
    private val handler = BusHandler(app.endpoint, app.slaves, app.identName, app.autoQueryDelaySeconds)
    private val msgDecoder: BusMessageDecoder? = if (app.identMessage != null || app.messages.isNotEmpty()) {
        BusMessageDecoder(app.identMessage?.patternText, app.messages.map { it.patternText }, app.identName)
    } else {
        null
    }
    private val respDecoder = BusResponseDecoder(app.slaves)
    private val reqEncoder = BusRequestEncoder()

    override fun onStart() {
        app.onCreate()
    }

    override fun afterStart() {
        app.onService()
    }

    override fun onStop() {
        app.onDestroy()
    }

    override fun initChannels(ch: SocketChannel) {
        if (msgDecoder != null) {
            ch.addLast(msgDecoder)
        }
        ch.addLast(respDecoder)
        ch.addLast(reqEncoder)
        ch.addLast(handler)
    }

}