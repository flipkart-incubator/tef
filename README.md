# Task Execution Framework

[![Java CI with Maven](https://github.com/flipkart-incubator/tef/actions/workflows/maven.yml/badge.svg)](https://github.com/flipkart-incubator/tef/actions/workflows/maven.yml)
[![Maven Package](https://github.com/flipkart-incubator/tef/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/flipkart-incubator/tef/actions/workflows/maven-publish.yml)

## Philosophy

A low-footprint Workflow Execution Framework to build and execute configured DAGs.

The highlights of this framework is its ability to configure within code, with the support for Data and Control
Dependencies along with packaging logical blocks as Capabilities.

## Terminology

### Bizlogic

An arbitrary piece of logic wrapped in the `execute` method. There are different flavours of bizlogics to cater to
custom needs - like Data Adapters, Enrichers and Validators.

##### Data Adapter

A bizlogic that lets the `execute` method return an immutable value object.

##### Validator

A bizlogic that can be used to validate arbitrary items and set a status on them.

##### Enricher

A bizlogic that lets the `execute` method return a mutable object, whose value can be `enriched` over the course of the
flow.

### Capability

A capability is a collection of bizlogics. It defines a logical feature in any large arbitrary system. Such a system is
made up of 1 more `pluggable`
capabilities.

### Flow

A Flow represents a set of bizlogic to be executed in a strict pre-defined order
(topologically sorted) to adhere to the defined dependencies.

### Flow Builder

Tef has a `FluentCapabilityBuilder` which exposes a fluent api to build a flow. It supports apis to

1. Add bizlogics and its various flavors to the flow.
2. Define control dependencies between bizlogics.
3. Add capabilities to a flow.
4. Exclude arbitrary bizlogics from a flow.
5. Add implicit bindings (more on this later in the doc)

The `FlowBuilder` takes all the input from `FluentCapabilityBuilder` and topologically sorts the bizlogics, returning a
flat list of bizlogics to be executed. Flow Builder ensures the generated flow is a DAG. If cycles are found in the
definition, flow build fails at runtime.

## Dependency

Tef supports 2 types of dependency - Control Dependency and Data Dependency

### Control Dependency

Here a `Bizlogic A` is said to depend upon another `Bizlogic B`, in which case Tef ensures `B` will get executed
before `A`.

### Data Dependency

Here a `Bizlogic A` is said to depend upon `Data B`.
`Bizlogic A` does not know how the `Data B's` value is computed. Tef ensures that the data adapter responsible for
emitting `Data B` is executed before `Bizlogic A`.

### Implicit Binding

A data dependency can either be satisfied by another Data Adapter, or if the data is provided upfront at the start of
the flow execution. This is known as an Implicit Binding.

## Flow Builder

Tef has a `FluentFlowBuilder`

## Advanced Features

### [Data Injection](https://github.com/flipkart-incubator/tef/blob/main/tef-core/src/main/java/flipkart/tef/annotations/InjectData.java)

There are various flags that can be passed to a data injection viz. `mutable`, `nullable`, `name` and `optional` .

#### mutable

If a DataAdapter is injecting dependent data, and if that data can change, the injection can be marked as mutable. In
such cases, when the injected data changes, the injectee data adapter will get re-triggered.

#### nullable

This flag should be used on the bizlogics which are ok to receive a null value for this injection.

#### name

Name of the Injected data for specificity

#### optional

This parameter is indicative of the fact that the particular injection is optional. No checks will be performed if there
are no data adapters present for this injection. Such injections will only be served via the ImplicitBindings.

### Mutation Listener

`MutationListener` provides an ability to plug a callback when data returned a DataAdapter changes.

### Lifecycle Hooks

`FlowExecutionListener` provides an ability to listen to lifecycle hooks in during flow execution

Refer to [tests](https://github.com/flipkart-incubator/tef/tree/main/tef-impl/src/test/java/flipkart/tef) for usage
instructions.

## Lead Developer

@bageshwar
