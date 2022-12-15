package calculator

import java.math.BigInteger

class InvalidExpression : Exception()
class InvalidIdentifier : Exception()
class InvalidAssignment : Exception()
class UnknownCommand : Exception()
class UnknownVariable : Exception()

class Variable(var name: String, var number: BigInteger)

class CalculatorVariables {
    private val variablesList = mutableListOf<Variable>()

    fun checkVariable(vName: String): Pair<Boolean, BigInteger> {
        for (v in variablesList) {
            if (v.name == vName) {
                return Pair(true, v.number)
            }
        }
        return Pair(false, BigInteger("0"))
    }

    fun setVariable(vName: String, vNumber: BigInteger) {
        for (v in variablesList) {
            if (v.name == vName) {
                v.number = vNumber
                return
            }
        }
        variablesList.add(Variable(vName, vNumber))
    }
}

/**
 * Calculates two numbers with given operator to construct the result from postfix form
 */
fun calculateTwoNumbersFromPostfix(num1: BigInteger, num2: BigInteger, operator: String): BigInteger? {
    var result: BigInteger? = null
    when (operator) {
        "*" -> {
            result = num1 * num2
        }
        "/" -> {
            result = num1 / num2
        }
        "+" -> {
            result = num1 + num2
        }
        "-" -> {
            result = num1 - num2
        }
    }
    return result
}

/**
 * Converts given list of numbers and operators to calculable postfix form
 * and calculates the final result from postfix
 */
fun cleanCalculations(list: MutableList<String>): BigInteger? {
    var result: BigInteger? = null
    try {
        // converting infix to postfix form
        val postfixList = mutableListOf<String>()
        val stack = mutableListOf<String>()
        for (v in list) {
            if (v.toBigIntegerOrNull() != null) {
                postfixList.add(v)
            } else {
                if (stack.isEmpty()) {
                    stack.add(v)
                } else {
                    if (v == "*" || v == "/") {
                        if (stack.last() == "+" || stack.last() == "-" || stack.last() == "(") {
                            stack.add(v)
                        } else if (stack.last() == "*" || stack.last() == "/") {
                            postfixList.add(stack.last())
                            stack.removeLast()
                            stack.add(v)
                        }
                    } else if (v == "+" || v == "-") {
                        for (s in stack.size - 1 downTo 0) {
                            if (stack[s] == "(") break
                            postfixList.add(stack[stack.size - 1])
                            stack.removeLast()
                        }
                        stack.add(v)
                    } else if (v == "(") {
                        stack.add(v)
                    } else if (v == ")") {
                        for (s in stack.size - 1 downTo 0) {
                            val value = stack[s]
                            if (stack[s] != "(" && stack[s] != ")") postfixList.add(stack[stack.size - 1])
                            stack.removeLast()
                            if (value == "(") break
                        }
                    }
                }
            }
        }
        if (stack.isNotEmpty()) {
            for (s in stack.indices) {
                postfixList.add(stack[stack.size - 1])
                stack.removeLast()
            }
        }

        // calculating from postfix
        for (v in postfixList) {
            stack.add(v)
            var operator: String
            var num1: String
            var num2: String
            if (stack.size >= 3 &&
                (stack.last() == "+" || stack.last() == "-" || stack.last() == "*" || stack.last() == "/") &&
                stack[stack.size - 2].toBigIntegerOrNull() != null &&
                stack[stack.size - 3].toBigIntegerOrNull() != null) {
                operator = stack.last()
                num2 = stack[stack.size - 2]
                num1 = stack[stack.size - 3]
                val triple = calculateTwoNumbersFromPostfix(num1.toBigInteger(), num2.toBigInteger(), operator).toString()
                stack.removeLast()
                stack.removeLast()
                stack.removeLast()
                stack.add(triple)
            } else {
                continue
            }
        }
        if (stack.size == 1) {
            result = stack[0].toBigInteger()
        } else if (stack.size == 2 && stack.last() == "-") {
            result = "-${stack[0]}".toBigInteger()
        }
    } catch (e: Exception) {
        println("Calculation goes wrong")
    }
    return result
}

