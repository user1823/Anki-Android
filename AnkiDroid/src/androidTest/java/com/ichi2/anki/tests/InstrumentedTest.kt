/****************************************************************************************
 *                                                                                      *
 * Copyright (c) 2020 Mike Hardy <github@mikehardy.net>                                 *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package com.ichi2.anki.tests

import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import com.ichi2.anki.CollectionHelper
import com.ichi2.anki.utils.EnsureAllFilesAccessRule
import com.ichi2.annotations.DuplicatedCode
import com.ichi2.libanki.Card
import com.ichi2.libanki.Collection
import com.ichi2.libanki.Consts
import com.ichi2.libanki.Note
import org.junit.Rule
import java.io.File
import java.io.IOException

abstract class InstrumentedTest {
    protected val col: Collection
        get() = CollectionHelper.instance.getColUnsafe(testContext)!!

    @get:Throws(IOException::class)
    protected val emptyCol: Collection
        get() = Shared.getEmptyCol()

    @get:Rule
    val ensureAllFilesAccessRule = EnsureAllFilesAccessRule()

    /**
     * @return A File object pointing to a directory in which temporary test files can be placed. The directory is
     * emptied on every invocation of this method so it is suitable to use at the start of each test.
     * Only add files (and not subdirectories) to this directory.
     */
    protected val testDir: File
        get() = Shared.getTestDir(testContext)
    protected val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    companion object {
        /**
         * This is how google detects emulators in flutter and how react-native does it in the device info module
         * https://github.com/react-native-community/react-native-device-info/blob/bb505716ff50e5900214fcbcc6e6434198010d95/android/src/main/java/com/learnium/RNDeviceInfo/RNDeviceModule.java#L185
         * @return boolean true if the execution environment is most likely an emulator
         */
        fun isEmulator(): Boolean {
            return (
                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.PRODUCT.contains("sdk_google") ||
                    Build.PRODUCT.contains("google_sdk") ||
                    Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("sdk_x86") ||
                    Build.PRODUCT.contains("vbox86p") ||
                    Build.PRODUCT.contains("emulator") ||
                    Build.PRODUCT.contains("simulator")
                )
        }
    }

    @DuplicatedCode("This is copied from RobolectricTest. This will be refactored into a shared library later")
    protected fun Card.moveToReviewQueue() {
        this.queue = Consts.QUEUE_TYPE_REV
        this.type = Consts.CARD_TYPE_REV
        this.due = 0
        this.col.updateCard(this, true)
    }

    @DuplicatedCode("This is copied from RobolectricTest. This will be refactored into a shared library later")
    protected fun addNoteUsingBasicModel(front: String, back: String): Note {
        return addNoteUsingModelName("Basic", front, back)
    }

    @DuplicatedCode("This is copied from RobolectricTest. This will be refactored into a shared library later")
    private fun addNoteUsingModelName(name: String, vararg fields: String): Note {
        val model = col.notetypes.byName(name)
            ?: throw IllegalArgumentException("Could not find model '$name'")
        // PERF: if we modify newNote(), we can return the card and return a Pair<Note, Card> here.
        // Saves a database trip afterwards.
        val n = col.newNote(model)
        for ((i, field) in fields.withIndex()) {
            n.setField(i, field)
        }
        check(col.addNote(n) != 0) { "Could not add note: {${fields.joinToString(separator = ", ")}}" }
        return n
    }
}
