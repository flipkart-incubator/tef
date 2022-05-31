package flipkart.tef.guicebridge;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import flipkart.tef.execution.InjectableValueProvider;

public class TefGuiceScope implements Scope, AutoCloseable {

    private final ThreadLocal<InjectableValueProvider> threadLocal;

    public TefGuiceScope() {
        threadLocal = new ThreadLocal<>();
    }

    @SuppressWarnings("unchecked")
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
        final String name = key.toString();
        return new Provider<T>() {
            public T get() {
                if(key.getTypeLiteral().getRawType().isAssignableFrom(InjectableValueProvider.class)){
                    return (T) threadLocal.get();
                } else {
                    return creator.get();
                }
            }

            public String toString() {
                return String.format("%s[%s]", creator, "TefRequestScoped");
            }
        };
    }

    public String toString() {
        return "TefRequestScoped";
    }

    public void open(InjectableValueProvider valueProvider){
        threadLocal.set(valueProvider);
    }

    @Override
    public void close() {
        threadLocal.remove();
    }
}