/**
 * Checks input string for wrong symbols
 */
fun processInput(input: String, vars: CalculatorVariables) {
    try {
        // checking for digits, letters, operators, brackets
        var leftBrackets = 0
        var rightBrackets = 0
        for (c in input) {
            if (!c.isLetter() && !c.isDigit() && !c.isWhitespace() &&
                c != '+' && c != '-' && c != '=' && c != '/' && c != '*' && c!='(' && c!= ')') {
                throw InvalidExpression()
            }
            if (c == '(') {
                leftBrackets++
            } else if (c == ')') {
                rightBrackets++
                if (rightBrackets > leftBrackets) throw InvalidExpression()
            }
        }
        if (leftBrackets != rightBrackets) throw InvalidExpression()

        // wrong command reaction
        if ("""/.*""".toRegex().matches(input)) {
            throw UnknownCommand()
        // wrong input
        } else if (""".*\w+\s+\w+.*""".toRegex().matches(input) ||       // space between numbers or variables
            """.*[=\-+*/]\s*""".toRegex().matches(input) ||              // expression ends with operator
            """\s*[-=+]\s*[a-zA-Z]+\s*=.*""".toRegex().matches(input) || // operators before variable and assignment
            """.*[+\-*/=(]+\s*[/*]+.*""".toRegex().matches(input) ||     // "*" or "/" after =, operators, open bracket
            """.*[(]+\s*[)]+.*""".toRegex().matches(input) ||            // empty brackets
            """\s*[*/]+.*""".toRegex().matches(input) ||                 // expression starts with "*" or "/"
            """.*[*/=]+\s*[-+]+\s*[(].*""".toRegex().matches(input)      // "-" between "*", "/", "=" and brackets
        ) {
            throw InvalidExpression()
        // wrong input: variables with digits
        } else if (""".*[a-zA-Z]+\d+\s*=.*""".toRegex().matches(input) ||
            """.*\d+[a-zA-Z]+\s*=.*""".toRegex().matches(input) ||
            """[a-zA-Z]+\d+\w*""".toRegex().matches(input) ||
            """\d+[a-zA-Z]+\w*""".toRegex().matches(input)
        ) {
            throw InvalidIdentifier()
        // wrong input: more than one "=" or variables with digits after "="
        } else if ("""=""".toRegex().findAll(input).count() > 1 ||
            """.*=\s*[a-zA-Z]\d+.*""".toRegex().matches(input) ||
            """.*\s*\d+[a-zA-Z]+.*""".toRegex().matches(input)
        ) {
            throw InvalidAssignment()
        }

        // removing spaces from input string
        val inputWoSpace = input.filter { !it.isWhitespace() }

        // converting string to list with numbers, operators and variables
        val listValues = stringToList(inputWoSpace)

        // resolving several +/- operators
        for (v in listValues.indices) {
            if (listValues[v].contains("+") || listValues[v].contains("-")) {
                listValues[v] = resolveOperator(listValues[v])
            }
        }

        // converting known variables to numbers
        if (listValues.contains("=")) {
            for (v in listValues.indexOf("=") + 1 until listValues.size) {
                if ("""[a-zA-Z]+""".toRegex().matches(listValues[v])) {
                    if (!vars.checkVariable(listValues[v]).first) throw UnknownVariable()
                    listValues[v] = vars.checkVariable(listValues[v]).second.toString()
                }
            }
        } else {
            for (v in listValues.indices) {
                if ("""[a-zA-Z]+""".toRegex().matches(listValues[v])) {
                    if (!vars.checkVariable(listValues[v]).first) throw UnknownVariable()
                    listValues[v] = vars.checkVariable(listValues[v]).second.toString()
                }
            }
        }

        // including "+-" to numbers
        try {
            var index = 0
            do {
                if ((index == 0 && ((listValues[index] == "+") || (listValues[index] == "-"))) ||
                    (index > 0 && ((listValues[index] == "+") || (listValues[index] == "-")) &&
                            (listValues[index - 1] == "="
                            || listValues[index - 1] == "*"
                            || listValues[index - 1] == "/"
                            || listValues[index - 1] == "("))) {
                    if (listValues[index] == "-") {
                        val nextNumber = listValues[index + 1]
                        listValues[index + 1] = "-$nextNumber"
                    }
                    listValues.removeAt(index)
                }
                index++
            } while (index < listValues.size)
        } catch (e: Exception) {
            println("Including \"+-\" to numbers goes wrong")
        }

        // assignment: checking for "=" operator and variable at first place
        if (listValues.contains("=")) {
            val varName = listValues[0]
            if (listValues[1] != "=" || !varName.contains("""[a-zA-Z]+""".toRegex())) {
                throw InvalidExpression()
            }
            // creating sublist of equation after "="
            val listWoEq = listValues.subList(2, listValues.size)
            // calculating the result
            val result = cleanCalculations(listWoEq)
            // assignment of variable
            if (result != null) vars.setVariable(varName, result)
        } else {
            println(cleanCalculations(listValues))
        }

    } catch (e: InvalidExpression) {
        println("Invalid expression")
    } catch (e: UnknownCommand) {
        println("Unknown command")
    } catch (e: InvalidIdentifier) {
        println("Invalid identifier")
    } catch (e: InvalidAssignment) {
        println("Invalid assignment")
    } catch (e: UnknownVariable) {
        println("Unknown variable")
    }
}

