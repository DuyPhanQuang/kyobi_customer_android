package com.kyobi.data.grab.grag2

/** question type
 * Check if an array is a valid mountain: strictly increases to a peak, then strictly decreases.
 * The peak cannot be at the start or end, and the array must have at least 3 elements.
 *
 * input
 * arr: an array of integers
 * Constraint: arr.size >= 3
 *
 * solution
 * We simulate climbing a mountain from left to right using one pointer.
 * First, move up while the array is increasing.
 * Then, move down while the array is decreasing.
 * At the end, if we’ve reached the last index — and the peak is not at the edges —
 * then it’s a valid mountain.
 * So I just simulate walking along the mountain from left to right.
 * First, I climb up: I keep moving forward as long as each next number is bigger than
 * the current one — that means the array is increasing.
 * If I stop too early — like, at the first element — or too late — like,
 * at the last element — then there’s no valid peak. So I return false in that case.
 * If the peak is valid (somewhere in the middle),
 * then I continue walking and check if the numbers now strictly decrease.
 * Finally, if I reach the very end of the array, that means the sequence first increased
 * then decreased properly — so it’s a valid mountain.
 * Otherwise, it’s not a mountain, maybe it’s flat somewhere or increases again —
 * so I return false.
 *
 * complexity
 * Time: O(n) — each element is visited at most once
 * Space: O(1) — no extra data structures used
 * */
fun execute(arr: Array<Int>): Boolean {
    if (arr.size < 3) return false
    var climb = 0
    while (climb < arr.size-1 && arr[climb] < arr[climb+1]) {
        climb += 1
    }
    if (climb == 0 || climb == arr.size-1) {
        return false
    }
    while (climb < arr.size-1 && arr[climb]> arr[climb+1]) {
        climb += 1
    }
    return climb == arr.size-1
}

fun main() {
    println(execute(arrayOf(1,2,3,4,5,4,3,7)))
    println(execute(arrayOf(1,2,3,4,7,6,5)))
}
