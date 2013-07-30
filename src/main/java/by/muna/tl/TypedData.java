package by.muna.tl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import by.muna.types.Constructor;
import by.muna.types.IType;

public class TypedData implements ITypedData, TLValue {
    private Constructor constructor;
    private Object[] data;

    public TypedData(Constructor type) {
        this.constructor = type;
        this.data = new Object[constructor.getArgs().getCount()];
    }
    public TypedData(Constructor constructor, Object[] data) {
        this.constructor = constructor;
        this.data = data;
    }

    @Override
    public Constructor getConstructor() {
        return this.constructor;
    }

    @Override
    public int getTypedDataCount() {
        return this.data.length;
    }

    @Override
    public <T> T getTypedData(int i) {
        return (T) this.data[i];
    }
    
    @Override
    public TypedData setTypedData(int i, Object o) {
        this.data[i] = o;
        return this;
    }
    
    @Override
    public int getId() {
        return this.constructor.getId();
    }
    @Override
    public int calcSize() {
        return TL.calcSize(this.constructor, this.data);
    }
    @Override
    public byte[] serialize() {
        byte[] serialized = new byte[this.calcSize()];
        
        ByteBuffer buffer = ByteBuffer.wrap(serialized);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    
        TL.serialize(this.constructor, this.data, buffer);
        
        return serialized;
    }
    @Override
    public void serialize(ByteBuffer buffer) {
        TL.serialize(this.constructor, this.data, buffer);
    }

}
