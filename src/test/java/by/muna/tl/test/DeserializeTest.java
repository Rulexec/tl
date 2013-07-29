package by.muna.tl.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Test;

import by.muna.tl.IConstructorProvider;
import by.muna.tl.ITypedDataProvider;
import by.muna.tl.TL;
import by.muna.tl.TypedData;
import by.muna.types.Constructor;
import by.muna.types.ConstructorArgs;
import by.muna.types.Type;

public class DeserializeTest {

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
        
        ITypedDataProvider expected = new TypedData(c, new Object[] {
            "length 8".getBytes(), // string
            
            new TypedData(TL.VECTOR.applyType(TL.INT), new Object[] { // vector
                new Integer[] {1, 2, 3, 4, 5} // vector first argument
            }),
            
            1337L // long
        });
        
        byte[] serialized = SerializeTest.fromHex(
            "08" + "6c656e6774682038" + "000000" + // string
            "ae5538a0" + // crc32("vector # [ int ] = Vector int")
                "05000000" + // vector elements count
                    "01000000" + "02000000" + "03000000" + "04000000" + "05000000" +
            "3905000000000000" // long
        );
        
        IConstructorProvider provider = new IConstructorProvider() {
            public Constructor getConstructor(int id) {
                switch (id) {
                case 0xa03855ae: return TL.VECTOR.applyType(TL.INT);
                default:
                    return null;
                }
            }
        };
        
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        ITypedDataProvider given = (ITypedDataProvider) TL.deserialize(provider, c, buffer);
        
        Assert.assertArrayEquals( // string
            SerializeTest.fromHex("6c656e6774682038"),
            (byte[]) given.getTypedData(0)
        );
        
        ITypedDataProvider numbersGiven = (ITypedDataProvider) given.getTypedData(1);
        Assert.assertEquals(0xa03855ae, numbersGiven.getConstructor().getId());
        Assert.assertArrayEquals(
            new Integer[] {1, 2, 3, 4, 5},
            (Integer[]) numbersGiven.getTypedData(0)
        );
        
        Assert.assertEquals( // long
            (Long) 1337L,
            (Long) given.getTypedData(2)
        );
    }
    
    @Test
    public void int128() {
        byte[] serialized = SerializeTest.fromHex(
            "112233445566778899aabbccddeeff11"
        );
        
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        byte[] deserialized = (byte[]) TL.deserialize(null, TL.INT128, buffer);
        
        Assert.assertArrayEquals(serialized, deserialized);
    }
    
    @Test
    public void int256() {
        byte[] serialized = SerializeTest.fromHex(
            "112233445566778899aabbccddeeff11" +
            "112233445566778899aabbccddeeff11"
        );
        
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        byte[] deserialized = (byte[]) TL.deserialize(null, TL.INT256, buffer);
        
        Assert.assertArrayEquals(serialized, deserialized);
    }

}
