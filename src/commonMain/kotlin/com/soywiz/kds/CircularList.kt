package com.soywiz.kds

import com.soywiz.kds.internal.*


class CircularList<TGen> : MutableCollection<TGen> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: Array<TGen> = arrayOfNulls<Any>(16) as Array<TGen>
    private val capacity: Int get() = data.size

    override val size: Int get() = _size

    fun isNotEmpty(): Boolean = size != 0
    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = arrayOfNulls<Any>(this.data.size * 2) as Array<TGen>
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: Array<TGen>, istart: Int, o: Array<TGen>, count: Int) {
        val size1 = kotlin.math.min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<TGen>) = run {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addFirst(item: TGen) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: TGen) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): TGen {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): TGen {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    // @TODO: This is slow. But we can improve it using two arraycopy. Also we can reduce from left or from right.
    fun removeAt(index: Int): TGen {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // if (index < size / 2) // @TODO: reduce from left
        val old = this[index]
        for (n in index until size - 1) this[n] = this[n + 1]
        _size--
        return old
    }

    override fun add(element: TGen): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<TGen>): Boolean = true.apply { addAll(elements as Iterable<TGen>) }
    override fun clear() = run { _size = 0 }
    override fun remove(element: TGen): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<TGen>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<TGen>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<TGen>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: TGen get() = data[_start]
    val last: TGen get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: TGen): Unit = run { data[internalIndex(index)] = value }
    operator fun get(index: Int): TGen = data[internalIndex(index)]

    override fun contains(element: TGen): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: TGen): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<TGen>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<TGen> {
        val that = this
        return object : MutableIterator<TGen> {
            var index = 0
            override fun next(): TGen = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() = TODO()
        }
    }
}


// Int

class IntCircularList : MutableCollection<Int> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: IntArray = IntArray(16) as IntArray
    private val capacity: Int get() = data.size

    override val size: Int get() = _size

    fun isNotEmpty(): Boolean = size != 0
    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = IntArray(this.data.size * 2) as IntArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: IntArray, istart: Int, o: IntArray, count: Int) {
        val size1 = kotlin.math.min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Int>) = run {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addFirst(item: Int) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Int) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): Int {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Int {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    // @TODO: This is slow. But we can improve it using two arraycopy. Also we can reduce from left or from right.
    fun removeAt(index: Int): Int {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // if (index < size / 2) // @TODO: reduce from left
        val old = this[index]
        for (n in index until size - 1) this[n] = this[n + 1]
        _size--
        return old
    }

    override fun add(element: Int): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Int>): Boolean = true.apply { addAll(elements as Iterable<Int>) }
    override fun clear() = run { _size = 0 }
    override fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Int>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Int>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Int>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Int get() = data[_start]
    val last: Int get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Int): Unit = run { data[internalIndex(index)] = value }
    operator fun get(index: Int): Int = data[internalIndex(index)]

    override fun contains(element: Int): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Int): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Int> {
        val that = this
        return object : MutableIterator<Int> {
            var index = 0
            override fun next(): Int = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() = TODO()
        }
    }
}


// Double

class DoubleCircularList : MutableCollection<Double> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: DoubleArray = DoubleArray(16) as DoubleArray
    private val capacity: Int get() = data.size

    override val size: Int get() = _size

    fun isNotEmpty(): Boolean = size != 0
    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = DoubleArray(this.data.size * 2) as DoubleArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: DoubleArray, istart: Int, o: DoubleArray, count: Int) {
        val size1 = kotlin.math.min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Double>) = run {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addFirst(item: Double) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Double) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): Double {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Double {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    // @TODO: This is slow. But we can improve it using two arraycopy. Also we can reduce from left or from right.
    fun removeAt(index: Int): Double {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // if (index < size / 2) // @TODO: reduce from left
        val old = this[index]
        for (n in index until size - 1) this[n] = this[n + 1]
        _size--
        return old
    }

    override fun add(element: Double): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Double>): Boolean = true.apply { addAll(elements as Iterable<Double>) }
    override fun clear() = run { _size = 0 }
    override fun remove(element: Double): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Double>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Double>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Double>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Double get() = data[_start]
    val last: Double get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Double): Unit = run { data[internalIndex(index)] = value }
    operator fun get(index: Int): Double = data[internalIndex(index)]

    override fun contains(element: Double): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Double): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Double>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Double> {
        val that = this
        return object : MutableIterator<Double> {
            var index = 0
            override fun next(): Double = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() = TODO()
        }
    }
}


