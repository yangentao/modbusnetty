package io.github.yangentao.modbus.netty

import io.github.yangentao.modbus.service.BusMessage
import io.github.yangentao.modbus.service.IDBusMessage
import io.github.yangentao.types.PatternText
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder





// HELLO {ident}
// DTU发送的, id消息和心跳,
@ChannelHandler.Sharable
class BusMessageDecoder(val messageIdent: PatternText?, val messagePatterns: List<PatternText>, val identName: String?, val maxLength: Int = 256) : ByteToMessageDecoder() {
    init {
        isSingleDecode = true
    }

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val buf = `in`
        try {
            val ls = `in`.bytesList
            if (ls.size > maxLength) {
                ctx.fireChannelRead(buf.retain())
                return
            }
            val s = ls.string()
            val identMap = messageIdent?.tryMatchEntire(s)
            if (identMap != null && identMap.isNotEmpty()) {
                val key = identName ?: identMap.keys.first()
                out.add(IDBusMessage(s, identMap, key, identMap[key]!!))
                ls.clearBuf()
                return
            }
            for (p in messagePatterns) {
                val map = p.tryMatchEntire(s)
                if (map != null) {
                    out.add(BusMessage(s, map))
                    ls.clearBuf()
                    return
                }
            }
        } catch (_: Exception) {
            //转换成字符串错误
        }
        ctx.fireChannelRead(buf.retain())
    }
}
