package flipkart.tef.guicebridge;

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
    private final Field field;
    private final String injectionName;
    private final InjectableValueProvider valueProvider;

    // package-private to let only the type listener create an instance
    InjectDataGuiceMembersInjector(Field field, String name, InjectableValueProvider valueProvider) {
        this.field = field;
        this.injectionName = name;
        field.setAccessible(true);
        this.valueProvider = valueProvider;
    }

    @Override
    public void injectMembers(T instance) {
        try {
            field.set(instance, valueProvider.getValueToInject(field.getType(), injectionName));
        } catch (IllegalAccessException | TefExecutionException e) {
            throw new RuntimeException("Exception while injecting members in tef-guice bridge", e);
        }
    }
}
