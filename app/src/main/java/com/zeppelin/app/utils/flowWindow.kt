package com.zeppelin.app.utils


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.collections.ArrayDeque

/**
 * Collects upstream values in non-overlapping chunks of size [count], then
 * calls [aggregate] on each full chunk and emits its result. Any trailing
 * values (< [count]) are dropped.
 *
 * @param count     Number of elements per chunk; must be > 0.
 * @param aggregate A function which, given a List<T> of exactly [count]
 *                  elements, computes an R (for you, the “mean”).
 * @return A Flow<R> emitting one element per chunk.
 * @throws IllegalArgumentException if [count] ≤ 0.
 */
fun <T, R> Flow<T>.onMeanCount(
    count: Int,
    aggregate: (List<T>) -> R
): Flow<R> {
    require(count > 0) { "count must be > 0" }
    val buffer = mutableListOf<T>()
    return transform { value ->
        buffer += value
        if (buffer.size == count) {
            emit(aggregate(buffer))
            buffer.clear()
        }
    }
}

/**
 * Emits sliding windows of the upstream elements.
 *
 * Each window is a [List] of length [size]. After emitting a window,
 * the first [step] items are removed from the buffer. Only *full* windows
 * are emitted; any trailing elements fewer than [size] are discarded.
 *
 * @param size Number of elements in each window; must be > 0.
 * @param step Number of elements to advance the window by; must be > 0.
 *             Defaults to [size], i.e. non‐overlapping windows.
 * @return A [Flow] emitting consecutive windows as lists of T.
 * @throws IllegalArgumentException if [size] ≤ 0 or [step] ≤ 0.
 */
fun <T> Flow<T>.windowed( size: Int, step: Int = size ): Flow<List<T>> {
    require(size  > 0) { "size must be > 0" }
    require(step  > 0) { "step must be > 0" }

    val buffer = ArrayDeque<T>(size)
    return this.transform { value ->
        buffer.addLast(value)
        if (buffer.size >= size) {
            // emit a snapshot of the first 'size' elements
            emit(buffer.take(size))
            // drop 'step' elements to advance the window
            repeat(step) {
                if (buffer.isNotEmpty()) buffer.removeFirst()
            }
        }
    }
}