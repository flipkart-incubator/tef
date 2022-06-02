package flipkart.tef.guicebridge;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import flipkart.tef.exception.TefExecutionException;
import flipkart.tef.execution.InjectableValueProvider;

import java.lang.reflect.Field;

/**
 * MemberInjector to inject the actual value on fields annotated with @DataInject.
 * @see TypeListenerForDataInjection for usage pattern
 *
 * Date: 31/05/22
 */
public class InjectDataGuiceMembersInjector<T> implements MembersInjector<T> {
    private Field field;
    private String injectionName;
    private final Injector injector;

    // package-private to let only the type listener create an instance
    InjectDataGuiceMembersInjector(Injector injector){
        this.injector = injector;
    }

    public void setField(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    public void setInjectionName(String injectionName) {
        this.injectionName = injectionName;
    }

    @Override
    public void injectMembers(T instance) {
        try {
            InjectableValueProvider valueProvider = injector.getScopeBindings().get(TefRequestScoped.class)
                    .scope(Key.get(InjectableValueProvider.class), null).get();
            field.set(instance, valueProvider.getValueToInject(field.getType(), injectionName));
        } catch (IllegalAccessException | TefExecutionException e) {
            throw new RuntimeException("Exception while injecting members in tef-guice bridge", e);
        }
    }
}
