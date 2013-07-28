package by.muna.tl;

import by.muna.types.Constructor;
import by.muna.types.IType;

public class TypedData implements ITypedDataProvider {
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
    public void setTypedData(int i, Object o) {
        this.data[i] = o;
    }

}
