package org.danila.util

import java.util.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

object UserIdKey : CoroutineContext.Key<UserIdElement>

class UserIdElement(val userId: UUID) :
    AbstractCoroutineContextElement(UserIdKey)