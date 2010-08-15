package org.openspaces.memcached.protocol.text;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferIndexFinder;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.openspaces.memcached.protocol.SessionStatus;
import org.openspaces.memcached.protocol.exceptions.IncorrectlyTerminatedPayloadException;


/**
 * The frame decoder is responsible for breaking the original stream up into a series of lines.
 * <p/>
 * The code here is heavily based on Netty's DelimiterBasedFrameDecoder, but has been modified because the
 * memcached protocol has two states: 1) processing CRLF delimited lines and 2) spooling results for SET/ADD
 */
public final class MemcachedFrameDecoder extends FrameDecoder {

    private final SessionStatus status;

    private final int maxFrameLength;

    private boolean discardingTooLongFrame;
    private long tooLongFrameLength;

    /**
     * Creates a new instance.
     *
     * @param status         session status instance for holding state of the session
     * @param maxFrameLength the maximum length of the decoded frame.
     *                       A {@link org.jboss.netty.handler.codec.frame.TooLongFrameException} is thrown if frame length is exceeded
     */
    public MemcachedFrameDecoder(SessionStatus status, int maxFrameLength) {
        this.status = status;
        validateMaxFrameLength(maxFrameLength);
        this.maxFrameLength = maxFrameLength;
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, org.jboss.netty.channel.Channel channel, ChannelBuffer buffer) throws Exception {
        // check the state. if we're WAITING_FOR_DATA that means instead of breaking into lines, we need N bytes
        // otherwise, we're waiting for input
        if (status.state == SessionStatus.State.WAITING_FOR_DATA) {
            if (buffer.readableBytes() < status.bytesNeeded + MemcachedResponseEncoder.CRLF.capacity()) return null;

            // verify delimiter matches at the right location
            ChannelBuffer dest = buffer.slice(status.bytesNeeded + buffer.readerIndex(), 2);

            if (!dest.equals(MemcachedResponseEncoder.CRLF)) {
                // before we throw error... we're ready for the next command
                status.ready();

                // error, no delimiter at end of payload
                throw new IncorrectlyTerminatedPayloadException("payload not terminated correctly");
            } 
            status.processingMultiline();

            // There's enough bytes in the buffer and the delimiter is at the end. Read it.
            ChannelBuffer result = buffer.slice(buffer.readerIndex(), status.bytesNeeded);
            buffer.skipBytes(status.bytesNeeded + MemcachedResponseEncoder.CRLF.capacity());

            return result;
        } 
        int minFrameLength = Integer.MAX_VALUE;
        ChannelBuffer foundDelimiter = null;
        int frameLength = buffer.bytesBefore(ChannelBufferIndexFinder.CRLF);
        if (frameLength >= 0 && frameLength < minFrameLength) {
            minFrameLength = frameLength;
            foundDelimiter = MemcachedResponseEncoder.CRLF;
        }

        if (foundDelimiter != null) {
            int minDelimLength = foundDelimiter.capacity();

            if (discardingTooLongFrame) {
                // We've just finished discarding a very large frame.
                // Throw an exception and go back to the initial state.
                long tooLongFrameLength = this.tooLongFrameLength;
                this.tooLongFrameLength = 0L;
                discardingTooLongFrame = false;
                buffer.skipBytes(minFrameLength + minDelimLength);
                fail(tooLongFrameLength + minFrameLength + minDelimLength);
            }

            if (minFrameLength > maxFrameLength) {
                // Discard read frame.
                buffer.skipBytes(minFrameLength + minDelimLength);
                fail(minFrameLength);
            }

            ChannelBuffer frame = buffer.slice(buffer.readerIndex(), minFrameLength);
            buffer.skipBytes(minFrameLength + minDelimLength);

            status.processing();

            return frame;
        } 
        if (buffer.readableBytes() > maxFrameLength) {
            // Discard the content of the buffer until a delimiter is found.
            tooLongFrameLength = buffer.readableBytes();
            buffer.skipBytes(buffer.readableBytes());
            discardingTooLongFrame = true;
        }

        return null;
    }

    private void fail(long frameLength) throws TooLongFrameException {
        throw new TooLongFrameException(
                "The frame length exceeds " + maxFrameLength + ": " + frameLength);
    }

    private static void validateMaxFrameLength(int maxFrameLength) {
        if (maxFrameLength <= 0) {
            throw new IllegalArgumentException(
                    "maxFrameLength must be a positive integer: " +
                    maxFrameLength);
        }
    }

}