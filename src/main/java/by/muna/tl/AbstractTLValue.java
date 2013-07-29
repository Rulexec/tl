package by.muna.tl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import by.muna.types.Constructor;
import by.muna.types.IType;

public class AbstractTLValue implements ITypedDataProvider, TLValue {
    protected IType type;
    protected TypedData data;
    
    protected AbstractTLValue(Constructor constructor) {
        this(constructor, new TypedData(constructor));
    }
    protected AbstractTLValue(Constructor constructor, Object[] argsData) {
        this(constructor, new TypedData(constructor, argsData));
    }
    protected AbstractTLValue(IType type, TypedData data) {
        this.type = type;
        this.data = data;
    }
    
    public TypedData getData() {
        return this.data;
    }

    @Override
    public int getId() {
        return this.data.getConstructor().getId();
    }

    @Override
    public int calcSize() {
        return TL.calcSize(this.type, data);
    }

    @Override
    public byte[] serialize() {
        byte[] result = new byte[this.calcSize()];
        
        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        TL.serialize(this.type, this.data, buffer);
        
        return result;
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        TL.serialize(this.type, this.data, buffer);
    }
    
    @Override
    public Constructor getConstructor() {
        return this.data.getConstructor();
    }
    @Override
    public int getTypedDataCount() {
        return this.data.getTypedDataCount();
    }
    @Override
    public void setTypedData(int i, Object o) {
        this.data.setTypedData(i, o);
    }
    @Override
    public <T> T getTypedData(int i) {
        return this.data.getTypedData(i);
    }

}
