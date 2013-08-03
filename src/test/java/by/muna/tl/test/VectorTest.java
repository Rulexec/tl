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

public class VectorTest {
    @Test
    public void universalVectorSerialize() {
        Constructor vectorInt = TL.VECTOR.applyType(TL.INT);
        Type vectorIntType = vectorInt.getType();
        
        ITypedData data = new TypedData(vectorInt, new Object[] {
            new Integer[] {4, 5, 6}
        });
        
        ByteBuffer buffer = ByteBuffer.allocate(TL.calcSize(vectorIntType, data));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        TL.serialize(vectorIntType, data, buffer);
        
        buffer.position(0);
        
        // constructor id
        Assert.assertEquals(0x1cb5c415, buffer.getInt());
        
        // 3 elements
        Assert.assertEquals(3, buffer.getInt());
        
        // elements
        Assert.assertEquals(4, buffer.getInt());
        Assert.assertEquals(5, buffer.getInt());
        Assert.assertEquals(6, buffer.getInt());
    }
    
    @Test
    public void universalVectorDeserialize() {
        Constructor vectorInt = TL.VECTOR.applyType(TL.INT);
        Type vectorIntType = vectorInt.getType();
        
        ITypedData expected = new TypedData(vectorInt, new Object[] {
            new Integer[] {4, 5, 6}
        });
        
        byte[] serialized = SerializeTest.fromHex(
            "15c4b51c" + // universal vector id
            "03000000" + // 3 elements
                "04000000" + // elements
                "05000000" +
                "06000000"
        );
        
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        ITypedData got = (ITypedData) TL.deserialize(null, vectorIntType, buffer);
        
        Assert.assertArrayEquals(
            new Integer[] {4, 5, 6},
            (Integer[]) got.getTypedData(0)
        );
    }
}
