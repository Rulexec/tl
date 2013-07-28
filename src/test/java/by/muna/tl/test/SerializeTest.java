package by.muna.tl.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

import by.muna.tl.ITypedDataProvider;
import by.muna.tl.TL;
import by.muna.tl.TypedData;
import by.muna.types.Constructor;
import by.muna.types.ConstructorArgs;
import by.muna.types.Type;

public class SerializeTest {

    @Test
    public void constructor() {
        Constructor c = new Constructor(
            "test", new Type("Test"),
            new ConstructorArgs(
                "name", TL.STRING,
                "numbers", TL.VECTOR_TYPE.applyType(TL.INT),
                "longNumber", TL.LONG
            )
        );
        
        ITypedDataProvider data = new TypedData(c, new Object[] {
            "length 8".getBytes(), // string
            
            new TypedData(TL.VECTOR.applyType(TL.INT), new Object[] { // vector
                new int[] {1, 2, 3, 4, 5} // vector first argument
            }),
            
            1337L // long
        });
        
        byte[] expected = SerializeTest.fromHex(
            "08" + "6c656e6774682038" + "000000" + // string
            "ae5538a0" + // crc32("vector # [ int ] = Vector int")
                "05000000" + // vector elements count
                    "01000000" + "02000000" + "03000000" + "04000000" + "05000000" +
            "3905000000000000" // long
        );
        
        byte[] serialized = new byte[TL.calcSize(c, data)];
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        TL.serialize(c, data, buffer);
        
        Assert.assertArrayEquals(expected, serialized);
    }
    
    public static byte[] fromHex(String hex) {
        byte[] result = new byte[hex.length() / 2];
        
        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        
        return result;
    }

}
