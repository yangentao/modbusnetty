@file:Suppress("unused")

package io.github.yangentao.modbus.netty

import io.github.yangentao.modbus.service.BusEndpoint
import io.github.yangentao.modbus.service.BusMessage
import io.github.yangentao.modbus.service.IDBusMessage
import io.github.yangentao.modbus.service.ModbusFrame
import io.github.yangentao.types.createInstanceX
import io.github.yangentao.types.printX
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlin.reflect.KClass

@ChannelHandler.Sharable
class BusHandler(val endpointClass: KClass<out BusEndpoint>, val slaves: HashSet<Int>, val identName: String?, val autoQueryDelaySeconds: Int?) : SimpleChannelInboundHandler<ModbusFrame>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ModbusFrame) {
        val m = ctx.endpoint ?: return
        when (msg) {
            is IDBusMessage -> {
                m.identValue = msg.identValue
                m.identName = msg.identName
                val old = BusEndpoint.find(msg.identValue)
                if (old != null && old !== ctx) {
                    printX("Ident Message 被重复发送!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    old.closeSync()
                }
                BusEndpoint.identContextMap[msg.identValue] = m.context
                m.onIdent(msg)
            }

            is BusMessage -> {
                m.onMessage(msg)
            }

            is BusResponseFrame -> {
                // nothing
            }
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val inst: BusEndpoint = endpointClass.createInstanceX(NettyModbusContext(ctx)) ?: return
        ctx.endpoint = inst
        inst.slaves = slaves
        inst.autoQueryDelaySeconds = autoQueryDelaySeconds
        inst.identName = identName
        inst.onCreate()
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val m = ctx.endpoint
        if (m != null) {
            val id = m.identValue
            if (id != null && BusEndpoint.Companion.identContextMap[id] === ctx) {
                BusEndpoint.identContextMap.remove(id)
            }
            m.onClose()
        }
        ctx.propMap.clear()
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        printX(cause)
        ctx.close()
    }

}

var ChannelHandlerContext.endpoint: BusEndpoint? by ChannelProperties

