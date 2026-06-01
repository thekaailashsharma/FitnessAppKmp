@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package org.awi.fitness.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()
