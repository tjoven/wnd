package com.async.http.filter;

import com.async.ByteBufferList;
import com.async.DataEmitter;
import com.async.FilteredDataEmitter;
import com.async.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Inflater;

public class InflaterInputFilter extends FilteredDataEmitter {
    private Inflater mInflater;
    
    @Override
    protected void report(Exception e) {
        if (e != null && mInflater.getRemaining() > 0) {
            e = new IOException("data still remaining in inflater");
        }
        super.report(e);
    }

    ByteBufferList transformed = new ByteBufferList();
    @Override
    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
        try {
            ByteBuffer output = ByteBufferList.obtain(bb.remaining() * 2);
            int totalRead = 0;
            while (bb.size() > 0) {
                ByteBuffer b = bb.remove();
                if (b.hasRemaining()) {
                    totalRead =+ b.remaining();
                    mInflater.setInput(b.array(), b.arrayOffset() + b.position(), b.remaining());
                    do {
                        int inflated = mInflater.inflate(output.array(), output.arrayOffset() + output.position(), output.remaining());
                        output.position(output.position() + inflated);
                        if (!output.hasRemaining()) {
                            output.limit(output.position());
                            output.position(0);
                            transformed.add(output);
                            assert totalRead != 0;
                            int newSize = output.capacity() * 2;
                            output = ByteBufferList.obtain(newSize);
                        }
                    }
                    while (!mInflater.needsInput() && !mInflater.finished());
                }
                ByteBufferList.reclaim(b);
            }
            output.limit(output.position());
            output.position(0);
            transformed.add(output);

            Util.emitAllData(this, transformed);
        }
        catch (Exception ex) {
            report(ex);
        }
    }

    public InflaterInputFilter() {
        this(new Inflater());
    }

    public InflaterInputFilter(Inflater inflater) {
        mInflater = inflater;
    }
}
