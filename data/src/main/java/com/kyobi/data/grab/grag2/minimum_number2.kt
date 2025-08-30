package com.kyobi.data.grab.grag2

/** question type: find minimum number of substrings with all unique characters
 * input: content length of input: must > 0
 *
 * solution:
 * here let me explain my overall solution:
 * i only split when necessary when there is no way to extend the current substring without
 * repeating a character.
 * details:
 * i use hashset to track the characters i have seen in the current substring and initialize count
 * variable as result then i loop through the input string character by character
 * for each character in given string:
 * if it is not in the set, i add it and continue building the current substring
 * if it is already in the set that mean i have a duplicate so i need:
 * - do split here
 * - i increase the count, clear the set and start new substring from this character at the end.
 * finally, i return count+1 to include the last substring as the result
 * */
private fun execute(s: String): Int {
    if (s.trim().isEmpty()) throw IllegalArgumentException("input_empty")
    val uniqueChars = hashSetOf<Char>()
    var count = 0
    for (char in s) {
        if (char in uniqueChars) {
            count++
            uniqueChars.clear()
        }
        uniqueChars.add(char)
    }
    return count + 1
}

fun main() {
    println(execute("world"))
    println(execute("dddd"))
    println(execute("cycle"))
    println(execute("abba"))
}
