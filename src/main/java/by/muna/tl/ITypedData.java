package by.muna.tl;

import by.muna.types.Constructor;

public interface ITypedData extends TLValue {
    Constructor getConstructor();
    int getTypedDataCount();
    ITypedData setTypedData(int i, Object o);
    <T> T getTypedData(int i);
}
