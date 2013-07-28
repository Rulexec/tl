package by.muna.tl;

import java.nio.ByteBuffer;

import by.muna.types.Constructor;
import by.muna.types.ConstructorArg;
import by.muna.types.ConstructorArgs;
import by.muna.types.DataHole;
import by.muna.types.IType;
import by.muna.types.Type;
import by.muna.types.TypeHole;
import by.muna.types.VectorHole;

public class TL {
    public static final int
        INT_ID = 0xa8509bda, // int ? = Int
        INT128_ID = 0xba4de549, // int128 ? = Int
        INT256_ID = 0x09b143a3, // int256 ? = Int
        STRING_ID = 0xb5286e24, // string ? = String
        LONG_ID = 0x22076cba; // long ? = Long
    
    public static ConstructorArgs SINGLE_DATA_HOLE = new ConstructorArgs(DataHole.HOLE);
    
    public static TypeHole TYPE_HOLE_0 = new TypeHole(0);
    
    public static final Type INT_TYPE = new Type("Int");
    public static final Type LONG_TYPE = new Type("Long");
    public static final Type STRING_TYPE = new Type("String");
    public static final Type VECTOR_TYPE = new Type("Vector", 1);
    
    public static final Constructor VECTOR = new Constructor(
        "vector", TL.VECTOR_TYPE, new ConstructorArgs(new VectorHole(TL.TYPE_HOLE_0))
    );
    
    public static final Constructor INT = new Constructor("int", TL.INT_TYPE, TL.SINGLE_DATA_HOLE);
    public static final Constructor INT128 = new Constructor("int128", TL.INT_TYPE, TL.SINGLE_DATA_HOLE);
    public static final Constructor INT256 = new Constructor("int256", TL.INT_TYPE, TL.SINGLE_DATA_HOLE);
    
    public static final Constructor LONG = new Constructor("long", new Type("Long"), TL.SINGLE_DATA_HOLE);
    public static final Constructor STRING = new Constructor("string", new Type("String"), TL.SINGLE_DATA_HOLE);
    
