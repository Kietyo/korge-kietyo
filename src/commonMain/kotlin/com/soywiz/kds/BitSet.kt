package com.soywiz.kds

import com.soywiz.kds.internal.*

/**
 * Fixed size [BitSet]. Similar to a [BooleanArray] but tightly packed to reduce memory usage.
 */
class BitSet(val size: Int) {
	val data = IntArray(size divCeil 4)

	private fun part(index: Int) = index ushr 5
	private fun bit(index: Int) = index and 0x1f

	operator fun get(index: Int): Boolean = ((data[part(index)] ushr (bit(index))) and 1) != 0
	operator fun set(index: Int, value: Boolean) {
		val i = part(index)
		val b = bit(index)
		if (value) {
			data[i] = data[i] or (1 shl b)
		} else {
			data[i] = data[i] and (1 shl b).inv()
		}
	}

	fun set(index: Int): Unit = set(index, true)
	fun unset(index: Int): Unit = set(index, false)

	fun clear(): Unit = data.fill(0)
}
