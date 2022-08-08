package com.soywiz.benchmarks

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
@Measurement(iterations = 5, timeUnit = BenchmarkTimeUnit.NANOSECONDS)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
class BoxVsUnboxBenchmarks {
    val inttArray = IntArray(4096)
    val boxxedIntArray = Array<Int>(4096) {
        0
    }

    @Benchmark
    fun setIntArray() {
        // 0.571 ±(99.9%) 0.093 ns/op [Average]
        //  (min, avg, max) = (0.551, 0.571, 0.610), stdev = 0.024
        //  CI (99.9%): [0.478, 0.664] (assumes normal distribution)
        inttArray[0] = 123
    }

    @Benchmark
    fun setBoxedIntArray() {
        // 1.141 ±(99.9%) 0.113 ns/op [Average]
        //  (min, avg, max) = (1.089, 1.141, 1.158), stdev = 0.029
        //  CI (99.9%): [1.028, 1.254] (assumes normal distribution)
        boxxedIntArray[0] = 123
    }

    @Benchmark
    fun getIntArray(): Int {
        // 2.194 ±(99.9%) 1.140 ns/op [Average]
        //  (min, avg, max) = (1.665, 2.194, 2.335), stdev = 0.296
        //  CI (99.9%): [1.054, 3.334] (assumes normal distribution)
        return inttArray[0]
    }

    @Benchmark
    fun getBoxedIntArray(): Int {
        // 2.128 ±(99.9%) 0.737 ns/op [Average]
        //  (min, avg, max) = (1.786, 2.128, 2.218), stdev = 0.192
        //  CI (99.9%): [1.391, 2.866] (assumes normal distribution)
        return boxxedIntArray[0]
    }

    @Benchmark
    fun iterateIntArray() {
        // 912.019 ±(99.9%) 2.172 ns/op [Average]
        //  (min, avg, max) = (911.288, 912.019, 912.824), stdev = 0.564
        //  CI (99.9%): [909.847, 914.191] (assumes normal distribution)
        var count = 0
        for (i in inttArray) {
            count += i
        }
    }

    @Benchmark
    fun iterateBoxedIntArray() {
        // 928.315 ±(99.9%) 4.841 ns/op [Average]
        //  (min, avg, max) = (927.444, 928.315, 930.526), stdev = 1.257
        //  CI (99.9%): [923.474, 933.156] (assumes normal distribution)
        var count = 0
        for (i in boxxedIntArray) {
            count += i
        }
    }
}