    public static int calcSize(IType t, Object data) {
        Constructor c;
    
        switch (t.getTypeType()) {
        case CONSTRUCTOR:
            c = (Constructor) t;
        
            switch (c.getId()) {
            case TL.INT_ID: return 4;
            case TL.LONG_ID: return 8;
            case TL.STRING_ID:
                byte[] bytes = (byte[]) data;
                
                int length = (bytes.length <= 253 ? 1 : 4) + bytes.length;
                
                return length + (4 - length % 4) % 4;
            default:
                ITypedDataProvider typedData = (ITypedDataProvider) data;
                
                int total = 0;
                
                int i = 0;
                for (ConstructorArg arg : c.getArgs()) {
                    total += TL.calcSize(arg.getType(), typedData.getTypedData(i++));
                }
                
                return total;
            }
        case TYPE:
            c = ((ITypedDataProvider) data).getConstructor();
            
            return 4 + TL.calcSize(c, data); 
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();
                
                if (specialisation instanceof Constructor) {
                    c = (Constructor) specialisation;
                    
                    switch (c.getId()) {
                    case TL.INT_ID: return 4 + 4 * ((int[]) data).length;
                    case TL.INT128_ID: return 4 + 16 * ((byte[]) data).length;
                    case TL.INT256_ID: return 4 + 32 * ((byte[]) data).length;
                    case TL.LONG_ID: return 4 + 8 * ((long[]) data).length;
                    }
                }
                
                int total = 4;
                
                Object[] vectorData = (Object[]) data;
                for (Object o : vectorData) {
                    total += TL.calcSize(specialisation, o);
                } 
                
                return total;
            } else {
                throw new RuntimeException("unsupported pseudo-type: " + data.getClass().getCanonicalName());
            }
        default:
            throw new RuntimeException("unknown type type: " + t.getTypeType());
        }
    }
    
    public static void serialize(IType t, Object data, ByteBuffer buffer) {
        Constructor c;
        
        switch (t.getTypeType()) {
        case CONSTRUCTOR:
            c = (Constructor) t;
        
            switch (c.getId()) {
            case TL.INT_ID: buffer.putInt((Integer) data); return;
            case TL.LONG_ID: buffer.putLong((Long) data); return;
            case TL.INT128_ID: case TL.INT256_ID: buffer.put((byte[]) data); return;
            case TL.STRING_ID:
                byte[] bytes = (byte[]) data;
                
                int length = bytes.length + (bytes.length <= 253 ? 1 : 4);
            
                if (bytes.length <= 253) {
                    buffer.put((byte) bytes.length);
                } else {
                    buffer.put((byte) 254);
                    byte q = (byte) ((bytes.length >>> 0) & 0xff),
                         w = (byte) ((bytes.length >>> 8) & 0xff),
                         e = (byte) ((bytes.length >>> 16) & 0xff);
                    
                    buffer.put(q);
                    buffer.put(w);
                    buffer.put(e);
                }
                
                buffer.put(bytes);
                
                buffer.position(buffer.position() + (4 - length % 4) % 4);
                
                return;
            default:
                ITypedDataProvider typedData = (ITypedDataProvider) data;
                
                int i = 0;
                for (ConstructorArg arg : c.getArgs()) {
                    TL.serialize(arg.getType(), typedData.getTypedData(i++), buffer);
                }
            }
            
            return;
        case TYPE:
            ITypedDataProvider typedData = (ITypedDataProvider) data;
            c = typedData.getConstructor();
            
            buffer.putInt(c.getId());
            
            TL.serialize(c, typedData, buffer);
            
            return;
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();
                
                if (specialisation instanceof Constructor) {
                    c = (Constructor) specialisation;
                    
                    switch (c.getId()) {
                    case TL.INT_ID:
                        int[] intValues = (int[]) data;
                        
                        buffer.putInt(intValues.length);
                        
                        for (int value : intValues) { buffer.putInt(value); }
                        
                        return;
                    case TL.LONG_ID:
                        long[] longValues = (long[]) data;
                        
                        buffer.putInt(longValues.length);
                        
                        for (long value : longValues) { buffer.putLong(value); }
                        
                        return;
                    }
                }
                
                Object[] vectorData = (Object[]) data;
                
                buffer.putInt(vectorData.length);
                
                for (Object o : vectorData) {
                    TL.serialize(specialisation, o, buffer);
                } 
                
                return;
            } else {
                throw new RuntimeException("unsupported pseudo-type: " + t.getClass().getCanonicalName());
            }
        default:
            throw new RuntimeException("unknown type type: " + t.getTypeType());
        }
    }
    
    public static Object deserialize(IConstructorProvider constructorProvider, IType t, ByteBuffer buffer) {
        Constructor c;
        
        switch (t.getTypeType()) {
        case CONSTRUCTOR:
            c = (Constructor) t;
        
            int bytesCount;
            byte[] bytes;
        
            switch (c.getId()) {
            case TL.INT_ID: return buffer.getInt();
            case TL.LONG_ID: return buffer.getLong();
            case TL.INT128_ID:
                bytesCount = 16;
            case TL.INT256_ID:
                bytesCount = 32;
                
                bytes = new byte[bytesCount];
                
                buffer.get(bytes);
                
                return bytes;
            case TL.STRING_ID:
                int first = buffer.get() & 0xff;
                
                int length = 0;
                int totalLength;
            
                if (first <= 253) {
                    length = first;
                    totalLength = length + 1;
                } else {
                    length = ((buffer.get() & 0xff) << 0)
                           + ((buffer.get() & 0xff) << 8)
                           + ((buffer.get() & 0xff) << 16);
                    totalLength = length + 4;
                }
                
                bytes = new byte[length];
                
                buffer.get(bytes);
                
                buffer.position(buffer.position() + (4 - totalLength % 4) % 4);
                
                return bytes;
            default:
                Object[] values = new Object[c.getArgs().getCount()];
                
                int i = 0;
                for (ConstructorArg arg : c.getArgs()) {
                    values[i++] = TL.deserialize(constructorProvider, arg.getType(), buffer);
                }
                
                return new TypedData(c, values);
            }
        case TYPE:
            int id = buffer.getInt();
            
            c = constructorProvider.getConstructor(id);
            if (c == null) throw new RuntimeException("No constructor: " + Long.toHexString(id & 0xffffffff));
            
            return TL.deserialize(constructorProvider, c, buffer);
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();
                
                int count = buffer.getInt();
                
                if (specialisation instanceof Constructor) {
                    c = (Constructor) specialisation;
                    
                    switch (c.getId()) {
                    case TL.INT_ID:
                        int[] intValues = new int[count];
                        
                        for (int i = 0; i < count; i++) { intValues[i] = buffer.getInt(); }
                        
                        return intValues;
                    case TL.LONG_ID:
                        long[] longValues = new long[count];
                        
                        for (int i = 0; i < count; i++) { longValues[i] = buffer.getLong(); }
                        
                        return longValues;
                    }
                }
                
                Object[] values = new Object[count];
                
                for (int i = 0; i < count; i++) {
                    values[i] = TL.deserialize(constructorProvider, specialisation, buffer);
                }
                
                return values;
            } else {
                throw new RuntimeException("unsupported pseudo-type: " + t.getClass().getCanonicalName());
            }
        default:
            throw new RuntimeException("unknown type type: " + t.getTypeType());
        }
    }
}
