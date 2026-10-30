package com.playmez.embed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaymezEventTest {
    @Test
    fun parsesResult() {
        val e = PlaymezEvent.parse("""{"source":"spinline","type":"result","game":"LUDO","won":true,"net":40}""")!!
        assertEquals("result", e.type)
        assertEquals("LUDO", e.game)
        assertTrue(e.won)
        assertEquals(40, e.net)
    }

    @Test
    fun rejectsOtherSendersAndMalformed() {
        assertNull(PlaymezEvent.parse("""{"source":"other","type":"result"}"""))
        assertNull(PlaymezEvent.parse("""{"source":"spinline"}"""))
        assertNull(PlaymezEvent.parse("not json"))
    }

    @Test
    fun exposesErrorFields() {
        val e = PlaymezEvent.parse("""{"source":"spinline","type":"error","code":"LAUNCH_USED","message":"used"}""")!!
        assertEquals("LAUNCH_USED", e.code)
        assertEquals("used", e.message)
    }

    @Test
    fun exposesLocalFinish() {
        val e = PlaymezEvent.parse("""{"source":"spinline","type":"local_finished","game":"chess","draw":false,"won":true}""")!!
        assertEquals("local_finished", e.type)
        assertEquals("chess", e.game)
        assertEquals(true, e.wonVsComputer)
        val together = PlaymezEvent.parse("""{"source":"spinline","type":"local_finished","game":"ludo","draw":true,"won":null}""")!!
        assertTrue(together.draw)
        assertNull(together.wonVsComputer)
    }
}
