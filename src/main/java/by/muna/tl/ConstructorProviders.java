package by.muna.tl;

import java.util.LinkedList;
import java.util.List;

import by.muna.types.Constructor;

public class ConstructorProviders implements IConstructorProvider {
    private List<IConstructorProvider> providers;
    
    public ConstructorProviders() {
        this.providers = new LinkedList<IConstructorProvider>();
    }
    
    public void addProvider(IConstructorProvider provider) {
        this.providers.add(provider);
    }

    @Override
    public Constructor getConstructor(int id) {
        for (IConstructorProvider provider : this.providers) {
            Constructor c = provider.getConstructor(id);
            if (c != null) return c;
        }
        
        return null;
    }
}
