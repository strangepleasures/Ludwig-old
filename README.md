# Foo
[![Build Status](https://api.travis-ci.org/strangepleasures/foo.svg?branch=master)](https://travis-ci.org/strangepleasures/foo/branches)

The aim of this project is to create a completely new way of programming.
It's going to be a new programming language and a new IDE concept and even more. 

Probably the key idea is first-class structural representation of code. No text files, code is not text! It is a graph of objects and it's very natural for programmers to think about code in this way.
Now-days we always work in IDE, programming language design must be IDE-friendly, even IDE-driven.
Your code shall be always under version control.
Dependency management is a first-class feature and should work out-of-the-box.
Everybody hates verbosity and programming should be absolutely intuitive for newbies. You should never start with `public static void main(String[] args)`.

So code should be represented in a structured, dynamic and customizable way. Think about sorting and filtering of classes and methods, showing and hiding comments and formatting of code in your favorite way! Code exists in your IDE as huge graph of objects. It's not jus a syntax tree, at any moment it has all references resolved.

The persistent representation of the code contains the full editing history. It is just a list of elementary structural changes, e.g. add a parameter to a method, rename a class, etc. The list of changes is only appended, so we never loose the history.
When you load a project you just start with an empty workspace and subsequently apply all changes. Merging of branches is as natural as merging of two lists. More than that, you can always replay the whole editing history and see your project evolution!
Another cool thing about this model is that structural changes tend to be local. Renaming of a method or moving it to another class changes only one string or reference. All the usages are just references to the method and update automatically. Renaming your favorite method's name changes one and only one string constant in your code. I don't think, it should be programing with a mouse, I would call it assisted editing (more than just autocompletion).

Again, code is structure! Every single structural element has a global unique identifier (GUID). Your project has a GUID. Even every version of a project has a GUID. Hmm... it's so easy to implement dependency management now!

Structural editing should benefit from first class support by IDE. Python-like indentation based syntax gets rid of all distractions.
There's no need to have special syntax for class and method declarations and documentation (think about javadoc), structural editing provided by IDE makes syntax literally invisible - you  don't need to remember the right order of modifiers in public static final if they all are just clickable icons in your method's header. To add a method you click + button, fill-in the name add arguments (there's always a placeholder for documentation as well). No tool windows or modal dialogs all in-place. Method marked with a red dot is private, method with a green is public.

The language how I see it is heavily inspired by Lisp, Smalltalk and Python.
The whole thing should look like Smalltalk's class browser with Pythonic syntax and Lisp's support for functional programming.

The syntax should be simple, uniform (Lisp's S-expressions are heavily contaminated with syntax sugar), easy to learn and almost invisible! We want to have seamless integration of functional and object oriented programming - object's method calls will look exactly like regular function calls, just dispatched slightly differently. Later on we will also add optional typing and type inference to the simplicity of the base language. 

We can use syntax similar to the indentation-based syntax for Scheme:

```
define
   fac x
   if
    = x 0
    1
    * x
      fac
       - x 1
```