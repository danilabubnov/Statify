package org.danila.util

import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

object UserSpotifyLibraryKey : CoroutineContext.Key<UserSpotifyLibraryElement>

class UserSpotifyLibraryElement(val id: UUID) :
    AbstractCoroutineContextElement(UserSpotifyLibraryKey)