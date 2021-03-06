package by.muna.tl;

import java.beans.Encoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.NumberFormat;
import java.util.Locale;

import by.muna.tl.by.muna.magic.utf8.UTF8;
import by.muna.types.Constructor;
import by.muna.types.ConstructorArg;
import by.muna.types.ConstructorArgs;
import by.muna.types.DataHole;
import by.muna.types.IType;
import by.muna.types.Type;
import by.muna.types.TypeHole;
import by.muna.types.VectorHole;

public class TL implements TLValue {
    public static final int
        INT_ID = 0xa8509bda, // int ? = Int
        INT128_ID = 0xba4de549, // int128 ? = Int
        INT256_ID = 0x09b143a3, // int256 ? = Int
        STRING_ID = 0xb5286e24, // string ? = String
        LONG_ID = 0x22076cba, // long ? = Long
        DOUBLE_ID = 0x2210c154; // double ? = Double
    
    public static final ConstructorArgs SINGLE_DATA_HOLE = new ConstructorArgs(DataHole.HOLE);
    
    public static final TypeHole TYPE_HOLE_0 = new TypeHole(0);
    
    public static final Type INT_TYPE = new Type("Int");
    public static final Type LONG_TYPE = new Type("Long");
    public static final Type DOUBLE_TYPE = new Type("Double");
    public static final Type STRING_TYPE = new Type("String");
    public static final Type VECTOR_TYPE = new Type("Vector", 1);
    public static final Type OBJECT_TYPE = new Type("Object");
    
    public static final Constructor VECTOR = new Constructor(
        "vector", TL.VECTOR_TYPE, new ConstructorArgs(new VectorHole(TL.TYPE_HOLE_0))
    );
    
    public static final Constructor INT = new Constructor("int", TL.INT_TYPE, TL.SINGLE_DATA_HOLE);
    public static final Constructor INT128 = new Constructor("int128", TL.INT_TYPE, TL.SINGLE_DATA_HOLE);
    public static final Constructor INT256 = new Constructor("int256", TL.INT_TYPE, TL.SINGLE_DATA_HOLE);
    
    public static final Constructor LONG = new Constructor("long", TL.LONG_TYPE, TL.SINGLE_DATA_HOLE);
    public static final Constructor DOUBLE = new Constructor("double", TL.DOUBLE_TYPE, TL.SINGLE_DATA_HOLE);
    public static final Constructor STRING = new Constructor("string", TL.STRING_TYPE, TL.SINGLE_DATA_HOLE);

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    
    private IType type;
    private ITypedData data;
    
    public TL(Type type, ITypedData data) {
        this.data = data;
        this.type = type;
    }
    public TL(ITypedData data) {
        this.data = data;
        this.type = this.data.getConstructor();
    }

    @Override
    public ITypedData getData() {
        return this.data;
    }
    
    @Override
    public int getId() {
        return this.data.getConstructor().getId();
    }
    @Override
    public int calcSize() {
        return TL.calcSize(this.type, this.data);
    }
    @Override
    public byte[] serialize() {
        byte[] serialized = new byte[this.calcSize()];
        
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    
        TL.serialize(this.type, this.data, buffer);
        
        return serialized;
    }
    @Override
    public void serialize(ByteBuffer buffer) {
        TL.serialize(this.type, this.data, buffer);
    }

    @Override
    public String toString() {
        return TL.toString(this.type, this.data);
    }
    
    public static ITypedData parse(IConstructorProvider schema, ByteBuffer buffer) {
        return (ITypedData) TL.deserialize(schema, TL.OBJECT_TYPE, buffer);
    }
    public static ITypedData parse(IConstructorProvider schema, byte[] serialized) {
        return TL.parse(schema, TL.OBJECT_TYPE, serialized, 0);
    }
    public static ITypedData parse(IConstructorProvider schema, byte[] serialized, int offset) {
        return TL.parse(schema, TL.OBJECT_TYPE, serialized, offset);
    }
    public static ITypedData parse(IConstructorProvider schema, IType type, byte[] serialized, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(offset);
        
        return (ITypedData) TL.deserialize(schema, type, buffer);
    }
    public static ITypedData parse(IConstructorProvider schema, IType type, ByteBuffer buffer) {
        return (ITypedData) TL.deserialize(schema, type, buffer);
    }

