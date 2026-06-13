<?php

declare(strict_types=1);

$router = new Router();

$router->get('/', 'DashboardController', 'index');
$router->get('/login', 'AuthController', 'showLogin', false);
$router->post('/login', 'AuthController', 'login', false);
$router->get('/logout', 'AuthController', 'logout');

$router->get('/dashboard', 'DashboardController', 'index');
$router->get('/calendario', 'CalendarController', 'index');
$router->get('/retos', 'ChallengeController', 'index');

$router->get('/api/calendar/events', 'ApiCalendarController', 'events');
$router->get('/api/calendar/challenge', 'ApiCalendarController', 'challenge');
$router->post('/api/calendar/store', 'ApiCalendarController', 'store');
$router->post('/api/calendar/save-details', 'ApiCalendarController', 'saveDetails');
$router->post('/api/calendar/complete', 'ApiCalendarController', 'complete');
$router->post('/api/calendar/miss', 'ApiCalendarController', 'miss');
$router->post('/api/calendar/cancel', 'ApiCalendarController', 'cancel');
$router->post('/api/calendar/update-date', 'ApiCalendarController', 'updateDate');

$router->get('/plataformas', 'PlatformController', 'index');
$router->post('/plataformas/guardar', 'PlatformController', 'save');
$router->post('/plataformas/desactivar', 'PlatformController', 'deactivate');
$router->post('/plataformas/activar', 'PlatformController', 'activate');

$router->get('/lenguajes', 'LanguageController', 'index');
$router->post('/lenguajes/guardar', 'LanguageController', 'save');
$router->post('/lenguajes/desactivar', 'LanguageController', 'deactivate');
$router->post('/lenguajes/activar', 'LanguageController', 'activate');

$router->get('/metas', 'GoalController', 'index');
$router->get('/reportes', 'ReportController', 'index');
$router->get('/notificaciones', 'NotificationController', 'index');

$router->get('/usuario', 'UserController', 'index');
$router->post('/usuario/actualizar', 'UserController', 'update');
$router->post('/usuario/cambiar-password', 'UserController', 'changePassword');
$router->post('/usuario/cambiar-tema', 'UserController', 'changeTheme');

$router->get('/seguridad', 'SecurityLogController', 'index');

return $router;
