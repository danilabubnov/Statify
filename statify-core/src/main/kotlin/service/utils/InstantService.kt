package org.danila.service.utils

import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class InstantService {

    val now: Instant
        get() = Instant.now()

}