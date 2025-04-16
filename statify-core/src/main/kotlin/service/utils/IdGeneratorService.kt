package org.danila.service.utils

import org.springframework.stereotype.Service
import java.util.*

@Service
class IdGeneratorService {

    val uuid: UUID
        get() = UUID.randomUUID()

}