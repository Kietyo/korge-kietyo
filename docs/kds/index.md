---
layout: default
title: KDS
permalink: /kds/
fa-icon: fa-tree
children: /kds/
priority: 58
---

<img src="/i/logos/kds.svg" width="196" height="196" style="float: left;margin: 0 16px 16px 0;" alt="KDS: Data Structures" />

<p align="center">
Kds is a Data Structure library for Multiplatform Kotlin.
It includes a set of optimized data structures written in Kotlin Common so they are available in
JVM, JS and future multiplatform targets. Those structures are designed to be allocation-efficient and fast, so Kds
include specialized versions for primitives like <code>Int</code> or <code>Double</code>.
</p>

[https://github.com/korlibs/kds](https://github.com/korlibs/kds)

{% include stars.html project="korge" central="com.soywiz.korlibs.kds/kds" %}

[![Build Status](https://travis-ci.org/korlibs/kds.svg?branch=master)](https://travis-ci.org/korlibs/kds)
[![Maven Version](https://img.shields.io/github/tag/korlibs/kds.svg?style=flat&label=maven)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22kds%22)

{% include toc_include.md %}

## Pages

{% include toc.html context="/kds/" description=true %}


### Some samples:

```kotlin
// Case Insensitive Map
val map = mapOf("hELLo" to 1, "World" to 2).toCaseInsensitiveMap()
println(map["hello"])

// BitSet
val array = BitSet(100) // Stores 100 bits
array[99] = true

// TypedArrayList
val v20 = intArrayListOf(10, 20).getCyclic(-1)

// Deque
val deque = IntDeque().apply {
    addFirst(n)
    removeFirst()
    addLast(n)
}

// CacheMap
val cache = CacheMap<String, Int>(maxSize = 2).apply {
    this["a"] = 1
    this["b"] = 2
    this["c"] = 3
    assertEquals("{b=2, c=3}", this.toString())
}

// IntIntMap
val m = IntIntMap().apply {
    this[0] = 98
}

// Pool
val pool = Pool { Demo() }
pool.alloc { demo ->
    println("Temporarilly allocated $demo")
}

// Priority Queue
val pq = IntPriorityQueue()
pq.add(10)
pq.add(5)
pq.add(15)
assertEquals(5, pq.removeHead())

// Extra Properties
class Demo : Extra by Extra.Mixin() { val default = 9 }
var Demo.demo by Extra.Property { 0 }
var Demo.demo2 by Extra.PropertyThis<Demo, Int> { default }
val demo = Demo()
assertEquals(0, demo.demo)
assertEquals(9, demo.demo2)
demo.demo = 7
assertEquals(7, demo.demo)
assertEquals("{demo=7, demo2=9}", demo.extra.toString())

// mapWhile
val iterator = listOf(1, 2, 3).iterator()
assertEquals(listOf(1, 2, 3), mapWhile({ iterator.hasNext() }) { iterator.next()})

// And much more!
```

{% include using_with_gradle.md name="kds" %}