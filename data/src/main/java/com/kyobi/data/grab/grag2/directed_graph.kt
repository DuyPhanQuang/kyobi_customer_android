package com.kyobi.data.grab.grag2

/** question type: detect cycle in a directed graph based on prerequisite relationships
 * Depth-First Search + Cycle Detection
 * input:
 * numCourses is total number of courses, labeled from 0 to numCourses - 1
 * prerequisites is each pair [a, b] means course a requires course b before it.
 * solution:
 * overall: we model this as a directed graph. we perform DFS to check if there's a cycle
 * in the course dependency graph. if there is no cycle, we can finish all courses.
 * details:
 * i build a graph using an adjacency list.
 * each node (course) points to its prerequisites
 * use a visited array to track course state:
 * UNVISITED = unvisited
 * VISITING = visiting (in current DFS path)
 * VISITED = visited and completed (no cycle from this node)
 * for each course:
 * if dfs detects a cycle is visiting will return false.
 * if already visited will skip
 * after finishing dfs, mark as completed
 * run dfs for all courses because the graph may be disconnected
 * if all dfs traversals succeed, return true
 * complexity:
 * time: 0(n+m) where n is the length of numCourses and m is number of prerequisites pairs
 * space: 0(n+m) for the graph and the visited state array
 * */

enum class TrackType { UNVISITED, VISITING, VISITED }
private fun TrackType.isVisiting() = this == TrackType.VISITING
private fun TrackType.isVisited() = this == TrackType.VISITED

private fun execute(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
    val graph = Array(numCourses) { mutableListOf<Int>() }
    for (p in prerequisites) {
        if (p.size != 2) throw IllegalArgumentException("prerequisite_invalid")
        val (l, r) = p
        graph[l].add(r)
    }
    // dfs traversal to detect cycle
    fun dfs(course: Int, visited: Array<TrackType>): Boolean {
        if (visited[course].isVisiting()) return false // visiting
        if (visited[course].isVisited()) return true // visited
        visited[course] = TrackType.VISITING // mark visiting
        for (p in graph[course]) {
            if (!dfs(p, visited)) return false
        }
        visited[course] = TrackType.VISITED // mark visited
        return true
    }
    // dfs traversal on each course
    val visited = Array(numCourses) { TrackType.UNVISITED }
    for (course in 0 until numCourses) {
        if (!dfs(course, visited)) return false
    }
    return true
}

fun main() {
    println(execute(2, arrayOf(intArrayOf(1, 0), intArrayOf(0, 1))))
    println(execute(2, arrayOf(intArrayOf(1, 0))))
    println(execute(3, arrayOf(intArrayOf(1, 0), intArrayOf(2, 1))))
}
