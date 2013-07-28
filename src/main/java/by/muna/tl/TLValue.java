package by.muna.tl;

import java.nio.ByteBuffer;

public abstract class TLValue {
    public abstract int getId();
    public abstract int calcSize();
    public abstract byte[] serialize();
    public abstract void serialize(ByteBuffer buffer);
}
