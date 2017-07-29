# Syntax

## There's the syntax?
The syntax is nearly invisible, there's no reserved words, precedence rules, commas, semicolons, parenthesis, curly or square brackets.
There's only a few special forms, such as assignment. 
Polish notation is used for everything, however, smart indentation automatically provided by IDE makes code easy to read.

## Literals
Standard literal types are strings, signed integers and real numbers:

`123`
`0xFF`
`1.23E-4`
`"It's a string"`

Number literals are not typed, i.e. there's no syntax like `1.23f`.

`true`, `false` and `null` are just constant names, not actually a part of the syntax.

Multiline strings can be represented by IDE in compact and expanded forms.
Everything is an object, there's no distinction between primitives and "real" objects.
String, numbers and booleans are immutable and are always compared by value.
(For optimization purposes, on the implementation level there can be multiple instances 
of a string or an integer with the same value, but it should be completely invisible on the the level 
on which the language operates).
There's no such thing as object identity for immutable objects!
There's no way to distinguish between two instances of integers with the same value. 
There's one and only one 42 in this world!

TBD: 
- Do we need special literals for characters and binary values?
- Multilingual string literals?

There's no need to have a special syntax regular expressions, dates, etc.  

##Assignments

Variable declarations and assignments are as simple as this:

`= x 1`

or
 
`= x + x 1`

Assignments are always local to the parent function, method or lambda.
There's no need for a special variable declaration syntax.
There's no way to declare a variable without assigning it a value but you can always assign `null`:

```
= value null
if use-names
    = value "five"
else
    = value 5
```

For the easy of typing variable names should be all lowercase with words separated by hyphens:
```
= my-xml-parser create-new-parser default-xml-parser-factory
``` 

TBD: Do we need a handful of ++, --, +=, *=, etc?

##Function calls

Function calls have the form of `foo arg1 arg2 arg3`. No parenthesis or commas are needed.
Nested calls also don't require parenthesis, because the interpreter always knows how many arguments each function takes,
so it can always figure out if `foo bar x y` means `foo(bar(x, y))`, `foo(bar(x), y)`, `foo(bar(), x, y)`.
All functions have fixed arity, there's no error-prone `varargs` syntax.
The IDE is also able to pretty-print complex expressions with nested calls. 

Novice users should be able to turn on explicit parenthesising provided by IDE, but it's not needed to type parenthesis. 
Similarly, IDE should show display argument names when possible/needed, but users don't have to type them. 

##Method calls
There's actually no difference between function and method call, they look exactly the same, so `foo arg1 arg2 arg3` can be either a method of object `arg1` or a function with three arguments defined somewhere else.
Methods and functions are pretty similar, they're just dispatched in a slightly different way.
Technically, methods are just single-dispatch polymorphic functions. 
IDE can make this difference visible by displaying them in different colors.

##Object creation
```
= order Order customer product quantity
```
Class names should be in capitalized camel case. 

##Operators!
There's no operators! All kind of operators are mere regular functions! There's no precedence rules.

`* + a b - a b` -> `(a + b) * (a - b)`

Unary minus (negation) is `neg` to distinguish it from binary `-`.

Boolean operators `and`, `or` are short-circuit and use lazy evaluation of arguments.

`and != s null > length s 0` is equivalent to `(s != null) && (s.length() > 0)` in Java.

Not is (logically) `not`.

`==` and `!=` are null-safe and check object identity for mutable objects and equality by value for immutable objects.

Again:
- There's no such thing as object identity for immutable objects!
- There's no way to distinguish between two instances of integers with the same value. 
- There's one and only one 42 in this world!

Mutable classes may define domain specific equality-like methods, e.g. `same-content`.

##Function & method references
Functional paradigm presumes that functions are firest-class objects and can be passed to other functions as arguments or returned from a function.
This means that there must be a way to obtain a reference to a function or a method without immediately applying it.
That can be done with a special `ref` meta-symbol:

`foo ref bar 10` 

calls function `foo` with two arguments, first of which is a reference to function `foo`.

A function reference can be then called at any time with `call`:
``` 
= plus ref +
= result call plus 2 3
assert == 5 result
``` 

##Currying
A function or method or method reference can be partially applied (curried):
```
= plus3 with 3 plus
```

```
= draw-red-circle with red-circle & draw 
```
##Lambdas

Lambdas are single argument anonymous functions defined within another function or lambda.
Lambda definition has the following format:
 
 `λ <arg*> : <stmt*>`
 
 
`λ : println "Hello World!"` returns a reference to an anonymous function without arguments which prints a string.

`λ x : * 2 x` returns a reference to an anonymous function which returns it's argument multiplied by two.

`λ x y : + x y` returns a reference to an anonymous function which returns a sum of its arguments.

The `λ` symbol can be entered by pressing backslash key.

This syntax is eager, it consumes the all the expressions to the right of `λ`. 

```
λ x :
    = u sin x
    = v exp x
    + * u - 1 v
```

Use indentation synatx to pass multiple lambdas to one function:
```
call compose
       λ x : * x x
       λ x : + x 2
    3  
```
Returns (3 + 2)^2 = 25


Lambdas inherit their parent lexical scope but cannot modify variables in it. 
However, lambdas can locally override variables without affecting the parent scope:

```
= a 1
= f λ :
  = a + a 1
  assert == a 2
call f 
assert == a 1
```

##Lists, Sets and Maps
Immutable lists can be defined in this way:

```
= my-list : 1 : 2 : 3 ;
```

It's not actually a special syntax again: `;` is actually a constant returning an empty list and `:` is a function with two arguments 
which prepends a list passed as a second argument with a value from the first.

Immutable set:

```
= my-set to-set : 1 : 2 : 3 ;
```

Immutable map:

```
= my-map :: k1 v1 :: k2 v2 :: k3 v3 ;
```

TBD: Do we need a better syntax? 

##if, else, elif
```
= sign if > x 0
          1
       elif < x 0
            -1
       else
            0    
```

##for
```
for shape shapes
    draw shape 
```

```
for i range 0 n
   = t * i i
   println t
```

##while
```
while has-next it
    print next it
```

##return, break, continue

```
for i range 1 n
	for j range 1 n
		if == 0
		        get matrix i j
		    println "Found a zero"    
		    break i
```

##throw, try, catch

##match

## There's nothing more!
No more syntax is needed.
Packages, function and method declarations, comments, visibility modifiers, imports, etc are all done by IDE. 


