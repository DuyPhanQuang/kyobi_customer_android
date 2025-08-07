package com.kyobi.data.grab

/** Given an array of integers nums and an integer target, return  two numbers such that they add up to target.
You may assume that each input would have exactly one solution, and you may not use the same element twice.
You can return the answer in any order.
Example 1:
Input: nums = [2,7,11,15], target = 9
Output: [2,7]
Explanation: Because nums[0] + nums[1] == 9, we return [2, 7].
Example 2:
Input: nums = [3,2,4], target = 6
Output: [2,4]
Example 3:
Input: nums = [3,3], target = 6
Output: [3,3]

question type: find two numbers in an array that sum up to a target value
only one valid pair exists. we can not reuse the same element

input:
arr: input array of integer
target: target sum value

solution:
overall of this solution is: we use a hashmap to store the value of each element and its
index while iterating through an array.
for each number we calculate its complement. if the complement exists in the map, we've
found a valid pair and return the values.
details:
While looping through the array, at each number, I calculate what value I need to reach the target — that’s the complement.
Then I check: "Have I seen that complement before?"
If yes, I immediately return the pair — because I found the two numbers that add up to the target.
If not, I store the current number in a map, so I can look it up later if it's ever someone else's complement.
This way, I only loop once and use constant time lookups.

complexity:
time: 0(n) iterate array once where n is the length of given array
space: 0(n) store up to n elements in the map
 * */

private fun execute(arr: IntArray, target: Int): IntArray {
    val numIndices = hashMapOf<Int, Int>()
    for (i in arr.indices) {
        val complement = target - arr[i]
        if (complement in numIndices) {
            return intArrayOf(arr[numIndices[complement]!!], arr[i])
        }
        numIndices[arr[i]] = i
    }
    return intArrayOf()
}

fun main() {
    println(execute(intArrayOf(2,7,11,15), 9).toList())
    println(execute(intArrayOf(3,2,4), 6).toList())
    println(execute(intArrayOf(3,3), 6).toList())
}