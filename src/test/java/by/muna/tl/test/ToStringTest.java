package by.muna.tl.test;

import by.muna.tl.ITypedData;
import by.muna.tl.TL;
import by.muna.tl.TypedData;
import by.muna.types.Constructor;
import by.muna.types.ConstructorArgs;
import by.muna.types.Type;
import org.junit.Assert;
import org.junit.Test;

import java.text.NumberFormat;
import java.util.Locale;

public class ToStringTest {
    private static final Constructor c = new Constructor(
        "test", new Type("Test"),
        new ConstructorArgs(
            "name", TL.STRING,
            "intNumbers", TL.VECTOR_TYPE.applyType(TL.INT),
            "doubleNumber", TL.DOUBLE,
            "longNumber", TL.LONG
        )
    );

    private static final ITypedData data = new TypedData(ToStringTest.c, new Object[] {
        "length 8".getBytes(), // string

        new TypedData(TL.VECTOR.applyType(TL.INT), new Object[] { // vector
            new Integer[] {1, 2, 3, 4, 5} // vector first argument
        }),

        13.37d, // double

        1337L // long
    });

    @Test
    public void unboxedTest() {
        String got = TL.toString(c, data);

        Assert.assertArrayEquals(
            "length 8".getBytes(),
            SerializeTest.fromHex("6c656e6774682038")
        );

        Assert.assertEquals(
            "(test name:'6c656e6774682038' intNumbers:[vector 1, 2, 3, 4, 5] doubleNumber:13.37d longNumber:1337L)",
            got
        );
    }

    @Test
    public void boxedTest() {
        String got = TL.toString(c.getType(), data);

        Assert.assertEquals(
            "[test name:'6c656e6774682038' intNumbers:[vector 1, 2, 3, 4, 5] doubleNumber:13.37d longNumber:1337L]",
            got
        );
    }
}
