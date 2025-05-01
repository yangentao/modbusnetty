package io.github.yangentao.modbus.netty

import io.github.yangentao.modbus.proto.BusRequest
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

@ChannelHandler.Sharable
class BusRequestEncoder : MessageToMessageEncoder<BusRequest>() {
    override fun encode(ctx: ChannelHandlerContext, msg: BusRequest, out: MutableList<Any>) {
        out.add(Unpooled.wrappedBuffer(msg.bytes))
    }
}
