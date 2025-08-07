package com.kyobi.data.grab

/** question type:
 * find minimum number of substrings with unique characters
 *
 * input:
 * content length of input: must > 0 characters
 * main case: "abacdec" => expectation best split: [ab, acde, c], vv => result: 3, "abaaa" => expectation best split: [ab, a, a, a] => result: 4
 * corner case: exist only 1 character in string: "aaa" => expectation best split: [a, a, a]
 *
 * solution:
 * overall of this solution is:
 * create the longest possible valid substrings before split character.
 * which is what give us the minimum number overall.
 *
 * details:
 * - so the idea here is to split the validated input string into the fewest
 * number of substrings where each substrings has only unique characters
 * - first, i check if input is empty. if so, we throw an exception
 * if not, i use a hashset to track the characters we have seen so far in the
 * current substring. i also keep a string builder to accumulate the current
 * substring and a mutable list to store results.
 * - now we iterate through the string character by character
 * For each character: if it already in the set,
 * it means we have hit a duplicate so we need:
 * - do add the current substring to our result list
 * - do clear the set and reset the string builder for a new substring.
 * otherwise, we just continue building the current substring.
 * after the loop, if there is anything left in the current substring,
 * we add it to the result list as the final piece.
 * Finally we return the number of substrings as the result.
 * complexity:
 * - time: 0(n) where n is the length of the validated input string.
   - space: 0(m) where m is the number of unique characters in current substring
worst case is 26 (26 that means total size of english alphabet) if all letters are unique
 * */

private fun execute(s: String): Int {
    if (s.trim().isEmpty()) throw IllegalArgumentException("input_empty")
    val uniqueChars = hashSetOf<Char>()
    val substrsResult = mutableListOf<String>()
    var currentSubstr = StringBuilder()
    for (char in s) {
        if (uniqueChars.contains(char)) {
            substrsResult.add(currentSubstr.toString())
            uniqueChars.clear()
            currentSubstr = StringBuilder()
        }
        uniqueChars.add(char)
        currentSubstr.append(char)
    }

    // last
    if (currentSubstr.isNotEmpty()) substrsResult.add(currentSubstr.toString())

    println("Substrings: $substrsResult")
    return substrsResult.size
}


fun main() {
    println(execute("abacaba"))
    println(execute("abaaa"))
    println(execute("abacdec"))
    println(execute("dddd"))
}