<?php

declare(strict_types=1);

namespace CodeGymApp\Services;

final class DateValidator
{
    public function isDate(string $date): bool
    {
        $parsed = \DateTimeImmutable::createFromFormat('Y-m-d', $date);
        return $parsed instanceof \DateTimeImmutable && $parsed->format('Y-m-d') === $date;
    }
}
