
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.content.cache
 * Modifications:
 * - Updated package statement and adjusted imports.
 */

package org.overengineer.talelistener.content.cache

class BookCachingService {
}

sealed class CacheProgress {
    data object Idle : CacheProgress()
    data object Caching : CacheProgress()
    data object Completed : CacheProgress()
    data object Removed : CacheProgress()
    data object Error : CacheProgress()
}
