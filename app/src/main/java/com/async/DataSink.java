package com.async;

import java.nio.ByteBuffer;

import com.async.callback.CompletedCallback;
import com.async.callback.WritableCallback;

public interface DataSink {
    public void write(ByteBuffer bb);
    public void write(ByteBufferList bb);
    public void setWriteableCallback(WritableCallback handler);
    public WritableCallback getWriteableCallback();
    
    public boolean isOpen();
    public void close();
    public void end();
    public void setClosedCallback(CompletedCallback handler);
    public CompletedCallback getClosedCallback();
    public AsyncServer getServer();
}
