package com.ctrip.framework.drc.core.driver.binlog.impl;

import com.ctrip.framework.drc.core.driver.IoCache;
import com.ctrip.framework.drc.core.driver.binlog.LogEvent;
import com.ctrip.framework.drc.core.driver.binlog.header.LogEventHeader;
import com.ctrip.framework.drc.core.driver.util.ByteHelper;
import com.ctrip.xpipe.utils.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.ctrip.framework.drc.core.driver.binlog.constant.LogEventHeaderLength.eventHeaderLengthVersionGt1;
import static com.ctrip.framework.drc.core.driver.binlog.constant.LogEventType.drc_delay_monitor_log_event;
import static com.ctrip.framework.drc.core.driver.config.GlobalConfig.LOG_EVENT_IGNORABLE_F;

/**
 * @Author limingdong
 * @create 2022/9/30
 */
public class ReferenceCountedDelayMonitorLogEvent extends AbstractRowsEvent implements LogEventMerger {

    private static Logger logger = LoggerFactory.getLogger(DelayMonitorLogEvent.class);

    private String gtid;

    protected UpdateRowsEvent updateRowsEvent;

    private static final long IGNORE_SERVER_ID = 0L;

    private String srcDcName = null;

    public ReferenceCountedDelayMonitorLogEvent() {
    }

    public ReferenceCountedDelayMonitorLogEvent(String gtid, UpdateRowsEvent updateRowsEvent) {
        this.gtid = gtid;
        this.updateRowsEvent = updateRowsEvent;

        final byte[] payloadBytes = payloadToBytes();
        final int payloadLength = payloadBytes.length;

        int eventSize = eventHeaderLengthVersionGt1 + payloadLength;
        setLogEventHeader(new LogEventHeader(drc_delay_monitor_log_event.getType(), IGNORE_SERVER_ID, eventSize, eventSize, LOG_EVENT_IGNORABLE_F));

        final ByteBuf payloadByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(payloadLength);
        payloadByteBuf.writeBytes(payloadBytes);
        payloadByteBuf.skipBytes(payloadLength);
        setPayloadBuf(payloadByteBuf);
    }

    private byte[] payloadToBytes() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] gtidBytes = gtid.getBytes();
        ByteHelper.writeUnsignedIntLittleEndian(gtidBytes.length, out);
        ByteHelper.writeFixedLengthBytesFromStart(gtid.getBytes(), gtidBytes.length, out);

        ByteBuf headerBuf = updateRowsEvent.getLogEventHeader().getHeaderBuf();
        headerBuf.readerIndex(0);
        ByteBuf payloadBuf = updateRowsEvent.getPayloadBuf();
        payloadBuf.readerIndex(0);

        int headerLength = headerBuf.readableBytes();
        byte[] headerBytes = new byte[headerLength];
        headerBuf.readBytes(headerBytes, 0, headerLength);

        int payloadLength = payloadBuf.readableBytes();
        byte[] payloadBytes = new byte[payloadLength];
        payloadBuf.readBytes(payloadBytes, 0, payloadLength);

        int bytesLength = headerLength + payloadLength;
        ByteHelper.writeUnsignedIntLittleEndian(bytesLength, out);
        ByteHelper.writeFixedLengthBytesFromStart(headerBytes, headerLength, out);
        ByteHelper.writeFixedLengthBytesFromStart(payloadBytes, payloadLength, out);

        return out.toByteArray();
    }

    @Override
    public ReferenceCountedDelayMonitorLogEvent read(ByteBuf byteBuf) {
        final LogEvent logEvent = super.read(byteBuf);
        // if there is no enough byteBuf to construct current logEvent payload
        if (null == logEvent) {
            return null;
        }

        final ByteBuf payloadBuf = getPayloadBuf();
        byte[] dst = new byte[payloadBuf.readableBytes()];
        payloadBuf.readBytes(dst, 0, payloadBuf.readableBytes());

        int index = 0;
        // read gtid
        long gtidLength = ByteHelper.readUnsignedIntLittleEndian(dst, index);
        index += 4;
        byte[] gtidBytes = ByteHelper.readFixedLengthBytes(dst, index, (int) gtidLength);
        gtid = new String(gtidBytes);
        index += gtidBytes.length;

        long length = ByteHelper.readUnsignedIntLittleEndian(dst, index);
        index += 4;
        byte[] bytes = ByteHelper.readFixedLengthBytes(dst, index, (int) length);
        ByteBuf updateRowsEventByteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(0, bytes.length);

        try {
            updateRowsEventByteBuf.writeBytes(bytes);
            updateRowsEvent = new UpdateRowsEvent();
            updateRowsEvent.read(updateRowsEventByteBuf);
            ByteBuf payloadByteBuf = updateRowsEvent.getPayloadBuf();
            payloadByteBuf.skipBytes(payloadByteBuf.writerIndex());
        } catch (Exception e) {
            logger.error("read UpdateRowsEvent error", e);
        } finally {
            ReferenceCountUtil.release(updateRowsEventByteBuf);
        }

        return this;
    }


    @Override
    public void load(final List<TableMapLogEvent.Column> columns) {
        updateRowsEvent.load(columns);
    }

    @Override
    public void write(IoCache ioCache) {
        super.write(ioCache);
    }

    public void retain() {
        getLogEventHeader().getHeaderBuf().retain();
        getPayloadBuf().retain();
    }

    @Override
    protected List<ByteBuf> getEventByteBuf(ByteBuf headByteBuf, ByteBuf payloadBuf) {
        return mergeByteBuf(headByteBuf, payloadBuf);
    }

    public void release(int decrement) {
        ByteBuf headerBuf = getLogEventHeader().getHeaderBuf();
        if (null != headerBuf && headerBuf.refCnt() + 1 > decrement) {
            headerBuf.release(decrement);
        }

        ByteBuf payloadBuf = getPayloadBuf();
        if (null != payloadBuf && payloadBuf.refCnt() + 1 > decrement) {
            payloadBuf.release(decrement);
        }
    }

    public int refCnt() {
        return getLogEventHeader().getHeaderBuf().refCnt();
    }

    public String getGtid() {
        return gtid;
    }

    public UpdateRowsEvent getUpdateRowsEvent() {
        return updateRowsEvent;
    }

    public String getSrcDcName() {
        return srcDcName;
    }

    @VisibleForTesting
    public void setSrcDcName(String srcDcName) {
        this.srcDcName = srcDcName;
    }
}
