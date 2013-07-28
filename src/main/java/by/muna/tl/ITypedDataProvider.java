package by.muna.tl;

import by.muna.types.Constructor;

public interface ITypedDataProvider {
    Constructor getConstructor();
    int getTypedDataCount();
    void setTypedData(int i, Object o);
    <T> T getTypedData(int i);
}
