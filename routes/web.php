<?php

declare(strict_types=1);

$router = new Router();

$router->get('/', 'CalendarController', 'index');
$router->get('/login', 'AuthController', 'showLogin', false);
$router->post('/login', 'AuthController', 'login', false);
$router->post('/logout', 'AuthController', 'logout');
$router->post('/api/auth/login', 'ApiAuthController', 'login', false);
$router->get('/api/me', 'ApiMeController', 'show');
$router->get('/api/mobile/today', 'ApiMobileController', 'today');
$router->get('/api/mobile/planned', 'ApiMobileController', 'planned');
$router->post('/api/mobile/challenges/complete', 'ApiMobileController', 'completeChallenge');
$router->post('/api/mobile/challenges/miss', 'ApiMobileController', 'missChallenge');

$router->get('/dashboard', 'DashboardController', 'index');
$router->get('/dashboard/reportes', 'DashboardController', 'reportsTab');
$router->get('/calendario', 'CalendarController', 'index');
$router->get('/retos', 'ChallengeController', 'index');
$router->post('/retos/manual', 'ChallengeController', 'manual');

$router->get('/api/calendar/events', 'ApiCalendarController', 'events');
$router->get('/api/calendar/bootstrap', 'ApiCalendarController', 'bootstrap');
$router->get('/api/calendar/routines', 'ApiCalendarController', 'routines');
$router->get('/api/calendar/challenge', 'ApiCalendarController', 'challenge');
$router->post('/api/calendar/store', 'ApiCalendarController', 'store');
$router->post('/api/calendar/save-details', 'ApiCalendarController', 'saveDetails');
$router->post('/api/calendar/complete', 'ApiCalendarController', 'complete');
$router->post('/api/calendar/miss', 'ApiCalendarController', 'miss');
$router->post('/api/calendar/cancel', 'ApiCalendarController', 'cancel');
$router->post('/api/calendar/update-date', 'ApiCalendarController', 'updateDate');
$router->post('/api/calendar/routine/store', 'ApiCalendarController', 'storeRoutine');
$router->post('/api/calendar/routine/update', 'ApiCalendarController', 'updateRoutine');
$router->post('/api/calendar/routine/disable', 'ApiCalendarController', 'disableRoutine');
$router->get('/api/catalogs/platforms/list', 'ApiCatalogController', 'platformList');
$router->get('/api/catalogs/platforms', 'ApiCatalogController', 'platforms');
$router->get('/api/catalogs/platforms/active', 'ApiCatalogController', 'activePlatforms');
$router->get('/api/catalogs/languages/list', 'ApiCatalogController', 'languageList');
$router->get('/api/catalogs/languages', 'ApiCatalogController', 'languages');
$router->get('/api/catalogs/languages/active', 'ApiCatalogController', 'activeLanguages');
$router->get('/api/dashboard/summary', 'ApiDashboardController', 'summary');
$router->get('/api/reports', 'ApiReportController', 'index');
$router->get('/api/challenges/list', 'ApiChallengeController', 'list');
$router->post('/api/challenges/manual', 'ApiChallengeController', 'manual');
$router->get('/api/goals/list', 'ApiGoalController', 'list');
$router->post('/api/platforms/save', 'ApiPlatformController', 'save');
$router->post('/api/platforms/deactivate', 'ApiPlatformController', 'deactivate');
$router->post('/api/platforms/activate', 'ApiPlatformController', 'activate');
$router->post('/api/languages/save', 'ApiLanguageController', 'save');
$router->post('/api/languages/deactivate', 'ApiLanguageController', 'deactivate');
$router->post('/api/languages/activate', 'ApiLanguageController', 'activate');
$router->post('/api/goals/save', 'ApiGoalController', 'save');
$router->post('/api/goals/deactivate', 'ApiGoalController', 'deactivate');
$router->get('/api/notifications/list', 'ApiNotificationController', 'list');
$router->post('/api/notifications/mark-read', 'ApiNotificationController', 'markRead');
$router->post('/api/notifications/delete', 'ApiNotificationController', 'delete');
$router->post('/api/user/update', 'ApiUserController', 'update');
$router->post('/api/user/change-password', 'ApiUserController', 'changePassword');
$router->post('/api/user/change-theme', 'ApiUserController', 'changeTheme');

$router->get('/plataformas', 'PlatformController', 'index');
$router->post('/plataformas/guardar', 'PlatformController', 'save');
$router->post('/plataformas/desactivar', 'PlatformController', 'deactivate');
$router->post('/plataformas/activar', 'PlatformController', 'activate');

$router->get('/lenguajes', 'LanguageController', 'index');
$router->post('/lenguajes/guardar', 'LanguageController', 'save');
$router->post('/lenguajes/desactivar', 'LanguageController', 'deactivate');
$router->post('/lenguajes/activar', 'LanguageController', 'activate');

$router->get('/metas', 'GoalController', 'index');
$router->post('/metas/guardar', 'GoalController', 'save');
$router->post('/metas/desactivar', 'GoalController', 'deactivate');
$router->get('/reportes', 'ReportController', 'index');
$router->get('/notificaciones', 'NotificationController', 'index');
$router->post('/notificaciones/marcar-leida', 'NotificationController', 'markRead');
$router->post('/notificaciones/eliminar', 'NotificationController', 'delete');

$router->get('/usuario', 'UserController', 'index');
$router->post('/usuario/actualizar', 'UserController', 'update');
$router->post('/usuario/cambiar-password', 'UserController', 'changePassword');
$router->post('/usuario/cambiar-tema', 'UserController', 'changeTheme');

$router->get('/seguridad', 'SecurityLogController', 'index');

return $router;
