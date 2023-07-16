package flipkart.tef.interfaces;

/**
 * This interface is intended to expose methods that can help the flow executor eliminate its
 * dependency on concrete implementations of DI (like google guice).
 * <p>
 * Flow Executor needs instances of Bizlogic during execution and this interface is queried for those instances.
 * Clients can stub this with the `Injector` in guice or a similar implementation
 * <p>
 * Date: 16/07/23
 */
public interface BizlogicInstanceProvider {

    <T> T getInstance(Class<T> var1);
}
