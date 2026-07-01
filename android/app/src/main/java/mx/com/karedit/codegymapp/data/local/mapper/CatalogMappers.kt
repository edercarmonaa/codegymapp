package mx.com.karedit.codegymapp.data.local.mapper

import mx.com.karedit.codegymapp.data.local.entity.CachedLanguageEntity
import mx.com.karedit.codegymapp.data.local.entity.CachedPlatformEntity
import mx.com.karedit.codegymapp.data.remote.dto.MobileLanguageDto
import mx.com.karedit.codegymapp.data.remote.dto.MobilePlatformDto
import mx.com.karedit.codegymapp.domain.model.MobileLanguage
import mx.com.karedit.codegymapp.domain.model.MobilePlatform

fun MobilePlatformDto.toDomain(): MobilePlatform = MobilePlatform(id = id, name = name)

fun MobileLanguageDto.toDomain(): MobileLanguage = MobileLanguage(id = id, name = name)

fun MobilePlatformDto.toCacheEntity(cachedAt: Long): CachedPlatformEntity =
    CachedPlatformEntity(id = id, name = name, cachedAt = cachedAt)

fun MobileLanguageDto.toCacheEntity(cachedAt: Long): CachedLanguageEntity =
    CachedLanguageEntity(id = id, name = name, cachedAt = cachedAt)

fun CachedPlatformEntity.toDomain(): MobilePlatform = MobilePlatform(id = id, name = name)

fun CachedLanguageEntity.toDomain(): MobileLanguage = MobileLanguage(id = id, name = name)