/**
 * Joins several operators in a row, return mathematically correct + or -
 */
fun resolveOperator(line: String): String {
    var minusCount = 0
    for (c in line) {
        if (c == '-') {
            minusCount++
        }
    }
    return if (minusCount > 0 && minusCount % 2 != 0) "-" else  "+"
}

/**
 * Converts string to a list with numbers, operators and variables
 */
fun stringToList(input: String): MutableList<String> {
    val list = mutableListOf<String>()
    var letter = ""
    var digit = ""
    var operator = ""
    for (c in input) {
        if (c.isLetter()) {
            if (digit.isNotEmpty()) list.add(digit)
            digit = ""
            if (operator.isNotEmpty()) list.add(operator)
            operator = ""
            letter += c
        } else if (c.isDigit()) {
            if (letter.isNotEmpty()) list.add(letter)
            letter = ""
            if (operator.isNotEmpty()) list.add(operator)
            operator = ""
            digit += c
        } else if (c == '=' || c == '*' || c == '/' || c == '(' || c == ')') {
            if (letter.isNotEmpty()) list.add(letter)
            letter = ""
            if (digit.isNotEmpty()) list.add(digit)
            digit = ""
            if (operator.isNotEmpty()) list.add(operator)
            operator = ""
            list.add(c.toString())
        } else if (c == '+' || c == '-') {
            if (letter.isNotEmpty()) list.add(letter)
            letter = ""
            if (digit.isNotEmpty()) list.add(digit)
            digit = ""
            operator += c
        }
    }
    if (letter.isNotEmpty()) list.add(letter)
    if (digit.isNotEmpty()) list.add(digit)
    return list
}

/**
 * Calculates the sum of any integer values provided in any reasonable form
 */
fun calculator() {
    val vars = CalculatorVariables()
    do {
        try {
            val userInput = readln()
            // empty input reaction
            if (userInput.isEmpty() || """\s+""".toRegex().matches(userInput)) {
                continue
            // help command reaction
            } else if (userInput == "/help") {
                println("The program calculates the arithmetic operations with given numbers")
            // exit command reaction
            } else if (userInput == "/exit") {
                println("Bye!")
                break
            } else {
                processInput(userInput, vars)
            }
        } catch (e: Exception) {
            println("Exception!")
        }
    } while (true)
}

fun main() {
    calculator()
}