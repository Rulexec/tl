package by.muna.tl.test;

import org.junit.Assert;
import org.junit.Test;

import by.muna.tl.ITypedData;
import by.muna.tl.TL;
import by.muna.tl.TypedData;
import by.muna.types.Constructor;
import by.muna.types.ConstructorArgs;
import by.muna.types.Type;

public class SizeTest {

    @Test
    public void constructor() {
        Constructor c = new Constructor(
            "test", new Type("Test"),
            new ConstructorArgs(
                "name", TL.STRING,
                "str", TL.STRING,
                "numbers", TL.VECTOR_TYPE.applyType(TL.INT),
                "longNumber", TL.LONG
            )
        );
        
        ITypedData data = new TypedData(c, new Object[] {
            "length 8".getBytes(), // string
            "length 8", // as UTF-8 String
            
            new TypedData(TL.VECTOR.applyType(TL.INT), new Object[] { // vector
                new Integer[] {1, 2, 3, 4, 5} // vector first argument
            }),
            
            1337L // long
        });
        
        int expected =
            1 + 8 + 3 + // string: size byte, bytes, padding
            1 + 8 + 3 + // another string
            4 + // Vector constructor id
                4 + // vector elements count int
                    5 * 4 + // vector elements (integers)
            8 // long
        ;
        
        Assert.assertEquals(expected, TL.calcSize(c, data));
    }

}
