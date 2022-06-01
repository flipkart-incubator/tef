package flipkart.tef.guicebridge;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * Guice module to setup common infra for the bridge to work
 * Date: 1/06/22
 */
public class GuiceBridgeModule extends AbstractModule {

    private final TefGuiceScope scope;

    public GuiceBridgeModule() {
        this.scope = new TefGuiceScope();
    }

    @Override
    protected void configure() {
        bindScope(TefRequestScoped.class, this.scope);
        bind(TefGuiceScope.class).toInstance(scope);
    }

    @Provides
    public InjectDataGuiceMembersInjector provideInjectDataGuiceMembersInjector(Injector injector){
        return new InjectDataGuiceMembersInjector(injector);
    }
}
