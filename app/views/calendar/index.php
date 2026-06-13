<div class="d-flex align-items-center justify-content-between mb-4">
    <div>
        <h1 class="h3 mb-1">Calendario</h1>
        <p class="text-body-secondary mb-0">Programación mensual, semanal y diaria</p>
    </div>
    <button class="btn btn-outline-primary" type="button" data-bs-toggle="modal" data-bs-target="#routineModal">Rutinas</button>
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
    const routineModalEl = document.getElementById('routineModal');
    const routineModal = routineModalEl ? new bootstrap.Modal(routineModalEl) : null;
    const routineForm = document.getElementById('routineForm');
    const routineFrequency = document.getElementById('routineFrequency');
    const routineId = document.getElementById('routineId');
    const routinePlatform = document.getElementById('routinePlatform');
    const routineStartDate = document.getElementById('routineStartDate');
    const routineEndDate = document.getElementById('routineEndDate');
    const routineMonthDay = document.getElementById('routineMonthDay');
    const routineWeekly = Array.from(document.querySelectorAll('.routine-weekly'));
    const routineWeekDayChecks = Array.from(document.querySelectorAll('input[name="week_days[]"]'));
    const routineMonthly = Array.from(document.querySelectorAll('.routine-monthly'));
    const routineAlert = document.getElementById('routineAlert');
    const routineSubmitButton = document.getElementById('routineSubmitButton');
    const routineResetButton = document.getElementById('routineResetButton');
    const form = document.getElementById('challengeForm');
    const modalTitle = document.getElementById('challengeModalTitle');
    const alertBox = document.getElementById('challengeAlert');
    const submitButton = document.getElementById('challengeSubmitButton');
    const completeButton = document.getElementById('challengeCompleteButton');
    const missButton = document.getElementById('challengeMissButton');
    const cancelButton = document.getElementById('challengeCancelButton');
    const challengeId = document.getElementById('challengeId');
    const scheduledDate = document.getElementById('challengeScheduledDate');
    const scheduledDateLabel = document.getElementById('challengeScheduledDateLabel');
    const platform = document.getElementById('challengePlatform');
    const title = document.getElementById('challengeTitle');
    const difficulty = document.getElementById('challengeDifficulty');
    const timeSpent = document.getElementById('challengeTime');
    const challengeUrl = document.getElementById('challengeUrl');
    const notes = document.getElementById('challengeNotes');
    const githubLinks = document.getElementById('challengeGithubLinks');
    const statusBadge = document.getElementById('challengeStatusBadge');
    const dataTab = document.getElementById('challengeDataTab');
    const languageChecks = Array.from(document.querySelectorAll('.challenge-language'));
    const editableFields = Array.from(document.querySelectorAll('#challengeForm input, #challengeForm select, #challengeForm textarea'));
    const editOnly = Array.from(document.querySelectorAll('.edit-only'));
    const editActions = Array.from(document.querySelectorAll('.edit-actions'));
    const csrfToken = el.dataset.csrfToken || '';
    let currentMode = 'create';

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

    const showRoutineError = (message) => {
        if (!routineAlert) return;
        routineAlert.textContent = message;
        routineAlert.classList.remove('d-none');
    };

    const updateRoutineFrequencyFields = () => {
        const value = routineFrequency?.value || 'daily';
        routineWeekly.forEach((node) => node.classList.toggle('d-none', value !== 'weekly'));
        routineMonthly.forEach((node) => node.classList.toggle('d-none', value !== 'monthly'));
    };

    const resetRoutineForm = () => {
        routineForm?.reset();
        if (routineId) routineId.value = '';
        routineAlert?.classList.add('d-none');
        if (routineSubmitButton) routineSubmitButton.textContent = 'Crear rutina';
        routineResetButton?.classList.add('d-none');
        updateRoutineFrequencyFields();
    };

    const fillRoutineForm = (button) => {
        routineAlert?.classList.add('d-none');
        if (routineId) routineId.value = button.dataset.id || '';
        if (routinePlatform) routinePlatform.value = button.dataset.platformId || '';
        if (routineFrequency) routineFrequency.value = button.dataset.frequencyType || 'daily';
        if (routineStartDate) routineStartDate.value = button.dataset.startDate || '';
        if (routineEndDate) routineEndDate.value = button.dataset.endDate || '';
        if (routineMonthDay) routineMonthDay.value = button.dataset.monthDay || '';
        const selectedDays = (button.dataset.weekDays || '').split(',').filter(Boolean);
        routineWeekDayChecks.forEach((check) => {
            check.checked = selectedDays.includes(check.value);
        });
        if (routineSubmitButton) routineSubmitButton.textContent = 'Guardar cambios';
        routineResetButton?.classList.remove('d-none');
        updateRoutineFrequencyFields();
    };

    const setEditVisible = (visible) => {
        editOnly.forEach((node) => node.classList.toggle('d-none', !visible));
        editActions.forEach((node) => node.classList.toggle('d-none', !visible));
    };

    const activateDataTab = () => {
        if (dataTab && window.bootstrap) {
            bootstrap.Tab.getOrCreateInstance(dataTab).show();
        }
    };

    const setFormLocked = (locked, allowCompletedEdit = false) => {
        editableFields.forEach((field) => {
            if (field.name === '_token' || field.id === 'challengeId') return;
            field.disabled = locked && !allowCompletedEdit;
        });
        if (platform && currentMode === 'edit') platform.disabled = true;
    };

    const setInactivePlatformOptions = (disabled) => {
        if (!platform) return;
        Array.from(platform.options).forEach((option) => {
            if (option.dataset.active === '0') {
                option.disabled = disabled;
            }
        });
    };

    const setStatusBadge = (status) => {
        const labels = {
            pending: 'Pendiente',
            completed: 'Cumplido',
            expired: 'Vencido',
            missed: 'No cumplido',
            cancelled: 'Cancelado'
        };
        const classes = {
            pending: 'text-bg-primary',
            completed: 'text-bg-success',
            expired: 'text-bg-secondary',
            missed: 'text-bg-danger',
            cancelled: 'text-bg-dark'
        };
        if (!statusBadge) return;
        statusBadge.className = `badge ${classes[status] || 'text-bg-secondary'}`;
        statusBadge.textContent = labels[status] || status;
    };

    const resetCreateForm = (date) => {
        currentMode = 'create';
        form?.reset();
        alertBox?.classList.add('d-none');
        activateDataTab();
        setEditVisible(false);
        setFormLocked(false);
        setInactivePlatformOptions(true);
        if (modalTitle) modalTitle.textContent = 'Crear reto';
        if (challengeId) challengeId.value = '';
        if (submitButton) {
            submitButton.classList.remove('d-none');
            submitButton.textContent = 'Guardar reto';
        }
        if (scheduledDate) scheduledDate.value = date;
        if (scheduledDateLabel) scheduledDateLabel.value = date;
        if (platform) platform.disabled = false;
    };

    const fillChallengeForm = (challenge) => {
        currentMode = 'edit';
        form?.reset();
        alertBox?.classList.add('d-none');
        activateDataTab();
        setEditVisible(true);
        if (modalTitle) modalTitle.textContent = challenge.platform_name + (challenge.title ? ' - ' + challenge.title : '');
        if (challengeId) challengeId.value = challenge.id || '';
        if (scheduledDate) scheduledDate.value = challenge.scheduled_date || '';
        if (scheduledDateLabel) scheduledDateLabel.value = challenge.scheduled_date || '';
        if (platform) {
            setInactivePlatformOptions(false);
            platform.value = challenge.platform_id || '';
            platform.disabled = true;
        }
        if (title) title.value = challenge.title || '';
        if (difficulty) difficulty.value = challenge.difficulty || '';
        if (timeSpent) timeSpent.value = challenge.time_spent_minutes || '';
        if (challengeUrl) challengeUrl.value = challenge.challenge_url || '';
        if (notes) notes.value = challenge.notes || '';
        if (githubLinks) githubLinks.value = (challenge.github_links || []).map((link) => link.github_url).join('\n');
        languageChecks.forEach((check) => {
            check.checked = (challenge.language_ids || []).map(Number).includes(Number(check.value));
        });
        setStatusBadge(challenge.status);

        const isClosed = ['missed', 'cancelled'].includes(challenge.status);
        const isCompleted = challenge.status === 'completed';
        setFormLocked(isClosed, isCompleted);
        if (submitButton) {
            submitButton.classList.toggle('d-none', isClosed);
            submitButton.textContent = isCompleted ? 'Guardar correcciones' : 'Guardar datos';
        }
        [completeButton, missButton, cancelButton].forEach((button) => button?.classList.toggle('d-none', isClosed || isCompleted));
        modal?.show();
    };

    const showEventDetails = async (event) => {
        try {
            const response = await fetch(`/api/calendar/challenge?id=${encodeURIComponent(event.id)}`);
            const payload = await response.json();
            if (!response.ok || !payload.ok) {
                showMessage(payload.message || 'No se pudo cargar el reto.', 'danger');
                return;
            }
            fillChallengeForm(payload.challenge);
        } catch (error) {
            showMessage('No se pudo cargar el reto.', 'danger');
        }
    };

    const postForm = async (url) => {
        const response = await fetch(url, {
            method: 'POST',
            body: new FormData(form)
        });
        const payload = await response.json();
        if (!response.ok || !payload.ok) {
            throw new Error(payload.message || 'No se pudo guardar.');
        }
        return payload;
    };

    const closeAction = async (url, saveFirst = false) => {
        alertBox?.classList.add('d-none');
        try {
            if (saveFirst) {
                await postForm('/api/calendar/save-details');
            }
            const payload = await postForm(url);
            modal?.hide();
            showMessage(payload.message || 'Acción realizada.');
            calendar.refetchEvents();
        } catch (error) {
            showFormError(error.message);
        }
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
            const payload = await postForm(currentMode === 'create' ? '/api/calendar/store' : '/api/calendar/save-details');
            modal?.hide();
            showMessage(payload.message || 'Reto guardado.');
            calendar.refetchEvents();
        } catch (error) {
            showFormError(error.message || 'No se pudo guardar el reto.');
        } finally {
            if (submitButton) submitButton.disabled = false;
        }
    });

    completeButton?.addEventListener('click', () => closeAction('/api/calendar/complete', true));
    missButton?.addEventListener('click', () => closeAction('/api/calendar/miss'));
    cancelButton?.addEventListener('click', async () => {
        if (await window.CodeGymConfirm('¿Cancelar este reto?')) {
            closeAction('/api/calendar/cancel');
        }
    });

    routineFrequency?.addEventListener('change', updateRoutineFrequencyFields);
    routineResetButton?.addEventListener('click', resetRoutineForm);
    updateRoutineFrequencyFields();

    routineForm?.addEventListener('submit', async (event) => {
        event.preventDefault();
        routineAlert?.classList.add('d-none');
        if (routineSubmitButton) routineSubmitButton.disabled = true;
        try {
            const response = await fetch(routineId?.value ? '/api/calendar/routine/update' : '/api/calendar/routine/store', {
                method: 'POST',
                body: new FormData(routineForm)
            });
            const payload = await response.json();
            if (!response.ok || !payload.ok) {
                showRoutineError(payload.message || 'No se pudo guardar la rutina.');
                return;
            }
            routineModal?.hide();
            showMessage(payload.message || 'Rutina guardada.');
            calendar.refetchEvents();
            window.setTimeout(() => window.location.reload(), 800);
        } catch (error) {
            showRoutineError('No se pudo guardar la rutina.');
        } finally {
            if (routineSubmitButton) routineSubmitButton.disabled = false;
        }
    });

    document.querySelectorAll('.routine-disable-button').forEach((button) => {
        button.addEventListener('click', async () => {
            if (!await window.CodeGymConfirm('¿Desactivar esta rutina?')) {
                return;
            }
            const data = new FormData();
            data.append('_token', csrfToken);
            data.append('id', button.dataset.id || '');
            button.disabled = true;
            try {
                const response = await fetch('/api/calendar/routine/disable', { method: 'POST', body: data });
                const payload = await response.json();
                if (!response.ok || !payload.ok) {
                    showRoutineError(payload.message || 'No se pudo desactivar la rutina.');
                    button.disabled = false;
                    return;
                }
                showMessage(payload.message || 'Rutina desactivada.');
                window.setTimeout(() => window.location.reload(), 800);
            } catch (error) {
                showRoutineError('No se pudo desactivar la rutina.');
                button.disabled = false;
            }
        });
    });

    document.querySelectorAll('.routine-edit-button').forEach((button) => {
        button.addEventListener('click', () => fillRoutineForm(button));
    });
});
</script>