// Float

class FloatCircularList : MutableCollection<Float> {
    private var _start: Int = 0
    private var _size: Int = 0
    private var data: FloatArray = FloatArray(16) as FloatArray
    private val capacity: Int get() = data.size

    override val size: Int get() = _size

    fun isNotEmpty(): Boolean = size != 0
    override fun isEmpty(): Boolean = size == 0

    private fun resizeIfRequiredFor(count: Int) {
        if (size + count > capacity) {
            val i = this.data
            val istart = this._start
            val o = FloatArray(this.data.size * 2) as FloatArray
            copyCyclic(i, istart, o, this._size)
            this.data = o
            this._start = 0
        }
    }

    private fun copyCyclic(i: FloatArray, istart: Int, o: FloatArray, count: Int) {
        val size1 = kotlin.math.min(i.size - istart, count)
        val size2 = count - size1
        arraycopy(i, istart, o, 0, size1)
        if (size2 > 0) arraycopy(i, 0, o, size1, size2)
    }

    fun addAll(items: Iterable<Float>) = run {
        resizeIfRequiredFor(items.count())
        for (i in items) addLast(i)
    }

    fun addFirst(item: Float) {
        resizeIfRequiredFor(1)
        _start = (_start - 1) umod capacity
        _size++
        data[_start] = item
    }

    fun addLast(item: Float) {
        resizeIfRequiredFor(1)
        data[(_start + size) umod capacity] = item
        _size++
    }

    fun removeFirst(): Float {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return first.apply { _start = (_start + 1) umod capacity; _size-- }
    }

    fun removeLast(): Float {
        if (_size <= 0) throw IndexOutOfBoundsException()
        return last.apply { _size-- }
    }

    // @TODO: This is slow. But we can improve it using two arraycopy. Also we can reduce from left or from right.
    fun removeAt(index: Int): Float {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException()
        if (index == 0) return removeFirst()
        if (index == size - 1) return removeLast()

        // if (index < size / 2) // @TODO: reduce from left
        val old = this[index]
        for (n in index until size - 1) this[n] = this[n + 1]
        _size--
        return old
    }

    override fun add(element: Float): Boolean = true.apply { addLast(element) }
    override fun addAll(elements: Collection<Float>): Boolean = true.apply { addAll(elements as Iterable<Float>) }
    override fun clear() = run { _size = 0 }
    override fun remove(element: Float): Boolean {
        val index = indexOf(element)
        if (index >= 0) removeAt(index)
        return (index >= 0)
    }

    override fun removeAll(elements: Collection<Float>): Boolean = _removeRetainAll(elements, retain = false)
    override fun retainAll(elements: Collection<Float>): Boolean = _removeRetainAll(elements, retain = true)

    private fun _removeRetainAll(elements: Collection<Float>, retain: Boolean): Boolean {
        val eset = elements.toSet()
        val temp = this.data.copyOf()
        var tsize = 0
        val osize = size
        for (n in 0 until size) {
            val c = this[n]
            if ((c in eset) == retain) {
                temp[tsize++] = c
            }
        }
        this.data = temp
        this._start = 0
        this._size = tsize
        return tsize != osize
    }

    val first: Float get() = data[_start]
    val last: Float get() = data[internalIndex(size - 1)]

    private fun internalIndex(index: Int) = (_start + index) umod capacity

    operator fun set(index: Int, value: Float): Unit = run { data[internalIndex(index)] = value }
    operator fun get(index: Int): Float = data[internalIndex(index)]

    override fun contains(element: Float): Boolean = (0 until size).any { this[it] == element }

    fun indexOf(element: Float): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }

    override fun containsAll(elements: Collection<Float>): Boolean {
        val emap = elements.map { it to 0 }.toLinkedMap()
        for (it in 0 until size) {
            val e = this[it]
            if (e in emap) emap[e] = 1
        }
        return emap.values.all { it == 1 }
    }

    override fun iterator(): MutableIterator<Float> {
        val that = this
        return object : MutableIterator<Float> {
            var index = 0
            override fun next(): Float = that[index++]
            override fun hasNext(): Boolean = index < size
            override fun remove() = TODO()
        }
    }
}
