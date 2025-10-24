package korlibs.datastructure.thread

import korlibs.datastructure.getExtra
import korlibs.datastructure.setExtra
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreadTest {
    @Test
    fun test() {
        val KEY = "hello"
        val VALUE = "world"
        val extra = NativeThread { }.extra
        extra.setExtra(KEY, VALUE)
        assertEquals(VALUE, extra.getExtra(KEY))
    }
}
