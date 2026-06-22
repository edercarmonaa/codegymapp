package mx.com.karedit.codegymapp.data.mapper

import mx.com.karedit.codegymapp.data.remote.dto.MobileChallengeDto
import mx.com.karedit.codegymapp.domain.model.MobileChallenge

fun MobileChallengeDto.toDomain(): MobileChallenge =
    MobileChallenge(
        id = id,
        platformId = platformId,
        platformName = platformName,
        title = title.orEmpty(),
        scheduledDate = scheduledDate,
        completedDate = completedDate,
        status = status,
        difficulty = difficulty.orEmpty(),
        challengeUrl = challengeUrl,
        timeSpentMinutes = timeSpentMinutes,
        notes = notes.orEmpty(),
        languageIds = languageIds,
        languageNames = languageNames,
        githubLinks = githubLinks.map { it.githubUrl },
        origin = origin.orEmpty(),
        isRescheduled = isRescheduled
    )
