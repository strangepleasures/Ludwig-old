#### Literals
- Integer (long) values:
`123456`
- Real (double) values: `123.456`
- Boolean: `true` and `false`
- `null`
- Strings: `"blah\nblah""` No string interpolation (Let's keep it simple)
- Binary literals ?
- Characters ?
#### Variable declaration
```
= var value
```
Declares a new variable in the current scope.
#### Assignment
```
:= var value
```
Changes the value of a variable in the current or outer lexical scope.
It's slightly more difficult to change variable's value than to declare a variable because static single assignment is a good thing.
The language softly forces you to avoid mutation.

There's no way to declare a variable without assigning a value, but you can assign `null`:
```
= result null
if [and [> x 0] [< x 1]
    := result 1
else
    := result 0
```
or even
```
= result null
if useNames
    := result "five"
else
    := result 5
```
The inferred type is now `Either String  Integer`

More assignments:
 `+=  -=  *=  /=  &=  |=` _Do we really need them?_
#### Function call
```
function arg1 arg2 arg3
[function-without-arguments]
```
Whenever needed and possible IDE displays arguments' names, but you don't need to type them:
 ```
move-to x: 100 y: 200
withdraw order: current-order amount: payment
```
It should be possible to declare argument with a default value.
On the call site arguments with default values can be hidden for brevity and better readability.
Nested calls
```
foo [bar [baz x]]
```
So far it's pretty similar to Lisp's S-expressions except to using square brackets instead of parenthesis and omitting brackets for top level forms.
The latter is to reduce "syntactic noise", but why [] instead of () ? I think, brackets are more readable and easy to match; they are also easier to type: you don't have to press `Shift`.
Don't be mistaken, this is not Lisp!

Too many nested calls? Here comes indentation-based syntax:
```
head
    filter
        map
            items
            lambda [x] [+ x 1]	 
        lambda [x] [> x 0]					

```
#### Method calls
Method calls look exactly like function calls, they are just dispatched in a slightly different way.

`cancel order` - calls method `cancel` of object `order`

Q: What if I have both method and function called `cancel`, which will be called?

A: Code is not text! Everything including functions and methods is referenced not by name but by object reference. At the moment you type `cancel` you will be prompted to pick one method or function called `cancel`.
 A reference to the chosen method or function will determine what should be called. The same is true for functions from different packages.

#### Arithmetic and relational operators
They're just functions
- `+ - * / < > == != >= <=`
- `\` _or `div`?_ integer division 
- `%` _or `mod`?_ remainder

#### Boolean operators
In contrast, boolean operators are not mere functions. All boolean operators are short-circuited and use lazy evaluation.
_Should we support lazy evaluation of function parameters for "normal" functions?_
The only exception is `!` (not) - it's just a simple eager function.
So, here are boolean operators: 
`& | ^ !` _or maybe `and or xor not`?_

For example:
```
= in-range lambda [x x1 x2]
   & [< x1 x] [<= x x2]
```
### Bitwise operators
They are just functions with names like `bitand32`
#### return

```
return <value>
```
or just
```
return
```
(returns `null`)
_Should we use inlined `return + 2 3` instead of `return [+ 2 3]`?_

 
#### if, else and elif
```
if <condition>
    <statement>
    ...
    <statement>
elif <condition>
    <statement>
    ...
    <statement>
else
    <statement>
    ...
    <statement>
```
#### cond
cond is a simplified form of if-else:
`= sign cond [> x 0] 1 [< x 0] -1 0`
#### while
```
while <condition>
    <statement>
    ...
    <statement>
```
#### for
```
for <variable> <iterable>
   <statement>
   ...
   <statement>
```

```
for i range 0 n
   print i
```
#### break, continue
#### throw, try, catch, finall
#### Lambdas
```
lambda [<argument>...]
    <statement>
    ...
    <statement>
```
_Should we mayby use λ for lambdas? Could be typed with `\`_
#### case
TBD
#### No more synatx!
No more syntax is needed.
Packages, function and method declarations, comments, visibility modifiers, imports, etc are all done by IDE. 


