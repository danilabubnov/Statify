package org.danila.repository

import org.danila.model.OAuth2LinkState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface OAuth2LinkStateRepository : JpaRepository<OAuth2LinkState, UUID>