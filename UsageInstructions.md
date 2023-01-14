## How-To Guide

### Scenario 1

Here we will create a couple of vanilla bizlogics and demonstrate control dependencies between them by stitching a flow
and executing them.

```java
class BizlogicA implements IBizlogic {

    @Override
    public void execute(TefContext tefContext) {
        System.out.println(this.getClass().getName());
    }
}

// This class demonstrates the way to define control dependency
@DependsOn(BizlogicA.class)
class BizlogicB implements IBizlogic {

    @Override
    public void execute(TefContext tefContext) {
        System.out.println(this.getClass().getName());
    }
}
```

Building and executing the flow

```java
public class Demo {
    public static void main(String args[]) {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(BizlogicA.class);
        flowBuilder.add(BizlogicB.class);
        // Please note that the order of adding bizlogics to the flow is inconsequential here. 
        // All the bizlogics in the flow are topologically sorted before at the time of `flowBuilder.build()`.

        SimpleFlow simpleFlow = flowBuilder.build();

        // Creating a FlowExecutor which takes flow and context as inputs.
        FlowExecutor executor = new FlowExecutor(flow, new DataContext(), new TefContext());
        // The execute method is a blocking call.
        executor.execute();
        
        // output
        /*
         BizlogicA
         BizlogicB
         * */
    }
}

```

### Scenario 2

Defining Data Dependency

To define data dependency, create a data adapter to emit data

```java
// A sample data class (to be emitted by a DataAdapter)
public class SampleDataAdapter extends DataAdapterBizlogic<String> {
    
    @Override
    public String adapt(TefContext tefContext) {
        System.out.println(this.getClass().getName());
        return "Hello World";
    }
}
```

Now this DataAdapter can be used in a bizlogic by injecting its data

```java
class BizlogicC implements IBizlogic {

    @InjectData
    private String dataFromSampleDataAdapter;
    
    @Override
    public void execute(TefContext tefContext) {
        System.out.println(this.getClass().getName());
        System.out.println(dataFromSampleDataAdapter);
    }
}
```

Building and executing the flow

```java
public class Demo {
    public static void main(String args[]) {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.add(BizlogicA.class);
        flowBuilder.add(BizlogicB.class);
        flowBuilder.add(BizlogicC.class);
        flowBuilder.add(SampleDataAdapter.class);
        SimpleFlow simpleFlow = flowBuilder.build();
        FlowExecutor executor = new FlowExecutor(flow, new DataContext(), new TefContext());
        executor.execute();
        
        // output
        /*
         BizlogicA
         BizlogicB
         SampleDataAdapter
         Hello World
         * */
    }
}

```