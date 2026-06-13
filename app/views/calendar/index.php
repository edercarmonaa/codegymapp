<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Calendario</h1>
        <p class="text-body-secondary mb-0">Programación mensual, semanal y diaria</p>
    </div>
</div>

<div class="border rounded-2 p-3">
    <div id="calendar" data-csrf-token="<?= e(csrf_token()) ?>"></div>
</div>

<?php require __DIR__ . '/modal_challenge.php'; ?>
<?php require __DIR__ . '/modal_routine.php'; ?>

<link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.14/index.global.min.js"></script>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const el = document.getElementById('calendar');
    if (!el || typeof FullCalendar === 'undefined') return;
    const modalEl = document.getElementById('challengeModal');
    const modal = modalEl ? new bootstrap.Modal(modalEl) : null;
    const form = document.getElementById('challengeForm');
    const modalTitle = document.getElementById('challengeModalTitle');
    const alertBox = document.getElementById('challengeAlert');
    const submitButton = document.getElementById('challengeSubmitButton');
    const scheduledDate = document.getElementById('challengeScheduledDate');
    const scheduledDateLabel = document.getElementById('challengeScheduledDateLabel');
    const platform = document.getElementById('challengePlatform');
    const csrfToken = el.dataset.csrfToken || '';

    const showMessage = (message, type = 'success') => {
        const wrapper = document.createElement('div');
        wrapper.className = `alert alert-${type} alert-dismissible fade show`;
        wrapper.setAttribute('role', 'alert');
        wrapper.innerHTML = `${message}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>`;
        document.querySelector('.app-shell')?.prepend(wrapper);
        window.setTimeout(() => bootstrap.Alert.getOrCreateInstance(wrapper).close(), 3500);
    };

    const showFormError = (message) => {
        if (!alertBox) return;
        alertBox.textContent = message;
        alertBox.classList.remove('d-none');
    };

    const resetCreateForm = (date) => {
        form?.reset();
        alertBox?.classList.add('d-none');
        if (modalTitle) modalTitle.textContent = 'Crear reto';
        if (submitButton) submitButton.classList.remove('d-none');
        if (scheduledDate) scheduledDate.value = date;
        if (scheduledDateLabel) scheduledDateLabel.value = date;
        if (platform) platform.disabled = false;
    };

    const showEventDetails = (event) => {
        alertBox?.classList.add('d-none');
        if (modalTitle) modalTitle.textContent = event.title;
        if (submitButton) submitButton.classList.add('d-none');
        if (scheduledDate) scheduledDate.value = event.startStr.substring(0, 10);
        if (scheduledDateLabel) scheduledDateLabel.value = event.startStr.substring(0, 10);
        if (platform) {
            platform.disabled = true;
            platform.value = '';
        }
        modal?.show();
    };

    const calendar = new FullCalendar.Calendar(el, {
        initialView: 'dayGridMonth',
        locale: 'es',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        editable: true,
        eventStartEditable: true,
        events: '/api/calendar/events',
        dateClick(info) {
            resetCreateForm(info.dateStr);
            modal?.show();
        },
        eventClick(info) {
            showEventDetails(info.event);
        },
        eventAllow(dropInfo, draggedEvent) {
            return draggedEvent.extendedProps.status === 'pending' && dropInfo.start >= new Date(new Date().toDateString());
        },
        async eventDrop(info) {
            try {
                const response = await fetch('/api/calendar/update-date', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-Token': csrfToken
                    },
                    body: JSON.stringify({
                        id: info.event.id,
                        scheduled_date: info.event.startStr.substring(0, 10)
                    })
                });
                const payload = await response.json();
                if (!response.ok || !payload.ok) {
                    info.revert();
                    showMessage(payload.message || 'No se pudo reprogramar el reto.', 'danger');
                    return;
                }
                showMessage(payload.message || 'Reto reprogramado.');
                calendar.refetchEvents();
            } catch (error) {
                info.revert();
                showMessage('No se pudo reprogramar el reto.', 'danger');
            }
        }
    });
    calendar.render();

    form?.addEventListener('submit', async (event) => {
        event.preventDefault();
        alertBox?.classList.add('d-none');
        if (submitButton) submitButton.disabled = true;

        try {
            const response = await fetch('/api/calendar/store', {
                method: 'POST',
                body: new FormData(form)
            });
            const payload = await response.json();
            if (!response.ok || !payload.ok) {
                showFormError(payload.message || 'No se pudo guardar el reto.');
                return;
            }
            modal?.hide();
            showMessage(payload.message || 'Reto guardado correctamente.');
            calendar.refetchEvents();
        } catch (error) {
            showFormError('No se pudo guardar el reto.');
        } finally {
            if (submitButton) submitButton.disabled = false;
        }
    });
});
</script>
