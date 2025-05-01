package io.github.yangentao.modbus.netty

import io.github.yangentao.modbus.proto.BusResponse
import io.github.yangentao.modbus.service.ModbusFrame
import io.github.yangentao.types.uintValue
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class BusResponseFrame(val response: BusResponse) : ModbusFrame

// slave 0-247, 如果只用值 1, 可以快速判断是不是modbus包
@ChannelHandler.Sharable
class BusResponseDecoder(val slaves: Set<Int>) : ByteToMessageDecoder() {
    init {
        isSingleDecode = true
    }

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any?>) {
        val buf = `in`
        val r = detectModbusResponseFrame(ctx, buf, slaves)
        if (r != null) {
            out.add(BusResponseFrame(r))
            buf.skipAll()
            return
        }
        ctx.fireChannelRead(buf.retain())
    }

    private fun detectModbusResponseFrame(ctx: ChannelHandlerContext, byteBuf: ByteBuf, slaves: Set<Int>): BusResponse? {
        val buf = byteBuf.bytesList

        if (buf.size !in 5..255) return null
        val slave: Int = buf[0].uintValue
        if (slave !in slaves) return null
        val action = buf[1].uintValue
        val length: Int = when (action) {
            0x81, 0x82, 0x84, 0x85, 0x86, 0x8F, 0x90 -> 5
            1, 2, 3, 4 -> buf[2].uintValue + 5
            5, 6, 15, 16 -> 8
            else -> return null
        }
        if (buf.size != length) return null
        return ctx.endpoint?.context?.parseResponse(buf.toByteArray())
    }
}