    public static String toString(IType t, Object data) {
        return TL.toString(t, data, false);
    }
    public static String toString(IType t, Object data, boolean boxed) {
        Constructor c;

        switch (t.getTypeType()) {
        case CONSTRUCTOR:
            c = (Constructor) t;

            switch (c.getId()) {
            case TL.INT_ID: return data.toString();
            case TL.LONG_ID: return data.toString() + "L";
            case TL.DOUBLE_ID: return NumberFormat.getNumberInstance(Locale.US).format(data) + "d";
            case TL.INT128_ID: case TL.INT256_ID: return "0x" + TL.bytesToHex((byte[]) data);
            case TL.STRING_ID: return '\'' + TL.bytesToHex((byte[]) data) + '\'';
            default:
                ITypedData typedData = (ITypedData) data;

                StringBuilder sb = new StringBuilder();
                sb.append(boxed ? '[' : '(');

                sb.append(c.getRootName());

                int i = 0;
                for (ConstructorArg arg : c.getArgs()) {
                    sb.append(' ');

                    if (arg.getName() != null) {
                        sb.append(arg.getName()).append(':');
                    }

                    sb.append(TL.toString(arg.getType(), typedData.getTypedData(i++)));
                }

                sb.append(boxed ? ']' : ')');

                return sb.toString();
            }
        case TYPE:
            ITypedData typedData = (ITypedData) data;

            return TL.toString(typedData.getConstructor(), typedData, true);
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();

                Object[] vectorData = (Object[]) data;

                StringBuilder sb = new StringBuilder();

                boolean isFirst = true;
                for (Object o : vectorData) {
                    if (!isFirst) sb.append(", ");
                    else isFirst = false;

                    sb.append(TL.toString(specialisation, o));
                }

                return sb.toString();
            } else {
                throw new RuntimeException("unsupported pseudo-type: " + t.getClass().getCanonicalName());
            }
        default:
            throw new RuntimeException("unknown type type: " + t.getTypeType());
        }
    }
    
    public static int calcSize(IType t, Object data) {
        Constructor c;
    
        switch (t.getTypeType()) {
        case CONSTRUCTOR:
            c = (Constructor) t;
        
            switch (c.getId()) {
            case TL.INT_ID: return 4;
            case TL.INT128_ID: return 16;
            case TL.INT256_ID: return 32;
            case TL.LONG_ID: case TL.DOUBLE_ID: return 8;
            case TL.STRING_ID:
                int bytesLength;

                if (data instanceof String) {
                    bytesLength = UTF8.calcSize((String) data);
                } else {
                    bytesLength = ((byte[]) data).length;
                }
                
                int length = (bytesLength <= 253 ? 1 : 4) + bytesLength;
                
                return length + (4 - length % 4) % 4;
            default:
                ITypedData typedData = (ITypedData) data;
                
                int total = 0;
                
                int i = 0;
                for (ConstructorArg arg : c.getArgs()) {
                    total += TL.calcSize(arg.getType(), typedData.getTypedData(i++));
                }
                
                return total;
            }
        case TYPE:
            c = ((ITypedData) data).getConstructor();
            
            return 4 + TL.calcSize(c, data); 
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();
                
                if (specialisation instanceof Constructor) {
                    c = (Constructor) specialisation;
                    
                    switch (c.getId()) {
                    case TL.INT_ID: return 4 + 4 * ((Integer[]) data).length;
                    case TL.INT128_ID: return 4 + 16 * ((Byte[]) data).length;
                    case TL.INT256_ID: return 4 + 32 * ((Byte[]) data).length;
                    case TL.LONG_ID: return 4 + 8 * ((Long[]) data).length;
                    case TL.DOUBLE_ID: return 4 + 8 * ((Double[]) data).length;
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
            case TL.DOUBLE_ID: buffer.putDouble((Double) data); return;
            case TL.INT128_ID: case TL.INT256_ID: buffer.put((byte[]) data); return;
            case TL.STRING_ID:
                int bytesLength;
                ByteBuffer stringBuffer = null;

                if (data instanceof String) {
                    bytesLength = UTF8.calcSize((String) data);
                } else {
                    bytesLength = ((byte[]) data).length;
                }
                
                int length = bytesLength + (bytesLength <= 253 ? 1 : 4);
            
                if (bytesLength <= 253) {
                    buffer.put((byte) bytesLength);
                } else {
                    buffer.put((byte) 254);
                    byte q = (byte) ((bytesLength >>> 0) & 0xff),
                         w = (byte) ((bytesLength >>> 8) & 0xff),
                         e = (byte) ((bytesLength >>> 16) & 0xff);
                    
                    buffer.put(q);
                    buffer.put(w);
                    buffer.put(e);
                }

                if (data instanceof String) {
                    UTF8.encode((String) data, buffer);
                } else {
                    buffer.put((byte[]) data);
                }
                
                buffer.position(buffer.position() + (4 - length % 4) % 4);
                
                return;
            default:
                ITypedData typedData = (ITypedData) data;
                
                int i = 0;
                for (ConstructorArg arg : c.getArgs()) {
                    TL.serialize(arg.getType(), typedData.getTypedData(i++), buffer);
                }
            }
            
            return;
        case TYPE:
            Type type = (Type) t;
        
            ITypedData typedData = (ITypedData) data;

            c = typedData.getConstructor();
            
            if (type.getRoot() != TL.VECTOR_TYPE) {
                buffer.putInt(c.getId());
            } else {
                buffer.putInt(0x1cb5c415);
            }
            
            TL.serialize(c, typedData, buffer);
            
            return;
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();
                
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
        
            byte[] bytes;
        
            switch (c.getId()) {
            case TL.INT_ID: return buffer.getInt();
            case TL.LONG_ID: return buffer.getLong();
            case TL.DOUBLE_ID: return buffer.getDouble();
            case TL.INT128_ID:
                bytes = new byte[16];
                
                buffer.get(bytes);
                
                return bytes;
            case TL.INT256_ID:
                bytes = new byte[32];
                
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
            Type type = (Type) t;
            
            int id = buffer.getInt();
            
            if (type.getRoot() != TL.VECTOR_TYPE) {
                c = constructorProvider.getConstructor(id);
                if (c == null) throw new RuntimeException("No constructor: " + Long.toHexString(id & 0xffffffff));
            } else {
                c = TL.VECTOR.applyType(type.getSpecialisation());
            }
            
            return TL.deserialize(constructorProvider, c, buffer);
        case PSEUDO:
            if (t instanceof VectorHole) {
                VectorHole hole = (VectorHole) t;
                IType specialisation = hole.getSpecialisation();
                
                int count = buffer.getInt();
                
                Object[] values = null;
                
                if (specialisation instanceof Constructor) {
                    c = (Constructor) specialisation;
                    
                    switch (c.getId()) {
                    case TL.INT_ID: values = new Integer[count]; break;
                    case TL.LONG_ID: values = new Long[count]; break;
                    case TL.DOUBLE_ID: values = new Double[count]; break;
                    }
                }
                
                if (values == null) values = new Object[count];
                
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

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            String hexString = Integer.toHexString(b & 0xff);

            if (hexString.length() == 1) sb.append('0');

            sb.append(hexString);
        }

        return sb.toString();
    }
}
