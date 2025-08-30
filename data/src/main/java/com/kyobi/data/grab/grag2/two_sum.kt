package com.kyobi.data.grab.grag2

/**
 * question type: find two numbers in an array that sum up to a target value
 * only one validated pair exists. we can not reuse the same element
 *
 *
 * okay so let me explain my overall solution is:
 * we use a hashmap to store the value of each element and its index while iterating through an array.
 * for each number we calculate its complement. if the complement exists in the map, we're found a
 * validate pair and return the values.
 * details: while looping through the array, at each number, i calculate what value i need to reach
 * the target that mean same with the complement.
 * then i check by myself have i seen that complement before?
 * if yes, i immediately return the pair - because i found the two numbers that add up to the target.
 * if not, i store the current number in a map, so i can look it up later if it is ever
 * someone else complement.
 *
 * complexity:
 * time: 0(n) where n is the length of the given input array. because we iterate an array one where
 * n is the length of given array.
 * space: 0(n) because we store up to n elements in the map
 *
 * why use IntArray instead of Array<Int>
 * IntArray is primitive array (same int[] in java) -> better performance
 * Array<Int> is generic array, every element is Integer object
 * -> a litter slower, more memory consuming
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
    println(execute(intArrayOf(2, 7, 11, 15), 9).toList())
    println(execute(intArrayOf(3, 2, 4), 6).toList())
    println(execute(intArrayOf(3, 3), 6).toList())
}