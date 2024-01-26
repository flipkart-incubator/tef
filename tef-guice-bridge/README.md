### Tef Guice Bridge

The objective of this module is to use Guice's Dependency Injection techniques to inject data available within tef.

While Guice uses `@Inject` to inject any arbitrary dependency in fields or methods, TEF uses `@InjectData`
to inject data that is emitted from data adapters, to stitch the data dependency. For classes that are using both Guice
and TEF can leverage this module to use `@Inject` instead of `@InjectData` to inject data objects as well.

Please note that this data injection is outside the Flow Graph Execution and dependency checks are not performed. i.e.
Use Guice bridge on classes that are not part of simple flow.

A class has to add the annotation `@TefRequestScoped` if it wants to leverage this capability.

### Usage Instructions

1. Install the Guice Module `GuiceBridgeModule`
2. Mark the class with `@TefRequestScoped` wherever required
3. Use `@Inject` for data objects if they are being emitted via Data Adapters from Simple Flow
4. Use them anywhere in code without propagating it throughout.
5. Dependency Checks will not be performed on the data being injected. If the relevant DataAdapter has not been executed
   yet, a null injection will be made.