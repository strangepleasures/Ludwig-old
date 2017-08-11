##Projects
Projects are largest elements of code. Every "piece of software" is a project. 

Projects are versioned. Every change increases at least the minor version.
Changes which add new functions to the public API increase the medior version.
Changes which break backward compatibility of the public API increase the major version.
In addition to that, versions can have free-form human-friendly names.

Due to the very nature of the language, every version contains all the previous versions.
That means that software developers don't have to provide multiple versions of their software, only the latest.
 
Every project can have particular versions of other projects (always including the runtime library) as dependencies.

In principle, a project can (transitively) depend on multiple versions of the same project.
A good practice allowing to avoid API conflicts between transitive dependencies is not to expose any symbols defined in project's dependencies (except to RTL) as a part of the public API.


##Packages
Packages are meant to logically group code.
Packages can contain other packages and functions.
Classes and interfaces are in fact "special" packages (see below).

##Functions
Functions are main elements of code. 
A function consists of function declaration and possibly multiple implementations (overrides), residing in different modules.
One (default) implementation can be a part of the function declaration.
Functions with multiple implementations can be called methods.
Functions without implementations are called abstract.

###Side effects
A function can have side effect. There are three different types of side effects:
- Functions which modify their arguments
- Functions which perform IO operations
- Functions which result is not fully determined by their arguments (random, etc).
Functions without side-effects called pure. Side effect are transitive. 
That means that a function calling another function inherits all side effects from it.
A result of invocation of pure function can be automatically cached by the runtime.
The runtime is supposed to use various heuristics to decide which results to cache, etc.
Generally speaking, caching and similar features of Ludwig encourage use of immutable data structures. 

###Constants?
There's no special syntax for constants. "Constants" in Ludwig are just pure functions without arguments. The result of the first invocation is cached by the runtime.


###Resources
Similarly to constants, different embedded resources (images, HTML pages, etc) are also represented as (pure) functions.
There's a special class for resources combining binary payload, mime-type and encoding information.

##Properties and Fields
Properties are functions with single argument.
The only difference between (read-only) properties and functions of one arguments is that properties are marked as such for introspection purposes.
Any function of one argument can be marked as property, and every property can be turned into a "normal" function.

Writeable properties have a second implementation taking two arguments (setter), which can be invoked with assignment operator:

`property object` calls the single-argument implementation (getter)

`= property object value` calls the setter

Fields are trivially implemented properties, with no logic in the getter or setter.
The difference between fields and properties is only in implementation, they have absolutely the same interface.
Every field can be turned into a property and vice versa without breaking API compatibility.

##Classes, Interfaces and Objects
TODO: