# The Ludwig Programming Language
[![Build Status](https://api.travis-ci.org/strangepleasures/foo.svg?branch=master)](https://travis-ci.org/strangepleasures/foo/branches)

Ludwig is called after Ludwig Wittgenstein, one of the most influential philosophers and logicians of the 20th century.
The key idea of his _Tractatus Logico-Philosophicus_ - "what can be said at all can be said clearly, and what we cannot talk about we must pass over in silence" is very consonant with our intention to put all the irrelevant stuff behind the scene and to provide a simple and powerful tool for expressing one's thoughts.

# Basic principles
The aim of this project is to create a new software development platform, combining a very simple but powerful programming language in the spirit of Lisp and Smalltalk with a revolutionary IDE concept. 

Most of technical decisions in Ludwig come from a combination of three key ideas:
- Unification of functional and object oriented paradigms.
- Structural representation of code.
- Event sourcing as the basis of the development tools stack.

# Unification of functional and object oriented paradigms
Object-oriented and functional programming paradigms have been long thought to be complementary if not mutually exclusive ways of thinking.
The two most exemplary languages representing the two approaches, Lisp and Smalltalk were separated by birth and that was probably the biggest schism in the history of computer science! (Another one is between dynamically typed and strongly typed languages and we're also trying to mitigate it in Ludwig).
The similarity between the two have been many times declared by the creators of Smalltalk and Lisp but the two traditions have diverged too far since then.
Neither introduction of small islands of OOP in Lisp and functional programming in Smalltalk nor creation of multi-paradigm languages combining two ecosystems and, in fact, two sub-languages solved the problem. 
An effort to unite the two worlds is probably the most ambitious goal of Ludwig.
Again, it means not creating just another multi-paradigm (=chimerical) language, but making object-oriented and functional styles indistinguishable by introducing one more generic way of thinking.

# Structural representation of code
First computer programs were written by hand, later stored on punched cards and then, finally, in text files. The design of the traditional programming langages is heavily influenced by the fact the first programs were stored in text files. 
Until now, creation of new programming languages typically starts with designing of a new textual notation (syntax) and writing a parser for it.
In fact every programming language supports multiple styles for expressing the same code structure, varying in line wrapping, number of spaces used for indentation, different styles of comments, etc.
Such subtle difference between different styles provoke endless discussions ("holy wars") between their proponents.

However code is not text!
Code consists of abstractions, such as functions and types and can be represented as a graph of objects. In fact, all professional software developers think in terms of abstractions and just use textual representation as a notation to express their ideas. _"Mathematicians think in symbols, physicists in objects, philosophers in concepts, geometers in images, jurists in constructs, logicians in operators, writers in impressions, and idiots in words." - Nassim Taleb_
As in mathematics, one abstract construction can be expressed in potentially infinite number of different notations.

Another consideration is that nowdays software developers always work in IDE, and support of language features from IDE including autocompletion, refactoring and validation of code is crucial for programming language's ease of use and popularity. 
This means that design of modern programming languages must be IDE-friendly, even IDE-driven.
In fact, all modern IDEs always create internal (in-memory) object representation of code or even some kind of indexed database and use object files just as a storage between editing sessions.
It typically takes some time for IDE to build such a representation of code.

In Ludwig we try radically change the approach by defining the language in terms of abstractions instead of tied to textual representation text grammar rules.
Abstract code constructs are **stored** in RAM or an external database in an indexed format best suited for lookup and refactoring and hidden from the user and can be **displayed** in different ways.
Instead of being tied to static textual representation, modern IDE should be able to display code in a dynamic and customizable way, using additional graphic markup such as icons in addition to textual representation.
Think about sorting and filtering of classes and methods, showing and hiding comments and on-the-fly formatting of code according to your personal preferences.
So far the closest approach to this was Smalltalk environments which represented code in structured way but still used textual representation for method bodies and stored stored whole codebase in text files. 

# Event sourcing as the basis of the development tools stack
The third idea naturally originates from storing code in structured way. Being free of preconception of saving code in text files, we can choose the most convenient persistent format and include any kind of metadata we need.
More than that, we can always keep the full editing history without using any extra tools such as Git or Subversion. We can even put the storage into cloud and make it serve as a dependency management system as well.
Each branch of code can be represented as a sequence of elementary changes. When IDE loads a project it just starts with an empty workspace and subsequently applies all the changes. Merging of branches is as natural as merging of two lists. More than that, you can always replay the whole editing history and see your project evolution!
Another cool thing about this model is that structural changes tend to be local. Renaming of a method or moving it to another class changes only one string or reference. All the usages are just references to the method and update automatically. Renaming your favorite method's name changes one and only one string constant in your code. I don't think, it should be programing with a mouse, I would call it assisted editing (more than just autocompletion).

## The secondary principles
### Syntax
Ludwig's syntax is almost invisible. It's based on prefix Polish (Łukasiewicz) notation and uses Python-style indentation for grouping and improved readability.
Actually it pretty much resembles well-formatted Lisps syntax... without parenthesis and any special symbols.
Yes, it doesn't use parenthesis, reserved words, precedence rules, commas, semicolons, curly or square brackets.
More than that, many usual elements of a programming language such as comments, type annotations, import statements, class and function declarations or visibility modifiers, are not a part of the language in Ludwig.
Instead Ludwig IDE provides a GUI with case-specific editors for each element of the language.
Ludwig doesn't make a distinction between "static" functions and object methods, properties and fields and use the same syntax for all. In Ludwig everything is an object and almost everything is a function.
### Static typing
Ludwig is a statically typed language with a powerful type system supporting generics, union and intersection types etc. That allows developers to benefit from all kinds of services provided by IDE.
However, the **feeling** of using Ludwig is very similar to that of dynamic languages. Ludwig relies on local type inference and in most cases requires type annotations only in function declarations.
### There's only one way to do it right
Unlike to languages like Scala or Ruby, there's usually only one preffered way to do routine tasks in Ludwig. Again, Ludwig unifies functional and object-oriented paradigms, so you don't have to choose between them. Also, typically Ludwig encourages you to prefer immutable abstract data types over mutable ones.
### Incremental compilation
Due to the structured nature of Ludwig's code, a single method is the minimal unit of compilation. As long as method's signature remains the same, changes in one method doesn't require recompilation of other methods.
### Ludwig is a high-level language
Although we cant foresse all the possible applications of Ludwig, it's supposed to be a universal language. It's not meant to be used for scriptiong or writing hardware drivers.
Due to its structured nature and code in database approach it should be well suited for large projects. Its simplisity and excellent support from IDE should make it a very simple language to learn and an excellent tool for educational projects.
### (Almost) ready to production
We're going to give it a quick start, it means that Ludwig should be able to utilize existing software technologies. Ludwig programs are supposed to run within managed application containers, including cloud-based solutions.
Ludwig programs should be easily portable between different containers. We're not going to implement for example HTTP stack from scratch, that would take years. That also means that at least the version the language will come without low-level support for concurrent programming.
 

## The NOTs or what shouldn't be a part of the language
- Not a multi-paradigm language. Ludwig uses only a very limited set of base concepts and language constructs.
- No macros. Although some people consider macros to be the most powerful feature of Lisp, they are hard to learn, hard to read, hard to support by IDE and lead to fragmentation of the language by invention of new sublanguages.
- More generally, no magic. Ludwig's philosophy denies any kind of implicit behavior or coding by convention. Dependency injection is a part of the language and has very simple semantics. 
- No syntax sugar.
- No shadow worlds. Typically programming languages consist of multiple sublanguages: besides "the core" language, there's one for regular expressions, one for string literals, documentation comments etc. In Ludwig there's only one language, all the others are a part of IDE tooling and can evolve with it from version to version.
- Not a universal language. Ludwig is a high-level and abstract programming language and is not supposed to be used for scripting or writing hardware drivers.   
