package com.kyobi.data.grab

/** given a string S of length N, returns the minimum number of substrings into which the string has to be split.
Examples:
1. Given 'world', your function should return 1.
There is no need to split the string into substrings as all letters occur just once.
2. Given 'dddd', your function should return 4.
The result can be achieved by splitting the string into four substrings ('d', 'd', 'd', 'd').
3. Given 'cycle', your function should return 2.
The result can be achieved by splitting the string into two substrings ('cy', 'cle') or ('c', 'ycle').
4. Given 'abba', your function should return 2.
The result can be achieved by splitting the string into two substrings ('ab', 'ba').

question type: find minimum number of substrings with all unique characters

 input:
 content length of input: must > 0
 * main case: "world" → 1, "dddd" → 4, "abba" → 2, "cycle" → 2
 *
 * solution:
 * overall of this solution is:
 * i only split when necessary when there is no way to extend the current substring without
 * repeating a character.
 * details:
 * i use hashset to track the characters i have seen in the current substring
 * and initialize count variable as result.
 * then i loop through the input string character by character
 * for each character in given string:
 * if it is not in the set, i add it and continue building the current substring
 * if it is already in the set that mean i have a duplicate so i need:
 * - do split here
 * - i increase the count, clear the set and start new substring from this character
 * add the end
 * Finally, i return count + 1 to include the last substring as the result.
 *
 * complexity:
 * - time: 0(n) where n is the length of the input string
 * - space: 0(m) where m is the number of unique characters in current substring
 * worst case is 26 (26 that means total size of english alphabet) if all letters are unique
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

