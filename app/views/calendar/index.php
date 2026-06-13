<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Calendario</h1>
        <p class="text-body-secondary mb-0">Programación mensual, semanal y diaria</p>
    </div>
</div>

<div class="border rounded-2 p-3">
    <div id="calendar"></div>
</div>

<?php require __DIR__ . '/modal_challenge.php'; ?>
<?php require __DIR__ . '/modal_routine.php'; ?>

<link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('calendar');
    if (!el || typeof FullCalendar === 'undefined') return;
    const calendar = new FullCalendar.Calendar(el, {
        initialView: 'dayGridMonth',
        locale: 'es',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        editable: true,
        events: []
    });
    calendar.render();
});
</script>
