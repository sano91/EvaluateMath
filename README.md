#Evaluate mathematical expression
###Instructions
Given a mathematical expression as a string you must return the result as a number.

###Numbers
Number may be both whole numbers and/or decimal numbers. The same goes for the returned result.

##Operators
You need to support the following mathematical operators:

* Multiplication *
* Division / (as true division)
* Addition +
* Subtraction -

Operators are always evaluated from left-to-right, and * and / must be evaluated before + and -.

###Parentheses
You need to support multiple levels of nested parentheses, ex. (2 / (2 + 3.33) * 4) - -6

###Whitespace
There may or may not be whitespace between numbers and operators.

>An addition to this rule is that the minus sign (-) used for negating numbers and parentheses will never be separated by whitespace. 

##My approach
**I cut the task to smaller and smaller pieces recursively until just the result left.**
####Solution steps in nutshell
1. Organize and trim the expression string to a list.
2. Search parenthesis
3. Search the strongest scope --> () > *,/ > +,-
4. Evaluate the signs before the second number --> 28.7 --+--- 7 -> 28.7 - 7
5. Calculate this scope
6. Replace the scope for the result of the scope in the scope above --> 3 * (28.7-7) -> 3 * 21.7
7. As it goes recursively it bubbling up the numbers as results of the scopes.
8. As it reach the main expression list, it's reduce it's size every time until only one number is left which is the result of the expression.
 
