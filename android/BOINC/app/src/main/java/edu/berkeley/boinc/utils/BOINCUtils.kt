/*
 * This file is part of BOINC.
 * http://boinc.berkeley.edu
 * Copyright (C) 2020 University of California
 *
 * BOINC is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * BOINC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BOINC.  If not, see <http://www.gnu.org/licenses/>.
 */
@file:JvmName("BOINCUtils")

package edu.berkeley.boinc.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.os.Build
import android.os.RemoteException
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import edu.berkeley.boinc.BOINCActivity
import edu.berkeley.boinc.R
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException
import java.io.Reader

val ConnectivityManager.isOnline: Boolean
    get() {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activeNetworkInfo?.isConnectedOrConnecting == true
        } else {
            activeNetwork != null
        }
    }

suspend fun writeClientModeAsync(mode: Int) = coroutineScope {
    val runMode = async {
        return@async try {
            BOINCActivity.monitor!!.setRunMode(mode)
        } catch (e: RemoteException) {
            false
        }
    }
    val networkMode = async {
        return@async try {
            BOINCActivity.monitor!!.setNetworkMode(mode)
        } catch (e: RemoteException) {
            false
        }
    }

    return@coroutineScope runMode.await() && networkMode.await()
}

fun Context.getBitmapFromVectorDrawable(@DrawableRes drawableId: Int): Bitmap? {
    var drawable = ContextCompat.getDrawable(this, drawableId)!!
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = DrawableCompat.wrap(drawable).mutate()
    }
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Throws(IOException::class)
fun Reader.readLineLimit(limit: Int): String? {
    val sb = StringBuilder()
    for (i in 0 until limit) {
        val c = read() //Read in single character
        if (c == -1) {
            return if (sb.isNotEmpty()) sb.toString() else null
        }
        if (c.toChar() == '\n' || c.toChar() == '\r') { //Found end of line, break loop.
            break
        }
        sb.append(c.toChar()) // String is not over and end line not found
    }
    return sb.toString() //end of line was found.
}

fun Context.translateRPCReason(reason: Int) = when (reason) {
    RPC_REASON_USER_REQ -> resources.getString(R.string.rpcreason_userreq)
    RPC_REASON_NEED_WORK -> resources.getString(R.string.rpcreason_needwork)
    RPC_REASON_RESULTS_DUE -> resources.getString(R.string.rpcreason_resultsdue)
    RPC_REASON_TRICKLE_UP -> resources.getString(R.string.rpcreason_trickleup)
    RPC_REASON_ACCT_MGR_REQ -> resources.getString(R.string.rpcreason_acctmgrreq)
    RPC_REASON_INIT -> resources.getString(R.string.rpcreason_init)
    RPC_REASON_PROJECT_REQ -> resources.getString(R.string.rpcreason_projectreq)
    else -> resources.getString(R.string.rpcreason_unknown)
}
