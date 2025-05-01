package io.github.yangentao.modbus.netty

import io.github.yangentao.modbus.service.BusContext
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext

class NettyModbusContext(val context: ChannelHandlerContext) : BusContext() {
    override val isActive: Boolean get() = context.channel().isActive

    override fun closeSync() {
        context.close().sync()
    }

    override fun writeBytes(data: ByteArray): Boolean {
        val a = Unpooled.wrappedBuffer(data)
        context.writeAndFlush(a)
        return true
    }

}