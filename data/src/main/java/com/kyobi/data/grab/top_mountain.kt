package com.kyobi.data.grab

/** Let's call an array A a mountain if the following properties hold:
A.length >= 3
There exists some 0 < i < A.length - 1 such that A[0] < A[1] < ... A[i-1] < A[i] >
A[i+1] > ... > A[A.length - 1]
Given an array that is definitely a mountain, return any i such that A[0] < A[1] < ... A[i-1]
< A[i] > A[i+1] > ... > A[A.length - 1].
Example 1:
Input: [0,1,0]
Output: 1
Example 2:
Input: [0,2,1,0]
Output: 1

question type: Given a valid mountain array, return the index of the peak element

input:
IntArray representing a valid mountain
Constraint: input always has length ≥ 3 and follows the mountain shape

solution
overall
We scan the array from left to right and track the index of the largest element
— this will be the peak of the mountain.
details
Initialize a variable peak at index 0
Loop from index 1 to the end of the array
At each step, compare the current element with the one at peak index
If the current element is larger, update peak to this index
After the loop, return peak
This works because the mountain array strictly increases then strictly decreases,
so the global maximum is guaranteed to be the peak.

complexity
Time: O(n) — one pass through the array
Space: O(1) — only a few variables used
 * */

private fun solution(input: IntArray): Int {
    var peak = 0
    for (i in 1 until input.size) {
        if (input[i] > input[peak]) {
            peak = i
        }
    }
    return peak
}

fun main() {
    println(solution(intArrayOf(0,1,0)))
    println(solution(intArrayOf(0,2,1,0)))
}

