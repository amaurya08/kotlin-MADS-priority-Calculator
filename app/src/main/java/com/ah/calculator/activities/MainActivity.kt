package com.ah.calculator.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ah.calculator.CalItem
import com.ah.calculator.R
import com.ah.calculator.databinding.ActMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var transRef: DatabaseReference
    private lateinit var binding: ActMainBinding
    private var infixString: String = ""
    private var answerString: String = ""
    private val TAG: String = "TAG"
    private var allCalculation: ArrayList<CalItem> = ArrayList()

    private var calculationResult: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActMainBinding.inflate(LayoutInflater.from(this))
        transRef = FirebaseDatabase.getInstance().getReference("users")
            .child("${FirebaseAuth.getInstance().currentUser?.uid}").child("all")
        setContentView(binding.root)
        listneres()
        transRef.addValueEventListener(postListener)
    }

    val postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            Log.d(TAG, dataSnapshot.toString())
            allCalculation.clear()
            for (ds: DataSnapshot in dataSnapshot.children) {
                //val item: CalItem? = ds.getValue(CalItem::class.java)
                Log.d(TAG, "onmark " + ds.toString())
                val item: CalItem? = ds.getValue(CalItem::class.java)
                if (item != null) {
                    allCalculation.add(item)
                }
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
        }
    }

    private fun listneres() {
        binding.backspace.setOnClickListener {
            //get text from infixVIew
            backspace()
        }
        binding.backspace.setOnLongClickListener {
            clearInfixString()
            return@setOnLongClickListener true
        }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut();
            var i = Intent(this, Login::class.java);
            startActivity(i)
            finish()
        }

    }

    private fun backspace() {
        if (infixString.isEmpty())
            return
        infixString = binding.tvInfix.text.toString()
        Log.d(TAG, infixString)
        //remove last char
        infixString = infixString.subSequence(0, infixString.length - 1).toString()
        //setText to infixview
        updateInxifField(infixString)
    }

    fun onNumericClick(view: View) {
        val tv: Button = view as Button
        Log.d("DEBUG", "pressed button is " + tv.text)
        //Toast.makeText(this, "oh yes", Toast.LENGTH_LONG).show()
        when (tv.id) {
            //history
            R.id.tv_H -> {
                var i = Intent(this, History::class.java);
                startActivity(i)
            }
            //Last Answer
            R.id.tv_A -> {
                infixString += answerString
                updateInxifField(infixString)
            }
            //Clear All
            R.id.tv_clear -> {
                Log.d("DEBUG", "C")
                //clear result view
                clearInfixString()
                clearResult()
                //clear infix string
            }
            //Calculate Result
            R.id.equals -> {
                if (!isArtihSym(infixString[infixString.length - 1]))
                    calculate()
            }
            else -> {
                val symbol: String = tv.text.toString()
                if (infixString.isNotEmpty()) {
                    if (validSymbol(symbol[symbol.length - 1])) {
                        infixString += (tv.text.toString())
                        updateInxifField(infixString)
                    }
                } else {
                    infixString += (tv.text.toString())
                    updateInxifField(infixString)
                }
            }
        }
    }


    private fun checkPrecedence(c: Char): Int {
        when (c) {

            '-' -> {
                return 1
            }

            '/' -> {
                return 2
            }

            '+' -> {
                return 3
            }

            'x' -> {
                return 4
            }
            else -> return 0
        }
    }

    private fun infixTOPostFix(infix: String): ArrayList<String> {
        var postFix: ArrayList<String> = ArrayList()
        var arr = tokenize(infix) //arrayOf("20", "-", "2", "x", "10")
        val characterStack: Stack<Char> = Stack()
        characterStack.push('#')
        for (op in arr) {
            if (op.length == 1 && isArtihSym(op[0])) {
                val sym: Char = op[0]
                //its definitely a operator
                if (checkPrecedence(sym) > checkPrecedence(characterStack.peek())) {
                    characterStack.push(sym)
                } else {
                    while (characterStack.peek() != '#' && checkPrecedence(sym) <= checkPrecedence(
                            characterStack.peek()
                        )
                    ) {
                        postFix.add(characterStack.pop().toString())
                    }
                    characterStack.push(sym)
                }
            } else {
                //its a number
                postFix.add(op)
            }
        }
        while (characterStack.peek() != '#') {
            postFix.add(characterStack.pop().toString())
        }
        return postFix
    }

    private fun evalPostFix(arr: ArrayList<String>): String {
        val stack: Stack<String> = Stack()
        for (item: String in arr) {
            when (item) {
                "+" -> {
                    var num1 = 0.0
                    var num2 = 0.0

                    if (stack.isNotEmpty()) {
                        num1 = stack.pop().toDouble()
                    }
                    if (stack.isNotEmpty()) {
                        num2 = stack.pop().toDouble()
                    }

                    stack.push((num2 + num1).toString())
                }
                "-" -> {
                    var num1 = 0.0
                    var num2 = 0.0

                    if (stack.isNotEmpty()) {
                        num1 = stack.pop().toDouble()
                    }
                    if (stack.isNotEmpty()) {
                        num2 = stack.pop().toDouble()
                    }

                    stack.push((num2 - num1).toString())
                }
                "/" -> {
                    val num1 = stack.pop().toDouble()
                    val num2 = stack.pop().toDouble()
                    if (num1 == 0.0) {
                        return "NAN"
                    }
                    stack.push((num2 / num1).toString())
                }
                "x" -> {
                    val num1 = stack.pop().toDouble()
                    val num2 = stack.pop().toDouble()
                    stack.push((num1 * num2).toString())
                }
                else -> {
                    stack.push(item)
                }
            }
        }
        return stack.pop().toString()
    }

    private fun calculate() {
        answerString = "123"
        //do actual calculation
        val a = infixTOPostFix(infixString)
        answerString = evalPostFix(a)
        updateResult(answerString)

        if (allCalculation.size == 10) {
            allCalculation.removeAt(0)
        }
        allCalculation.add(CalItem(infixString, answerString))
        transRef.setValue(allCalculation)
    }

    private fun updateInxifField(s: String) {
        binding.tvInfix.text = s
    }

    private fun updateResult(s: String) {
        binding.tvAnswer.text = s
    }

    private fun clearInfixString() {
        infixString = ""
        updateInxifField(infixString)
    }

    private fun clearResult() {
        calculationResult = ""
        updateResult(calculationResult)
    }

    private fun validSymbol(c: Char): Boolean {
        val lastSymbol = infixString[infixString.length - 1]
        return !(isArtihSym(c) && isArtihSym(lastSymbol))
    }

    private fun isArtihSym(c: Char): Boolean {
        when (c) {
            '+' -> {
                return true
            }
            '-' -> {
                return true
            }
            'x' -> {
                return true
            }
            '/' -> {
                return true
            }
            else -> return false
        }
    }

    private fun tokenize(input: String): List<String> {
        var tokens: ArrayList<String> = ArrayList()
        var sym: String = ""
        for (c: Char in input) {
            if (isArtihSym(c)) {
                if (sym.isNotEmpty())
                    tokens.add(sym)
                tokens.add(c.toString())
                sym = ""
            } else {
                sym += c
            }
        }
        //Adding last symbol
        if (sym.isNotEmpty())
            tokens.add(sym)
        return tokens
    }


}