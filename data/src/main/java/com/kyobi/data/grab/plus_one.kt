package com.kyobi.data.grab

/** 6/ Given a non-negative number represented as an array of digits,
add 1 to the number ( increment the number represented by the digits ).
The digits are stored such that the most significant digit is at the head of the list.

Example:
If the vector has [1, 2, 3]
the returned vector should be [1, 2, 4]
as 123 + 1 = 124.
return [1, 2, 4]
[0, 0, 0]
-> [1] or [0, 0, 1]

question type: Add 1 to an integer represented as an array of digits.
Each digit is between 0-9, most significant digit is at the start.

input
An IntArray of digits representing a non-negative number

solution:
overall: We simulate adding 1 to the number,
starting from the end of the array (least significant digit).
We handle carry if a digit becomes 10,
and extend the array if there's a leftover carry at the end.
details:
Step 1: Add 1 to the last digit (rightmost)
Step 2: Loop backward through the array
Add any carry to the current digit
If the result is 10 or more, carry becomes 1 and current digit becomes digit % 10
Otherwise, carry is 0
Step 3: After the loop, check if carry > 0
If yes, we need a new digit at the front (e.g. from 999 to 1000)
Create a new array with size n + 1, place carry in front, copy the rest

complexity
Time: O(n) — we process each digit once
Space: O(n) — in the worst case, the result array is one element longer than input
 * */

private fun solution(digits: IntArray): IntArray {
    val n = digits.size
    // adding 1 to the least significant digit
    digits[n - 1]++
    // Handling carry over
    var carry = 0
    for (i in n - 1 downTo 0) {
        digits[i] += carry
        carry = digits[i] / 10
        digits[i] %= 10
    }
    // if there's still carry after the iteration, add a new most significant digit
    if (carry > 0) {
        val result = IntArray(n + 1)
        result[0] = carry
        for (i in 1 until n + 1) {
            result[i] = digits[i - 1]
        }
        return result
    }
    return digits
}

fun main() {
    println(solution(intArrayOf(1, 2, 3)).toList())
    println(solution(intArrayOf(1, 5, 9)).toList())
    println(solution(intArrayOf(1, 9, 9)).toList())
    println(solution(intArrayOf(9, 9, 9)).toList())
    println(solution(intArrayOf(0, 0, 0)).toList())
}