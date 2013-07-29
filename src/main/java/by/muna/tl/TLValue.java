package by.muna.tl;

import java.nio.ByteBuffer;

public interface TLValue {
    int getId();
    int calcSize();
    byte[] serialize();
    void serialize(ByteBuffer buffer);
}
