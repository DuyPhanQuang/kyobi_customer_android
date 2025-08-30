package com.kyobi.data.grab.grag2

/** question type: find minimum number of substrings with unique characters
 * input:
 * content length of input: must be > 0 characters
 * corner case: exist only 1 character in the given string.
 *
 * okay so let me explain my overall solution is:
 * create the longest possible valid substrings before split character
 * which is what give us the minimum number overall.
 *
 * details:
 * - so the idea here is to split the validated input string into the fewest number of substrings
 * where each substrings has only unique characters.
 * - first i check if input is empty. if so, we throw an exception
 * if not i use a hashset to track the characters we have seen so far in the current substring.
 * i also keep a string builder to accumulate the current substring and a mutable list to store results.
 * now we iterate through the string character by character.
 * for each character: if it already in the set, it means we have hit a duplicate so we need:
 * do add the current substring to our result list
 * then do clear the set and reset the string builder for a new substring.
 * otherwise, we just continue building the current substring.
 * after the loop, if there is anything left in the current substring,
 * we add it to the result list as the final piece.
 * finally we return the number of substrings as the result.
 *
 * complexity:
 * time: 0(n) where n is the length of the validated input string.
 * space: 0(m) where m is the number of unique characters in current substring worst case in 26
 * (26 that means total size of english alphabet) if all letters are unique.
 * */
private fun execute(s: String): Int {
    if (s.trim().isEmpty()) throw IllegalArgumentException("input_empty")
    val uniqueChars = hashSetOf<Char>()
    val substringsResult = mutableListOf<String>()
    var currentSubstring = StringBuilder()
    for (char in s) {
        if (uniqueChars.contains(char)) {
            substringsResult.add(currentSubstring.toString())
            uniqueChars.clear()
            currentSubstring = StringBuilder()
        }
        uniqueChars.add(char)
        currentSubstring.append(char)
    }
    if (currentSubstring.isNotEmpty()) {
        substringsResult.add(currentSubstring.toString())
    }
    return substringsResult.size
}

fun main() {
    println(execute("abacaba"))
    println(execute("abaaa"))
    println(execute("abacdec"))
    println(execute("dddd"))
}