package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlin.math.exp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // textViews
        val input = findViewById<TextView>(R.id.input)
        val result = findViewById<TextView>(R.id.result)

        // defined buttons in a list
        val buttons = listOf<Button>(
            findViewById<Button>(R.id.numberZero), //0
            findViewById<Button>(R.id.numberOne), //1
            findViewById<Button>(R.id.numberTwo), //2
            findViewById<Button>(R.id.numberThree), //3
            findViewById<Button>(R.id.numberFour), //4
            findViewById<Button>(R.id.numberFive), //5
            findViewById<Button>(R.id.numberSix), //6
            findViewById<Button>(R.id.numberSeven), //7
            findViewById<Button>(R.id.numberEight), //8
            findViewById<Button>(R.id.numberNine), //9
            findViewById<Button>(R.id.operatorSign), //10
            findViewById<Button>(R.id.decimalPoint), //11
            findViewById<Button>(R.id.operatorPlus), //12
            findViewById<Button>(R.id.operatorMinus), //13
            findViewById<Button>(R.id.operatorMultiply), //14
            findViewById<Button>(R.id.operatorDivide), //15
            findViewById<Button>(R.id.operatorPercentage), //16
            findViewById<Button>(R.id.symbolParenthesis), //17
            findViewById<Button>(R.id.operatorEquals), //18
            findViewById<Button>(R.id.functionClear), //19
            findViewById<Button>(R.id.backspace) // 20
        )

        val calc = Calculator(buttons, input.text.toString(), result.text.toString())

        // detect button press and add text to input
        for (i in 0 until 18){
            buttons[i].setOnClickListener(){
                calc.add_character(buttons[i].text.toString())
                input.text = calc.input
                result.text = calc.result
            }
        }

        buttons[18].setOnClickListener{
            input.text = result.text
            result.text = ""
        }
        buttons[19].setOnClickListener(){
            input.text = ""
            result.text = ""
            calc.clear()
        }
        // backspace
        buttons[20].setOnClickListener{
            if (input.text.length != 0) {
                if (input.text.length == 1) {
                    calc.clear()
                    input.text = ""
                    result.text = ""
                } else {
                    calc.input = calc.input.dropLast(2)
                    calc.add_character(input.text[input.text.length - 2].toString())
                    input.text = calc.input
                    result.text = calc.result
                }
            }
        }
    }
}

class Calculator(buttons_lst: List<Button>, p_input: String, p_result: String) {

    var input = p_input
    var result = p_result
    val buttons = buttons_lst
    var input_num: Float = 0.0f
    var answer_num: Float = 0.0f

    fun add_character(new: String){
        // if new and last is operator return or input is empty
        if (new.toDoubleOrNull() == null && (input.isEmpty() || input[input.length-1].toString().toDoubleOrNull() == null)){
            return
        }
        // add to input
        input = "$input$new"
        // if one element or last is operator input appended but don't calculate
        if (input.length == 1 || input[input.length-1].toString().toDoubleOrNull() == null){
            return
        }
        // if there are no operators don't calculate, if operators calculate
        var has_operator = false
        for (item in input){if (item.toString().toDoubleOrNull() == null && item != '.'){has_operator = true}}
        if (has_operator){calculate()}
        return
    }

    fun percentage(){

    }

    fun brackets(){

    }

    fun clear(){
        input = ""
        result = ""
    }

    // takes input as a list of strings and carries out calculation
    // start off simple cause it is probably hard to do it correct ordering
    fun calculate(): String{

        // 3x6/9+5/

        // use https://www.digitalocean.com/community/conceptual-articles/understanding-order-of-operations#what-is-postfix-notation
        // change to postfix and then evaluate, call this whenever a something should be updated, so if num+num

        // take input and add into expression list
        val expression= mutableListOf<String>()
        Log.i("ListLen", expression.size.toString())

        // add all elements in, combining numbers
        var pos = 0
        for (i in input.indices){
            val cur = input[i].toString()

            // check for number or period
            if ((cur.toDoubleOrNull() != null || cur == ".") && expression.isNotEmpty()){
                if (expression[pos].toDoubleOrNull() != null){
                    expression[pos] += cur
                    continue
                }
            }
            if (expression.isNotEmpty()) {
                pos++
            }
            expression.add(cur)
        }
        // if last char in element is '.', add a zero so the conversion is understood
        for (item in expression){
            if (expression[pos][expression[pos].length-1] == '.'){
                expression[pos] += "0"
            }
        }

        val infix = infix_to_postfix(expression)
        result = evaluate_postfix(infix)

        return result
    }

    fun infix_to_postfix(expression: List<String>): List<String>{
        // now convert from infix to post fix
        val results = mutableListOf<String>()
        val operator_stack = mutableListOf<String>()

        for (item in expression){
            // add numbers to result queue
            if (item.toDoubleOrNull() != null){
                results.add(item)
                continue
            }

            var done: Boolean = false
            fun recurse() {
                // base case I guess
                if (done){
                    return
                }
                if (operator_stack.isEmpty()){
                    operator_stack.add(item)
                    done = true
                    return
                }
                // based of precedence
                val pre_one = precendence(item)
                val pre_two = precendence(operator_stack[operator_stack.size-1])

                if (pre_one > pre_two){
                    operator_stack.add(item)
                    done = true
                    return
                }
                else if (pre_one == pre_two){
                    results.add(operator_stack[operator_stack.size-1])
                    operator_stack[operator_stack.size-1] = item
                    done = true
                    return
                }
                else if (pre_one < pre_two) {
                    results.add(operator_stack[operator_stack.size-1])
                    operator_stack.removeAt(operator_stack.size-1)
                    recurse()
                }
            }
            recurse()
        }

        // add rest of elements to results
        for (elem in operator_stack){
            results.add(elem)
        }
        operator_stack.clear()

        return results
    }

    fun evaluate_postfix(express: List<String>): String{

        val stack = mutableListOf<String>()

        for (cur in express){
            if (cur.toDoubleOrNull() != null){
                stack.add(cur)
            }
            else if (cur.toDoubleOrNull() == null){
                val one = stack[stack.size-1].toFloat()
                val two = stack[stack.size-2].toFloat()
                stack.removeAt(stack.size-1)
                stack.removeAt(stack.size-1)

                // basic operators for the moment
                when (cur) {
                    "+" -> {
                        stack.add((one + two).toString())
                    }
                    "-" -> {
                        stack.add((two - one).toString())
                    }
                    "×" -> {
                        stack.add((one * two).toString())
                    }
                    "÷" -> {
                        stack.add((two / one).toString())
                    }
                }
            }
        }
        if (stack.size == 1){
            return stack[0]
        }
        return "calculation error"
    }


    fun precendence(operator: String): Int{
        if (operator == "+" || operator == "-") {
            return 0
        }
        if (operator == "×" || operator == "÷") {
            return 1
        }
        // shouldn't be reached once all implemented
        return 0
    }
}