package by.muna.tl.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

import by.muna.tl.ITypedData;
import by.muna.tl.TL;
import by.muna.tl.TypedData;
import by.muna.types.Constructor;
import by.muna.types.ConstructorArgs;
import by.muna.types.Type;

public class SerializeTest {

    private static final Constructor c = new Constructor(
        "test", new Type("Test"),
        new ConstructorArgs(
            "name", TL.STRING,
            "numbers", TL.VECTOR_TYPE.applyType(TL.INT),
            "longNumber", TL.LONG
        )
    );

    private static final ITypedData data = new TypedData(SerializeTest.c, new Object[] {
        "length 8".getBytes(), // string

        new TypedData(TL.VECTOR.applyType(TL.INT), new Object[] { // vector
            new Integer[] {1, 2, 3, 4, 5} // vector first argument
        }),

        1337L // long
    });

    private static final byte[] expected = SerializeTest.fromHex(
        "08" + "6c656e6774682038" + "000000" + // string
            "15c4b51c" + // universal vector
            "05000000" + // vector elements count
            "01000000" + "02000000" + "03000000" + "04000000" + "05000000" +
            "3905000000000000" // long
    );

    @Test
    public void tlSerialize() {
        byte[] serialized = new byte[TL.calcSize(SerializeTest.c, SerializeTest.data)];
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        TL.serialize(SerializeTest.c, SerializeTest.data, buffer);
        
        Assert.assertArrayEquals(SerializeTest.expected, serialized);
    }

    @Test
    public void typedDataSerialize() {
        byte[] serialized = SerializeTest.data.serialize();

        Assert.assertArrayEquals(SerializeTest.expected, serialized);
    }
    
    public static byte[] fromHex(String hex) {
        byte[] result = new byte[hex.length() / 2];
        
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        
        return result;
    }

}
