package by.muna.tl.test;

import by.muna.tl.TL;
import by.muna.tl.TypedData;
import by.muna.types.Constructor;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StringTest {
    @Test
    public void serializeTest() throws Exception {
        String string = "String as utf-8 string";

        ByteBuffer buffer = ByteBuffer.allocate(TL.calcSize(TL.STRING, string));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        TL.serialize(TL.STRING, string, buffer);

        ByteBuffer expectedBuffer = ByteBuffer.allocate(buffer.capacity());
        expectedBuffer.order(ByteOrder.LITTLE_ENDIAN);

        expectedBuffer.put((byte) 22);
        expectedBuffer.put(string.getBytes("UTF-8"));

        Assert.assertArrayEquals(
            expectedBuffer.array(),
            buffer.array()
        );
    }
}
