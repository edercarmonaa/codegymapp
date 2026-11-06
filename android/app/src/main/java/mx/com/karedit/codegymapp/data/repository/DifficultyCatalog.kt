package mx.com.karedit.codegymapp.data.repository

import mx.com.karedit.codegymapp.data.local.dao.CachedCatalogDao
import mx.com.karedit.codegymapp.data.local.entity.CachedCatalogOptionEntity
import mx.com.karedit.codegymapp.domain.model.ChallengeDifficulty
import mx.com.karedit.codegymapp.domain.model.defaultDifficulties

internal suspend fun CachedCatalogDao.cacheDifficulties(options: Map<String, String>?) {
    // Missing means an older backend. Preserve an existing catalog, otherwise use
    // the web's original three levels until that backend is updated.
    if (options == null) {
        seedDifficulties()
        return
    }
    val now = System.currentTimeMillis()
    replaceOptions(CatalogOptionKinds.DIFFICULTIES, options.entries.mapIndexed { index, entry ->
        CachedCatalogOptionEntity(CatalogOptionKinds.DIFFICULTIES, entry.key, entry.value, index, now)
    })
}

internal suspend fun CachedCatalogDao.difficulties(): List<ChallengeDifficulty> =
    options(CatalogOptionKinds.DIFFICULTIES).map { ChallengeDifficulty(it.value, it.label) }

internal suspend fun CachedCatalogDao.seedDifficulties() {
    if (options(CatalogOptionKinds.DIFFICULTIES).isEmpty()) {
        cacheDifficulties(defaultDifficulties().associate { it.value to it.label })
    }
}
