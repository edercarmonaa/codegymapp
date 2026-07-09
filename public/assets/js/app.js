window.CodeGymConfirm = (message = '¿Deseas continuar?') => new Promise((resolve) => {
    const modalEl = document.getElementById('confirmModal');
    const messageEl = document.getElementById('confirmModalMessage');
    const acceptButton = document.getElementById('confirmModalAccept');
    if (!modalEl || !acceptButton || !window.bootstrap) {
        resolve(window.confirm(message));
        return;
    }

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    const cleanup = () => {
        acceptButton.removeEventListener('click', onAccept);
        modalEl.removeEventListener('hidden.bs.modal', onHide);
    };
    const onAccept = () => {
        cleanup();
        modal.hide();
        resolve(true);
    };
    const onHide = () => {
        cleanup();
        resolve(false);
    };

    if (messageEl) {
        messageEl.textContent = message;
    }
    acceptButton.addEventListener('click', onAccept, { once: true });
    modalEl.addEventListener('hidden.bs.modal', onHide, { once: true });
    modal.show();
});

document.addEventListener('DOMContentLoaded', () => {
    const tablePanel = document.getElementById('tablePanel');
    const csrfToken = () => document.querySelector('input[name="_token"]')?.value || '';
    const catalogEndpoint = (panel, url = null) => {
        const base = panel?.dataset?.catalogUrl || '';
        if (!base) return '';
        const source = url ? new URL(url, window.location.origin) : new URL(window.location.href);
        return base + source.search;
    };
    const makeElement = (tag, className = '', text = '') => {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== '') node.textContent = text;
        return node;
    };
    const cssEscape = (value) => window.CSS?.escape ? window.CSS.escape(String(value)) : String(value).replace(/[^a-zA-Z0-9_-]/g, '\\$&');
    const catalogUrl = (pagination, params) => {
        const query = new URLSearchParams();
        query.set('page', String(params.page || pagination.page || 1));
        query.set('per_page', String(params.per_page || pagination.per_page || 20));
        query.set('sort', String(params.sort || pagination.sort || 'name'));
        query.set('dir', String(params.dir || pagination.dir || 'asc'));
        return '?' + query.toString();
    };
    const bulkSelectAll = (tableName) => {
        const input = document.createElement('input');
        input.className = 'form-check-input';
        input.type = 'checkbox';
        input.dataset.bulkSelectAll = tableName;
        return input;
    };
    const bulkItem = (tableName, id) => {
        const input = document.createElement('input');
        input.className = 'form-check-input';
        input.type = 'checkbox';
        input.value = String(id || 0);
        input.dataset.bulkItem = tableName;
        return input;
    };
    const renderBulkToolbar = (tableName, actions = []) => {
        const toolbar = makeElement('div', 'codegym-bulk-toolbar d-flex flex-wrap gap-2 align-items-center');
        const checkWrap = makeElement('div', 'form-check mb-0');
        const check = bulkSelectAll(tableName);
        check.id = `bulk_select_${tableName}_${Math.random().toString(36).slice(2)}`;
        const label = makeElement('label', 'form-check-label small', 'Seleccionar visibles');
        label.htmlFor = check.id;
        checkWrap.appendChild(check);
        checkWrap.appendChild(label);
        toolbar.appendChild(checkWrap);
        const counter = makeElement('span', 'badge rounded-pill text-bg-secondary', '0 seleccionados');
        counter.dataset.bulkCount = tableName;
        toolbar.appendChild(counter);

        actions.forEach((action) => {
            const button = makeElement('button', `btn btn-sm ${action.className || 'btn-outline-secondary'}`, action.label);
            button.type = 'button';
            button.disabled = true;
            button.dataset.bulkAction = tableName;
            button.dataset.bulkUrl = action.url;
            button.dataset.bulkRefresh = action.refresh || tableName;
            if (action.confirm) button.dataset.confirm = action.confirm;
            toolbar.appendChild(button);
        });
        return toolbar;
    };
    const bulkConfig = {
        challenges: [
            { label: 'No cumplido', url: '/api/calendar/miss', refresh: 'challenges', className: 'btn-outline-warning', confirm: '¿Marcar los retos seleccionados como no cumplidos?' },
            { label: 'Cancelar', url: '/api/calendar/cancel', refresh: 'challenges', className: 'btn-outline-danger', confirm: '¿Cancelar los retos seleccionados?' }
        ],
        platforms: [
            { label: 'Activar', url: '/api/platforms/activate', refresh: 'platforms', className: 'btn-outline-success' },
            { label: 'Desactivar', url: '/api/platforms/deactivate', refresh: 'platforms', className: 'btn-outline-secondary', confirm: '¿Desactivar las plataformas seleccionadas?' }
        ],
        languages: [
            { label: 'Activar', url: '/api/languages/activate', refresh: 'languages', className: 'btn-outline-success' },
            { label: 'Desactivar', url: '/api/languages/deactivate', refresh: 'languages', className: 'btn-outline-secondary', confirm: '¿Desactivar los lenguajes seleccionados?' }
        ],
        goals: [
            { label: 'Desactivar', url: '/api/goals/deactivate', refresh: 'goals', className: 'btn-outline-secondary', confirm: '¿Desactivar las metas seleccionadas?' }
        ],
        notifications: [
            { label: 'Marcar leídas', url: '/api/notifications/mark-read', refresh: 'notifications', className: 'btn-outline-primary' },
            { label: 'Eliminar', url: '/api/notifications/delete', refresh: 'notifications', className: 'btn-outline-danger', confirm: '¿Eliminar las notificaciones seleccionadas?' }
        ]
    };
    const renderCatalogPagination = (pagination, tableName = '') => {
        const wrapper = makeElement('div', 'd-flex flex-wrap gap-2 align-items-center justify-content-between my-3');
        const left = makeElement('div', 'd-flex flex-wrap gap-3 align-items-center');
        left.appendChild(makeElement('div', 'text-body-secondary small', `${pagination.total || 0} registros`));
        if (tableName && bulkConfig[tableName]) {
            left.appendChild(renderBulkToolbar(tableName, bulkConfig[tableName]));
        }
        wrapper.appendChild(left);

        const controls = makeElement('div', 'd-flex flex-wrap gap-2 align-items-center');
        controls.appendChild(makeElement('label', 'form-label mb-0 small', 'Por página'));
        const select = makeElement('select', 'form-select form-select-sm w-auto table-per-page');
        [10, 20, 25, 50].forEach((option) => {
            const optionNode = document.createElement('option');
            optionNode.value = catalogUrl(pagination, { page: 1, per_page: option });
            optionNode.textContent = String(option);
            optionNode.selected = Number(pagination.per_page || 20) === option;
            select.appendChild(optionNode);
        });
        controls.appendChild(select);

        const nav = document.createElement('nav');
        nav.setAttribute('aria-label', 'Paginación');
        const list = makeElement('ul', 'pagination pagination-sm mb-0');
        const page = Number(pagination.page || 1);
        const pages = Number(pagination.pages || 1);
        [
            ['Anterior', Math.max(1, page - 1), page <= 1],
            [`${page} / ${pages}`, page, true],
            ['Siguiente', Math.min(pages, page + 1), page >= pages]
        ].forEach(([label, targetPage, disabled]) => {
            const item = makeElement('li', `page-item ${disabled ? 'disabled' : ''}`.trim());
            if (label.includes('/')) {
                item.appendChild(makeElement('span', 'page-link', label));
            } else {
                const link = makeElement('a', 'page-link', label);
                link.href = catalogUrl(pagination, { page: targetPage });
                link.dataset.tableLink = '1';
                item.appendChild(link);
            }
            list.appendChild(item);
        });
        nav.appendChild(list);
        controls.appendChild(nav);
        wrapper.appendChild(controls);
        return wrapper;
    };
    const sortLink = (label, sort, dir = 'asc') => {
        const link = document.createElement('a');
        link.href = `?sort=${encodeURIComponent(sort)}&dir=${encodeURIComponent(dir)}`;
        link.textContent = label;
        return link;
    };
    const hiddenToken = () => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = '_token';
        input.value = csrfToken();
        return input;
    };
    const hiddenId = (id) => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'id';
        input.value = String(id || 0);
        return input;
    };
    const statusBadge = (active, activeLabel, inactiveLabel) => {
        const badge = makeElement('span', `badge ${active ? 'text-bg-success' : 'text-bg-secondary'}`, active ? activeLabel : inactiveLabel);
        return badge;
    };
    const actionForm = (row, type) => {
        const active = Boolean(row.is_active);
        const form = makeElement('form', 'd-inline');
        form.method = 'post';
        form.action = `/api/${type}/${active ? 'deactivate' : 'activate'}`;
        form.dataset.apiForm = '1';
        form.dataset.apiRefreshCatalog = type === 'platforms' ? 'platforms' : 'languages';
        if (active) {
            form.dataset.confirm = type === 'platforms' ? '¿Desactivar esta plataforma?' : '¿Desactivar este lenguaje?';
        }
        form.appendChild(hiddenToken());
        form.appendChild(hiddenId(row.id));
        const button = makeElement('button', 'btn btn-sm btn-outline-secondary', active ? 'Desactivar' : 'Activar');
        form.appendChild(button);
        return form;
    };
    const postActionForm = (action, id, buttonClass, buttonText, refreshCatalog, confirm = '') => {
        const form = makeElement('form', 'd-inline');
        form.method = 'post';
        form.action = action;
        form.dataset.apiForm = '1';
        form.dataset.apiRefreshCatalog = refreshCatalog;
        if (confirm) form.dataset.confirm = confirm;
        form.appendChild(hiddenToken());
        form.appendChild(hiddenId(id));
        form.appendChild(makeElement('button', buttonClass, buttonText));
        return form;
    };
    const goalUnit = (goalType) => {
        if (goalType === 'practice_time') return 'min';
        if (goalType === 'streak') return 'días';
        return 'retos';
    };
    const renderPlatformsTable = (panel, payload) => {
        clearNode(panel);
        const pagination = payload.pagination || {};
        panel.appendChild(renderCatalogPagination(pagination, 'platforms'));
        const responsive = makeElement('div', 'table-responsive');
        const table = makeElement('table', 'table align-middle');
        const thead = document.createElement('thead');
        const headRow = document.createElement('tr');
        const selectHead = makeElement('th', 'text-center');
        selectHead.appendChild(bulkSelectAll('platforms'));
        headRow.appendChild(selectHead);
        ['Nombre', 'URL', 'Descripción', 'Estado', 'Acciones'].forEach((label, index) => {
            const th = document.createElement('th');
            if (index === 0) th.appendChild(sortLink(label, 'name', 'asc'));
            else if (index === 3) th.appendChild(sortLink(label, 'is_active', 'desc'));
            else th.textContent = label;
            if (index === 4) th.className = 'text-end';
            headRow.appendChild(th);
        });
        thead.appendChild(headRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        (payload.platforms || []).forEach((platform) => {
            const row = document.createElement('tr');
            const selectCell = makeElement('td', 'text-center');
            selectCell.appendChild(bulkItem('platforms', platform.id));
            row.appendChild(selectCell);
            row.appendChild(makeElement('td', '', platform.name || ''));
            const urlCell = document.createElement('td');
            if (platform.url) {
                const link = document.createElement('a');
                link.href = platform.url;
                link.target = '_blank';
                link.rel = 'noopener';
                link.textContent = 'Abrir';
                urlCell.appendChild(link);
            }
            row.appendChild(urlCell);
            row.appendChild(makeElement('td', '', platform.description || ''));
            const status = document.createElement('td');
            status.appendChild(statusBadge(Boolean(platform.is_active), 'Activa', 'Inactiva'));
            row.appendChild(status);
            const actions = makeElement('td', 'text-end');
            actions.appendChild(actionForm(platform, 'platforms'));
            row.appendChild(actions);
            tbody.appendChild(row);
        });
        table.appendChild(tbody);
        responsive.appendChild(table);
        panel.appendChild(responsive);
        panel.appendChild(renderCatalogPagination(pagination, 'languages'));
    };
    const renderLanguagesTable = (panel, payload) => {
        clearNode(panel);
        const pagination = payload.pagination || {};
        panel.appendChild(renderCatalogPagination(pagination));
        const responsive = makeElement('div', 'table-responsive');
        const table = makeElement('table', 'table align-middle');
        const thead = document.createElement('thead');
        const headRow = document.createElement('tr');
        const selectHead = makeElement('th', 'text-center');
        selectHead.appendChild(bulkSelectAll('languages'));
        headRow.appendChild(selectHead);
        ['Nombre', 'Estado', 'Acciones'].forEach((label, index) => {
            const th = document.createElement('th');
            if (index === 0) th.appendChild(sortLink(label, 'name', 'asc'));
            else if (index === 1) th.appendChild(sortLink(label, 'is_active', 'desc'));
            else th.textContent = label;
            if (index === 2) th.className = 'text-end';
            headRow.appendChild(th);
        });
        thead.appendChild(headRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        (payload.languages || []).forEach((language) => {
            const row = document.createElement('tr');
            const selectCell = makeElement('td', 'text-center');
            selectCell.appendChild(bulkItem('languages', language.id));
            row.appendChild(selectCell);
            row.appendChild(makeElement('td', '', language.name || ''));
            const status = document.createElement('td');
            status.appendChild(statusBadge(Boolean(language.is_active), 'Activo', 'Inactivo'));
            row.appendChild(status);
            const actions = makeElement('td', 'text-end');
            actions.appendChild(actionForm(language, 'languages'));
            row.appendChild(actions);
            tbody.appendChild(row);
        });
        table.appendChild(tbody);
        responsive.appendChild(table);
        panel.appendChild(responsive);
        panel.appendChild(renderCatalogPagination(pagination, 'goals'));
    };
    const renderGoalsTable = (panel, payload) => {
        clearNode(panel);
        const pagination = payload.pagination || {};
        const goalTypes = payload.goalTypes || {};
        const periodTypes = payload.periodTypes || {};
        panel.appendChild(renderCatalogPagination(pagination));

        const responsive = makeElement('div', 'table-responsive');
        const table = makeElement('table', 'table align-middle table-hover');
        const thead = document.createElement('thead');
        const headRow = document.createElement('tr');
        const selectHead = makeElement('th', 'text-center');
        selectHead.appendChild(bulkSelectAll('goals'));
        headRow.appendChild(selectHead);
        [
            ['Meta', 'goal_type', 'asc'],
            ['Periodo', 'period_end', 'asc'],
            ['Alcance', '', ''],
            ['Avance', 'progress_percent', 'desc'],
            ['Estado', 'status', 'asc'],
            ['Acciones', '', '']
        ].forEach(([label, sort, dir], index) => {
            const th = document.createElement('th');
            if (sort) th.appendChild(sortLink(label, sort, dir));
            else th.textContent = label;
            if (index === 5) th.className = 'text-end';
            headRow.appendChild(th);
        });
        thead.appendChild(headRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        const goals = payload.goals || [];
        goals.forEach((goal) => {
            const row = document.createElement('tr');
            const unit = goalUnit(goal.goal_type);
            const selectCell = makeElement('td', 'text-center');
            selectCell.appendChild(bulkItem('goals', goal.id));
            row.appendChild(selectCell);

            const meta = document.createElement('td');
            appendText(meta, 'div', 'fw-semibold', goalTypes[goal.goal_type] || String(goal.goal_type || 'Meta'));
            appendText(meta, 'div', 'text-body-secondary small', `Objetivo: ${goal.target_value || 0} ${unit}`);
            row.appendChild(meta);

            const period = document.createElement('td');
            appendText(period, 'div', '', periodTypes[goal.period_type] || String(goal.period_type || 'Periodo'));
            appendText(period, 'div', 'text-body-secondary small', `${goal.period_start || ''} a ${goal.period_end || ''}`);
            row.appendChild(period);

            const scope = document.createElement('td');
            appendText(scope, 'div', '', goal.platform_name || 'Todas las plataformas');
            appendText(scope, 'div', 'text-body-secondary small', goal.language_name || 'Todos los lenguajes');
            row.appendChild(scope);

            const progressCell = document.createElement('td');
            progressCell.style.minWidth = '220px';
            const progressText = makeElement('div', 'd-flex justify-content-between small mb-1');
            appendText(progressText, 'span', '', `${goal.current_value || 0} / ${goal.target_value || 0} ${unit}`);
            appendText(progressText, 'span', '', `${goal.progress_percent || 0}%`);
            progressCell.appendChild(progressText);
            const progress = makeElement('div', 'progress');
            progress.setAttribute('role', 'progressbar');
            progress.setAttribute('aria-valuenow', String(goal.progress_percent || 0));
            progress.setAttribute('aria-valuemin', '0');
            progress.setAttribute('aria-valuemax', '100');
            const bar = makeElement('div', 'progress-bar');
            bar.style.width = `${Math.min(100, Number(goal.progress_percent || 0))}%`;
            progress.appendChild(bar);
            progressCell.appendChild(progress);
            row.appendChild(progressCell);

            const status = document.createElement('td');
            status.appendChild(statusBadge(goal.status === 'active', 'Activa', 'Cerrada'));
            if (goal.auto_renew) status.appendChild(makeElement('span', 'badge text-bg-info ms-1', 'Renovable'));
            row.appendChild(status);

            const actions = makeElement('td', 'text-end');
            if (goal.status === 'active') {
                actions.appendChild(postActionForm('/api/goals/deactivate', goal.id, 'btn btn-sm btn-outline-secondary', 'Desactivar', 'goals', '¿Desactivar esta meta?'));
            } else {
                actions.appendChild(makeElement('span', 'text-body-secondary', '-'));
            }
            row.appendChild(actions);
            tbody.appendChild(row);
        });
        if (goals.length === 0) {
            const row = document.createElement('tr');
            const cell = makeElement('td', 'text-body-secondary', 'No hay metas registradas.');
            cell.colSpan = 7;
            row.appendChild(cell);
            tbody.appendChild(row);
        }
        table.appendChild(tbody);
        responsive.appendChild(table);
        panel.appendChild(responsive);
        panel.appendChild(renderCatalogPagination(pagination, 'notifications'));
    };
    const renderNotificationsTable = (panel, payload) => {
        clearNode(panel);
        const pagination = payload.pagination || {};
        panel.appendChild(renderCatalogPagination(pagination));

        const responsive = makeElement('div', 'table-responsive');
        const table = makeElement('table', 'table align-middle table-hover');
        const thead = document.createElement('thead');
        const headRow = document.createElement('tr');
        const selectHead = makeElement('th', 'text-center');
        selectHead.appendChild(bulkSelectAll('notifications'));
        headRow.appendChild(selectHead);
        [
            ['Notificación', 'title', 'asc'],
            ['Estado', 'is_read', 'asc'],
            ['Fecha', 'created_at', 'desc'],
            ['Acciones', '', '']
        ].forEach(([label, sort, dir], index) => {
            const th = document.createElement('th');
            if (sort) th.appendChild(sortLink(label, sort, dir));
            else th.textContent = label;
            if (index === 3) th.className = 'text-end';
            headRow.appendChild(th);
        });
        thead.appendChild(headRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        const notifications = payload.notifications || [];
        notifications.forEach((notification) => {
            const row = document.createElement('tr');
            const selectCell = makeElement('td', 'text-center');
            selectCell.appendChild(bulkItem('notifications', notification.id));
            row.appendChild(selectCell);
            const content = document.createElement('td');
            appendText(content, 'div', 'fw-semibold', notification.title || '');
            appendText(content, 'div', 'text-body-secondary', notification.message || '');
            if (notification.action_url) {
                const link = makeElement('a', 'small', 'Abrir');
                link.href = safePath(notification.action_url, '/notificaciones');
                content.appendChild(link);
            }
            row.appendChild(content);

            const status = document.createElement('td');
            status.appendChild(makeElement(
                'span',
                `badge ${notification.is_read ? 'text-bg-secondary' : 'text-bg-primary'}`,
                notification.is_read ? 'Leída' : 'Pendiente'
            ));
            row.appendChild(status);

            const date = document.createElement('td');
            appendText(date, 'div', '', notification.created_at || '');
            if (notification.read_at) appendText(date, 'div', 'small text-body-secondary', `Leída: ${notification.read_at}`);
            row.appendChild(date);

            const actions = makeElement('td', 'text-end');
            if (!notification.is_read) {
                actions.appendChild(postActionForm('/api/notifications/mark-read', notification.id, 'btn btn-sm btn-outline-primary', 'Marcar leída', 'notifications'));
            } else {
                actions.appendChild(postActionForm('/api/notifications/delete', notification.id, 'btn btn-sm btn-outline-danger', 'Eliminar', 'notifications', '¿Eliminar esta notificación del historial?'));
            }
            row.appendChild(actions);
            tbody.appendChild(row);
        });
        if (notifications.length === 0) {
            const row = document.createElement('tr');
            const cell = makeElement('td', 'text-body-secondary', 'No hay notificaciones registradas.');
            cell.colSpan = 5;
            row.appendChild(cell);
            tbody.appendChild(row);
        }
        table.appendChild(tbody);
        responsive.appendChild(table);
        panel.appendChild(responsive);
        panel.appendChild(renderCatalogPagination(pagination, 'challenges'));
    };
    const renderChallengeFilters = (payload) => {
        const form = makeElement('form', 'row g-2 align-items-end mb-4');
        form.method = 'get';
        form.action = '/retos';

        const statusWrap = makeElement('div', 'col-12 col-md-3');
        const statusLabel = makeElement('label', 'form-label', 'Estado');
        statusLabel.htmlFor = 'status';
        statusWrap.appendChild(statusLabel);
        const statusSelect = makeElement('select', 'form-select');
        statusSelect.id = 'status';
        statusSelect.name = 'status';
        const allStatuses = document.createElement('option');
        allStatuses.value = '';
        allStatuses.textContent = 'Todos';
        statusSelect.appendChild(allStatuses);
        Object.entries(payload.statusLabels || {}).forEach(([value, label]) => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = String(label);
            option.selected = (payload.filters?.status || '') === value;
            statusSelect.appendChild(option);
        });
        statusWrap.appendChild(statusSelect);
        form.appendChild(statusWrap);

        const platformWrap = makeElement('div', 'col-12 col-md-4');
        const platformLabel = makeElement('label', 'form-label', 'Plataforma');
        platformLabel.htmlFor = 'platform_id';
        platformWrap.appendChild(platformLabel);
        const platformSelect = makeElement('select', 'form-select');
        platformSelect.id = 'platform_id';
        platformSelect.name = 'platform_id';
        const allPlatforms = document.createElement('option');
        allPlatforms.value = '0';
        allPlatforms.textContent = 'Todas';
        platformSelect.appendChild(allPlatforms);
        (payload.platforms || []).forEach((platform) => {
            const option = document.createElement('option');
            option.value = String(platform.id || 0);
            option.textContent = `${platform.name || ''}${platform.is_active ? '' : ' (inactiva)'}`;
            option.selected = Number(payload.filters?.platform_id || 0) === Number(platform.id || 0);
            platformSelect.appendChild(option);
        });
        platformWrap.appendChild(platformSelect);
        form.appendChild(platformWrap);

        const buttons = makeElement('div', 'col-12 col-md-auto');
        buttons.appendChild(makeElement('button', 'btn btn-outline-primary', 'Filtrar'));
        const clear = makeElement('a', 'btn btn-outline-secondary ms-1', 'Limpiar');
        clear.href = '/retos';
        clear.dataset.tableLink = '1';
        buttons.appendChild(clear);
        form.appendChild(buttons);
        return form;
    };
    const challengeSortLink = (label, sort, dir, filters = {}) => {
        const query = new URLSearchParams();
        if (filters.status) query.set('status', String(filters.status));
        if (Number(filters.platform_id || 0) > 0) query.set('platform_id', String(filters.platform_id));
        query.set('sort', sort);
        query.set('dir', dir);
        query.set('page', '1');
        const link = document.createElement('a');
        link.href = '?' + query.toString();
        link.textContent = label;
        return link;
    };
    const renderChallengesTable = (panel, payload) => {
        clearNode(panel);
        const pagination = payload.pagination || {};
        panel.appendChild(renderChallengeFilters(payload));
        panel.appendChild(renderCatalogPagination(pagination));

        const responsive = makeElement('div', 'table-responsive');
        const table = makeElement('table', 'table align-middle table-hover');
        const thead = document.createElement('thead');
        const headRow = document.createElement('tr');
        const selectHead = makeElement('th', 'text-center');
        selectHead.appendChild(bulkSelectAll('challenges'));
        headRow.appendChild(selectHead);
        [
            ['Fecha programada', 'scheduled_date', 'desc'],
            ['Plataforma', 'platform', 'asc'],
            ['Nombre del reto', '', ''],
            ['Estado', 'status', 'asc'],
            ['Dificultad', '', ''],
            ['Lenguajes', '', ''],
            ['Tiempo', 'time_spent_minutes', 'desc'],
            ['Fecha de cumplimiento', 'completed_date', 'desc'],
            ['GitHub', '', ''],
            ['Acciones', '', '']
        ].forEach(([label, sort, dir], index) => {
            const th = document.createElement('th');
            if (sort) th.appendChild(challengeSortLink(label, sort, dir, payload.filters || {}));
            else th.textContent = label;
            if (index === 9) th.className = 'text-end';
            headRow.appendChild(th);
        });
        thead.appendChild(headRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        const rows = payload.challenges || [];
        rows.forEach((challenge) => {
            const row = document.createElement('tr');
            const status = String(challenge.status || '');
            const selectCell = makeElement('td', 'text-center');
            selectCell.appendChild(bulkItem('challenges', challenge.id));
            row.appendChild(selectCell);
            const isLate = status === 'completed'
                && challenge.completed_date
                && challenge.scheduled_date
                && String(challenge.completed_date) > String(challenge.scheduled_date);

            const scheduled = document.createElement('td');
            scheduled.appendChild(document.createTextNode(challenge.scheduled_date || ''));
            if (challenge.is_rescheduled) scheduled.appendChild(makeElement('span', 'badge text-bg-info ms-1', 'Reprogramado'));
            if (challenge.origin === 'manual') scheduled.appendChild(makeElement('span', 'badge text-bg-success ms-1', 'Manual'));
            row.appendChild(scheduled);

            row.appendChild(makeElement('td', '', challenge.platform_name || ''));

            const title = document.createElement('td');
            appendText(title, 'div', 'fw-semibold', challenge.title || 'Pendiente por detallar');
            if (challenge.challenge_url) {
                const link = makeElement('a', 'small', 'Ver reto');
                link.href = challenge.challenge_url;
                link.target = '_blank';
                link.rel = 'noopener';
                title.appendChild(link);
            }
            row.appendChild(title);

            const statusCell = document.createElement('td');
            statusCell.appendChild(makeElement('span', `badge ${payload.statusBadgeClasses?.[status] || 'text-bg-secondary'}`, payload.statusLabels?.[status] || status));
            if (isLate) statusCell.appendChild(makeElement('span', 'badge text-bg-warning ms-1', 'Fuera de fecha'));
            row.appendChild(statusCell);

            row.appendChild(makeElement('td', '', challenge.difficulty || '-'));
            row.appendChild(makeElement('td', '', challenge.language_names || '-'));
            row.appendChild(makeElement('td', '', Number(challenge.time_spent_minutes || 0) > 0 ? `${challenge.time_spent_minutes} min` : '-'));
            row.appendChild(makeElement('td', '', challenge.completed_date || '-'));

            const github = document.createElement('td');
            if (Array.isArray(challenge.github_urls) && challenge.github_urls.length > 0) {
                const links = makeElement('div', 'd-flex flex-column gap-1');
                challenge.github_urls.forEach((url, index) => {
                    const link = document.createElement('a');
                    link.href = url;
                    link.target = '_blank';
                    link.rel = 'noopener';
                    link.textContent = `Solución ${index + 1}`;
                    links.appendChild(link);
                });
                github.appendChild(links);
            } else {
                github.appendChild(makeElement('span', 'text-body-secondary', '-'));
            }
            row.appendChild(github);

            const actions = makeElement('td', 'text-end');
            const edit = makeElement('a', 'btn btn-sm btn-outline-primary', 'Editar');
            edit.href = '/calendario';
            actions.appendChild(edit);
            row.appendChild(actions);

            tbody.appendChild(row);
        });
        if (rows.length === 0) {
            const row = document.createElement('tr');
            const cell = makeElement('td', 'text-body-secondary', 'No hay retos con esos filtros.');
            cell.colSpan = 11;
            row.appendChild(cell);
            tbody.appendChild(row);
        }
        table.appendChild(tbody);
        responsive.appendChild(table);
        panel.appendChild(responsive);
        panel.appendChild(renderCatalogPagination(pagination));
    };
    const loadCatalogPanel = async (url = null) => {
        if (!tablePanel?.dataset?.catalogPanel) return false;
        const endpoint = catalogEndpoint(tablePanel, url);
        if (!endpoint) return false;

        const response = await fetch(endpoint, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        const payload = await response.json().catch(() => ({}));
        if (!response.ok || payload.ok === false) {
            window.alert(payload.message || 'No se pudo cargar el catálogo.');
            return true;
        }

        if (tablePanel.dataset.catalogPanel === 'platforms') {
            renderPlatformsTable(tablePanel, payload);
        } else if (tablePanel.dataset.catalogPanel === 'languages') {
            renderLanguagesTable(tablePanel, payload);
        } else if (tablePanel.dataset.catalogPanel === 'goals') {
            renderGoalsTable(tablePanel, payload);
        } else if (tablePanel.dataset.catalogPanel === 'notifications') {
            renderNotificationsTable(tablePanel, payload);
        } else if (tablePanel.dataset.catalogPanel === 'challenges') {
            renderChallengesTable(tablePanel, payload);
        }
        if (url) {
            const historyUrl = new URL(url, window.location.href);
            window.history.pushState({}, '', historyUrl.pathname + historyUrl.search);
        }
        return true;
    };
    const submitApiForm = async (form) => {
        const submitButton = form.querySelector('[type="submit"], button:not([type])');
        if (submitButton) submitButton.disabled = true;

        try {
            const response = await fetch(form.action, {
                method: (form.method || 'POST').toUpperCase(),
                body: new FormData(form),
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' }
            });
            const payload = await response.json().catch(() => ({}));
            if (!response.ok || payload.ok === false) {
                window.alert(payload.message || 'No se pudo guardar el cambio.');
                return;
            }

            if (form.dataset.apiSuccessUrl) {
                window.location.href = form.dataset.apiSuccessUrl;
                return;
            }

            if (form.dataset.apiRefreshCatalog && await loadCatalogPanel()) {
                if (!form.closest('#tablePanel')) form.reset();
                return;
            }

            window.location.reload();
        } finally {
            if (submitButton) submitButton.disabled = false;
        }
    };
    const loadTablePanel = (url) => {
        if (tablePanel?.dataset?.catalogPanel) {
            loadCatalogPanel(url);
            return;
        }

        if (!tablePanel || !window.htmx) {
            window.location.href = url;
            return;
        }
        htmx.ajax('GET', url, {
            target: '#tablePanel',
            swap: 'innerHTML'
        });
        window.history.pushState({}, '', url);
    };

    document.body.addEventListener('click', (event) => {
        const link = event.target.closest('#tablePanel a[data-table-link], #tablePanel th a[href^="?"]');
        if (!link) return;
        event.preventDefault();
        loadTablePanel(link.href);
    });

    document.body.addEventListener('change', (event) => {
        const bulkSelect = event.target.closest('[data-bulk-select-all]');
        if (bulkSelect) {
            const tableName = bulkSelect.dataset.bulkSelectAll;
            const panel = bulkSelect.closest('#tablePanel') || document;
            panel.querySelectorAll(`[data-bulk-item="${cssEscape(tableName)}"]`).forEach((item) => {
                item.checked = bulkSelect.checked;
            });
            updateBulkActions(panel, tableName);
            return;
        }

        const bulkItemInput = event.target.closest('[data-bulk-item]');
        if (bulkItemInput) {
            const tableName = bulkItemInput.dataset.bulkItem;
            const panel = bulkItemInput.closest('#tablePanel') || document;
            const items = Array.from(panel.querySelectorAll(`[data-bulk-item="${cssEscape(tableName)}"]`));
            const checked = items.filter((item) => item.checked);
            panel.querySelectorAll(`[data-bulk-select-all="${cssEscape(tableName)}"]`).forEach((selectAll) => {
                selectAll.checked = items.length > 0 && checked.length === items.length;
                selectAll.indeterminate = checked.length > 0 && checked.length < items.length;
            });
            updateBulkActions(panel, tableName);
            return;
        }

        const select = event.target.closest('#tablePanel .table-per-page');
        if (!select) return;
        event.preventDefault();
        loadTablePanel(select.value);
    });

    const updateBulkActions = (panel, tableName) => {
        const selectedCount = panel.querySelectorAll(`[data-bulk-item="${cssEscape(tableName)}"]:checked`).length;
        panel.querySelectorAll(`[data-bulk-action="${cssEscape(tableName)}"]`).forEach((button) => {
            button.disabled = selectedCount === 0;
        });
        panel.querySelectorAll(`[data-bulk-count="${cssEscape(tableName)}"]`).forEach((counter) => {
            counter.textContent = selectedCount === 1 ? '1 seleccionado' : `${selectedCount} seleccionados`;
            counter.classList.toggle('text-bg-primary', selectedCount > 0);
            counter.classList.toggle('text-bg-secondary', selectedCount === 0);
        });
    };

    const runBulkAction = async (button) => {
        const tableName = button.dataset.bulkAction;
        const panel = button.closest('#tablePanel') || document;
        const ids = Array.from(panel.querySelectorAll(`[data-bulk-item="${cssEscape(tableName)}"]:checked`))
            .map((item) => item.value)
            .filter(Boolean);
        if (ids.length === 0) return;

        const confirmed = button.dataset.confirm
            ? await window.CodeGymConfirm(button.dataset.confirm)
            : true;
        if (!confirmed) return;

        button.disabled = true;
        try {
            for (const id of ids) {
                const body = new FormData();
                body.append('_token', csrfToken());
                body.append('id', id);
                const response = await fetch(button.dataset.bulkUrl, {
                    method: 'POST',
                    body,
                    credentials: 'same-origin',
                    headers: { 'Accept': 'application/json' }
                });
                const payload = await response.json().catch(() => ({}));
                if (!response.ok || payload.ok === false) {
                    window.alert(payload.message || 'No se pudo aplicar la acción masiva.');
                    return;
                }
            }

            if (panel.id === 'tablePanel' && await loadCatalogPanel()) return;
            window.location.reload();
        } finally {
            button.disabled = false;
        }
    };

    document.body.addEventListener('click', (event) => {
        const bulkButton = event.target.closest('[data-bulk-action]');
        if (!bulkButton) return;
        event.preventDefault();
        runBulkAction(bulkButton);
    });

    document.body.addEventListener('submit', (event) => {
        const apiForm = event.target.closest('form[data-api-form]');
        if (apiForm) {
            event.preventDefault();
            const runSubmit = () => submitApiForm(apiForm);
            if (apiForm.dataset.confirm && apiForm.dataset.confirmed !== '1') {
                window.CodeGymConfirm(apiForm.dataset.confirm).then((confirmed) => {
                    if (confirmed) runSubmit();
                });
                return;
            }

            runSubmit();
            return;
        }

        const confirmForm = event.target.closest('form[data-confirm]');
        if (confirmForm && confirmForm.dataset.confirmed !== '1') {
            event.preventDefault();
            window.CodeGymConfirm(confirmForm.dataset.confirm || '¿Deseas continuar?').then((confirmed) => {
                if (confirmed) {
                    confirmForm.dataset.confirmed = '1';
                    confirmForm.submit();
                }
            });
            return;
        }

        const form = event.target.closest('#tablePanel form[method="get"]');
        if (!form) return;
        event.preventDefault();
        const query = new URLSearchParams(new FormData(form)).toString();
        loadTablePanel(form.action + (query ? '?' + query : ''));
    });

    loadCatalogPanel();

    const dashboardTabHashes = ['#datos-generales', '#graficas', '#reportes'];
    const reportChartIds = [
        'reportComplianceChart',
        'reportTimeChart',
        'reportPlatformsChart',
        'reportLanguagesChart',
        'reportPunctualityChart',
        'reportHistoryChart'
    ];
    const dashboardChartIds = [
        'dashboardWeeklyChart',
        'dashboardComplianceChart',
        ...reportChartIds
    ];
    const getChartInstance = (idOrCanvas) => {
        if (!window.Chart || typeof Chart.getChart !== 'function') return null;
        return Chart.getChart(idOrCanvas) || null;
    };
    const destroyCharts = (ids) => {
        if (!window.Chart) return;

        ids.forEach((id) => {
            const canvas = document.getElementById(id);
            const chart = (canvas ? getChartInstance(canvas) : null) || getChartInstance(id);
            if (chart) chart.destroy();
        });
    };
    const isReportsHtmxEvent = (event) => {
        const path = event.detail?.requestConfig?.path || event.detail?.pathInfo?.requestPath || '';
        return event.detail?.target?.id === 'reportes' || String(path).includes('/dashboard/reportes');
    };
    const resizeDashboardCharts = () => {
        if (!window.Chart || typeof Chart.getChart !== 'function') return;

        dashboardChartIds.forEach((id) => {
            const canvas = document.getElementById(id);
            if (!canvas) return;
            const chart = getChartInstance(canvas) || getChartInstance(id);
            if (chart) chart.resize();
        });
    };

    if (dashboardTabHashes.includes(window.location.hash) && window.bootstrap) {
        const tabButton = document.querySelector(`#dashboardTabs [data-bs-target="${window.location.hash}"]`);
        if (tabButton) {
            bootstrap.Tab.getOrCreateInstance(tabButton).show();
        }
    }

    document.querySelectorAll('#dashboardTabs button[data-bs-toggle="tab"]').forEach((button) => {
        button.addEventListener('shown.bs.tab', (event) => {
            const target = event.target?.dataset?.bsTarget;
            if (dashboardTabHashes.includes(target)) {
                window.history.replaceState(null, '', target);
            }
            window.setTimeout(resizeDashboardCharts, 50);
        });
    });

    const dashboardDataNode = document.getElementById('dashboardData');
    let dashboardWeekly = [];
    const createChart = (canvas, config) => {
        if (!canvas || !window.Chart) return null;
        const currentChart = getChartInstance(canvas) || getChartInstance(canvas.id);
        if (currentChart) currentChart.destroy();
        return new Chart(canvas, config);
    };
    const chartRows = (rows, fallbackLabel = 'Sin datos') => {
        if (!Array.isArray(rows) || rows.length === 0) {
            return { labels: [fallbackLabel], values: [0] };
        }
        return {
            labels: rows.map((row) => row.label),
            values: rows.map((row) => Number(row.value || 0))
        };
    };
    const appendText = (parent, tag, className, text) => {
        const node = document.createElement(tag);
        if (className) node.className = className;
        node.textContent = text;
        parent.appendChild(node);
        return node;
    };
    const safePath = (value, fallback = '#') => {
        const path = String(value || '');
        return path.startsWith('/') && !path.startsWith('//') ? path : fallback;
    };
    const clearNode = (node) => {
        while (node.firstChild) node.removeChild(node.firstChild);
    };
    const emptyListItem = (message) => {
        const item = document.createElement('div');
        item.className = 'list-group-item text-body-secondary';
        item.textContent = message;
        return item;
    };
    const linkListItem = (href = '#') => {
        const item = document.createElement('a');
        item.className = 'list-group-item list-group-item-action';
        item.href = safePath(href);
        return item;
    };
    const updateDashboardMetrics = (dashboard) => {
        const stats = dashboard?.stats || {};
        const streaks = dashboard?.streaks || {};
        const values = {
            completed_month: String(stats.completed_month || 0),
            general_percent: `${Number(stats.general_percent || 0)}%`,
            on_time_percent: `${Number(stats.on_time_percent || 0)}%`,
            time_month: `${Number(stats.time_month || 0)} min`,
            current_streak: `${Number(streaks.current || 0)} días`,
            best_streak: `${Number(streaks.best || 0)} días`,
            month_streak: `${Number(streaks.month || 0)} días`,
            expired_review: String(stats.expired_review || 0)
        };

        Object.entries(values).forEach(([key, value]) => {
            const node = document.querySelector(`[data-dashboard-metric="${key}"]`);
            if (node) node.textContent = value;
        });
    };
    const renderAttentionList = (dashboard) => {
        const list = document.querySelector('[data-dashboard-list="attention"]');
        if (!list) return;

        clearNode(list);
        const attention = dashboard?.attention || {};
        const expired = linkListItem('/retos?status=expired');
        appendText(expired, 'span', '', 'Retos vencidos por revisar');
        appendText(expired, 'span', 'badge text-bg-secondary', String(attention.expired || 0));
        expired.classList.add('d-flex', 'justify-content-between', 'align-items-center');
        list.appendChild(expired);

        const pending = linkListItem('/calendario');
        appendText(pending, 'span', '', 'Pendientes próximos 7 días');
        appendText(pending, 'span', 'badge text-bg-primary', String(attention.pending_week || 0));
        pending.classList.add('d-flex', 'justify-content-between', 'align-items-center');
        list.appendChild(pending);

        const days = document.createElement('div');
        days.className = 'list-group-item d-flex justify-content-between align-items-center';
        appendText(days, 'span', '', 'Días sin práctica registrada');
        appendText(
            days,
            'span',
            `badge text-bg-${Number(attention.days_without_practice || 0) > 2 ? 'warning' : 'success'}`,
            String(attention.days_without_practice || 0)
        );
        list.appendChild(days);

        (dashboard?.goalAlerts || []).forEach((goal) => {
            const item = linkListItem('/metas');
            appendText(item, 'strong', '', dashboard?.goalTypes?.[goal.goal_type] || String(goal.goal_type || 'Meta'));
            const detail = [
                `${goal.current_value || 0}/${goal.target_value || 0}`,
                `${goal.progress_percent || 0}%`,
                [goal.platform_name, goal.language_name].filter(Boolean).join(' ')
            ].filter(Boolean).join(' · ');
            appendText(item, 'span', 'text-body-secondary', detail);
            list.appendChild(item);
        });
    };
    const renderTodayChallenges = (rows) => {
        const list = document.querySelector('[data-dashboard-list="todayChallenges"]');
        if (!list) return;
        clearNode(list);
        if (!Array.isArray(rows) || rows.length === 0) {
            list.appendChild(emptyListItem('No hay retos pendientes para hoy.'));
            return;
        }
        rows.forEach((challenge) => {
            const item = linkListItem('/retos');
            appendText(item, 'strong', '', String(challenge.platform_name || 'Plataforma'));
            appendText(item, 'span', 'text-body-secondary', String(challenge.title || 'Pendiente por detallar'));
            list.appendChild(item);
        });
    };
    const renderActiveGoals = (dashboard) => {
        const list = document.querySelector('[data-dashboard-list="activeGoals"]');
        if (!list) return;
        clearNode(list);
        const rows = dashboard?.activeGoals || [];
        if (!Array.isArray(rows) || rows.length === 0) {
            list.appendChild(emptyListItem('No hay metas activas.'));
            return;
        }
        rows.forEach((goal) => {
            const item = linkListItem('/metas');
            const row = document.createElement('div');
            row.className = 'd-flex justify-content-between gap-3';
            appendText(row, 'strong', '', dashboard?.goalTypes?.[goal.goal_type] || String(goal.goal_type || 'Meta'));
            appendText(row, 'span', 'text-body-secondary', `${goal.progress_percent || 0}%`);
            item.appendChild(row);

            const progress = document.createElement('div');
            progress.className = 'progress mt-2';
            progress.setAttribute('role', 'progressbar');
            progress.setAttribute('aria-valuenow', String(goal.progress_percent || 0));
            progress.setAttribute('aria-valuemin', '0');
            progress.setAttribute('aria-valuemax', '100');
            const bar = document.createElement('div');
            bar.className = 'progress-bar';
            bar.style.width = `${Math.min(100, Number(goal.progress_percent || 0))}%`;
            progress.appendChild(bar);
            item.appendChild(progress);
            appendText(item, 'span', 'small text-body-secondary', `${goal.current_value || 0}/${goal.target_value || 0} · vence ${goal.period_end || ''}`);
            list.appendChild(item);
        });
    };
    const renderExpiredChallenges = (rows) => {
        const list = document.querySelector('[data-dashboard-list="expiredChallenges"]');
        if (!list) return;
        clearNode(list);
        if (!Array.isArray(rows) || rows.length === 0) {
            list.appendChild(emptyListItem('No hay retos vencidos pendientes.'));
            return;
        }
        rows.forEach((challenge) => {
            const item = linkListItem('/retos');
            appendText(item, 'strong', '', String(challenge.platform_name || 'Plataforma'));
            appendText(item, 'span', 'badge text-bg-secondary', String(challenge.scheduled_date || ''));
            list.appendChild(item);
        });
    };
    const renderNotifications = (rows) => {
        const list = document.querySelector('[data-dashboard-list="notifications"]');
        if (!list) return;
        clearNode(list);
        if (!Array.isArray(rows) || rows.length === 0) {
            list.appendChild(emptyListItem('No hay notificaciones pendientes.'));
            return;
        }
        rows.forEach((notification) => {
            const item = linkListItem(notification.action_url || '/notificaciones');
            appendText(item, 'strong', '', String(notification.title || 'Notificación'));
            appendText(item, 'span', 'd-block text-body-secondary', String(notification.message || ''));
            list.appendChild(item);
        });
    };
    const renderTopList = (key, rows, emptyMessage) => {
        const list = document.querySelector(`[data-dashboard-list="${key}"]`);
        if (!list) return;
        clearNode(list);
        if (!Array.isArray(rows) || rows.length === 0) {
            list.appendChild(emptyListItem(emptyMessage));
            return;
        }
        rows.forEach((row) => {
            const item = document.createElement('div');
            item.className = 'list-group-item d-flex justify-content-between align-items-center';
            appendText(item, 'span', '', String(row.label || 'Sin nombre'));
            appendText(item, 'span', 'text-body-secondary', `${row.value || 0} retos · ${row.minutes || 0} min`);
            list.appendChild(item);
        });
    };
    const renderDashboardLists = (dashboard) => {
        renderAttentionList(dashboard);
        renderTodayChallenges(dashboard?.todayChallenges);
        renderActiveGoals(dashboard);
        renderExpiredChallenges(dashboard?.expiredChallenges);
        renderNotifications(dashboard?.notifications);
        renderTopList('topPlatforms', dashboard?.topPlatforms, 'Sin plataformas completadas este mes.');
        renderTopList('topLanguages', dashboard?.topLanguages, 'Sin lenguajes completados este mes.');
    };
    const setReportsData = (reports) => {
        const reportsNode = document.getElementById('reportsData');
        if (!reportsNode) return;

        reportsNode.dataset.reports = JSON.stringify(reports || {});
    };
    const updateReportsSummary = (reports) => {
        const compliance = reports?.compliance || {};
        const values = {
            scheduled: String(compliance.scheduled || 0),
            completed: String(compliance.completed || 0),
            general_percent: `${Number(compliance.general_percent || 0)}%`,
            on_time_percent: `${Number(compliance.on_time_percent || 0)}%`
        };

        Object.entries(values).forEach(([key, value]) => {
            const node = document.querySelector(`[data-report-metric="${key}"]`);
            if (node) node.textContent = value;
        });

        const totalTime = Array.isArray(reports?.timeByMonth)
            ? reports.timeByMonth.reduce((total, row) => total + Number(row.value || 0), 0)
            : 0;
        const totalNode = document.querySelector('[data-report-time-total]');
        if (totalNode) totalNode.textContent = `${totalTime} min`;
    };
    const applyReportApiPayload = (payload) => {
        const reports = payload?.report?.reports || null;
        if (!reports) return;

        setReportsData(reports);
        updateReportsSummary(reports);
        refreshReportCharts();
        window.history.replaceState(null, '', '#reportes');
    };
    const loadReportsFromApi = async (form) => {
        const query = new URLSearchParams(new FormData(form)).toString();
        const endpoint = `${form.dataset.reportApiUrl || '/api/reports'}${query ? '?' + query : ''}`;
        const response = await fetch(endpoint, {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        const payload = await response.json().catch(() => ({}));
        if (!response.ok || payload.ok === false) {
            window.alert(payload.message || 'No se pudieron cargar los reportes.');
            return;
        }

        applyReportApiPayload(payload);
    };
    const initializeReportCharts = () => {
        const reportsNode = document.getElementById('reportsData');
        let reports = null;
        if (reportsNode) {
            try {
                reports = JSON.parse(reportsNode.dataset.reports || '{}');
            } catch (error) {
                reports = null;
            }
        }
        if (!reports) return;

        const compliance = document.getElementById('reportComplianceChart');
        if (compliance) {
            createChart(compliance, {
                type: 'bar',
                data: {
                    labels: ['General', 'Puntual'],
                    datasets: [{
                        label: 'Porcentaje',
                        data: [
                            Number(reports.compliance?.general_percent || 0),
                            Number(reports.compliance?.on_time_percent || 0)
                        ],
                        backgroundColor: ['#0d6efd', '#198754']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { min: 0, max: 100 } }
                }
            });
        }

        const time = document.getElementById('reportTimeChart');
        if (time) {
            const data = chartRows(reports.timeByMonth, 'Sin meses');
            createChart(time, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: 'Minutos',
                        data: data.values,
                        backgroundColor: '#0d6efd'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { beginAtZero: true } }
                }
            });
        }

        const platforms = document.getElementById('reportPlatformsChart');
        if (platforms) {
            const data = chartRows(reports.platforms, 'Sin plataformas');
            createChart(platforms, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{ label: 'Retos', data: data.values, backgroundColor: '#0d6efd' }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }

        const languages = document.getElementById('reportLanguagesChart');
        if (languages) {
            const data = chartRows(reports.languages, 'Sin lenguajes');
            createChart(languages, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [{ label: 'Retos', data: data.values, backgroundColor: '#20c997' }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }

        const punctuality = document.getElementById('reportPunctualityChart');
        if (punctuality) {
            createChart(punctuality, {
                type: 'doughnut',
                data: {
                    labels: ['A tiempo', 'Fuera de fecha'],
                    datasets: [{
                        data: [
                            Number(reports.punctuality?.on_time || 0),
                            Number(reports.punctuality?.late || 0)
                        ],
                        backgroundColor: ['#198754', '#ffc107']
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }

        const history = document.getElementById('reportHistoryChart');
        if (history) {
            createChart(history, {
                type: 'doughnut',
                data: {
                    labels: ['Cumplidos', 'No cumplidos', 'Vencidos', 'Cancelados', 'Pendientes'],
                    datasets: [{
                        data: [
                            Number(reports.history?.completed || 0),
                            Number(reports.history?.missed || 0),
                            Number(reports.history?.expired || 0),
                            Number(reports.history?.cancelled || 0),
                            Number(reports.history?.pending || 0)
                        ],
                        backgroundColor: ['#198754', '#dc3545', '#6c757d', '#842029', '#0d6efd']
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }
    };
    const refreshReportCharts = () => {
        if (!document.getElementById('reportes') || !document.getElementById('reportsData')) return;
        destroyCharts(reportChartIds);
        initializeReportCharts();
        window.setTimeout(resizeDashboardCharts, 80);
    };
    const initializeDashboardCharts = () => {
        const dashboardWeeklyChart = document.getElementById('dashboardWeeklyChart');
        if (dashboardWeeklyChart) {
            createChart(dashboardWeeklyChart, {
                type: 'bar',
                data: {
                    labels: dashboardWeekly.map((row) => row.label),
                    datasets: [
                        {
                            label: 'Programados',
                            data: dashboardWeekly.map((row) => Number(row.scheduled || 0)),
                            backgroundColor: '#0d6efd'
                        },
                        {
                            label: 'Cumplidos',
                            data: dashboardWeekly.map((row) => Number(row.completed || 0)),
                            backgroundColor: '#198754'
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { beginAtZero: true } }
                }
            });
        }

        const dashboardChart = document.getElementById('dashboardComplianceChart');
        if (dashboardChart) {
            createChart(dashboardChart, {
                type: 'doughnut',
                data: {
                    labels: ['Cumplidos', 'No cumplidos', 'Vencidos', 'Cancelados'],
                    datasets: [{
                        data: [
                            Number(dashboardChart.dataset.completed || 0),
                            Number(dashboardChart.dataset.missed || 0),
                            Number(dashboardChart.dataset.expired || 0),
                            Number(dashboardChart.dataset.cancelled || 0)
                        ],
                        backgroundColor: ['#198754', '#dc3545', '#6c757d', '#842029']
                    }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }
    };
    const applyDashboardApiPayload = (payload) => {
        const dashboard = payload?.dashboard || null;
        if (!dashboard) return;

        updateDashboardMetrics(dashboard);
        renderDashboardLists(dashboard);

        dashboardWeekly = Array.isArray(dashboard.weeklyCompliance) ? dashboard.weeklyCompliance : [];
        const distribution = dashboard.distribution || {};
        const dashboardChart = document.getElementById('dashboardComplianceChart');
        if (dashboardChart) {
            dashboardChart.dataset.completed = String(distribution.completed || 0);
            dashboardChart.dataset.missed = String(distribution.missed || 0);
            dashboardChart.dataset.expired = String(distribution.expired || 0);
            dashboardChart.dataset.cancelled = String(distribution.cancelled || 0);
        }
        initializeDashboardCharts();
        window.setTimeout(resizeDashboardCharts, 80);
    };
    const loadDashboardFromApi = async () => {
        if (!dashboardDataNode) return;

        const response = await fetch('/api/dashboard/summary', {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });
        const payload = await response.json().catch(() => ({}));
        if (!response.ok || payload.ok === false) return;

        applyDashboardApiPayload(payload);
    };

    document.body.addEventListener('htmx:beforeSwap', (event) => {
        if (!isReportsHtmxEvent(event)) return;
        destroyCharts(reportChartIds);
    });

    document.body.addEventListener('htmx:afterSwap', (event) => {
        if (!isReportsHtmxEvent(event)) return;
        window.history.replaceState(null, '', '#reportes');
        window.setTimeout(refreshReportCharts, 50);
    });

    document.body.addEventListener('htmx:afterSettle', (event) => {
        if (!isReportsHtmxEvent(event)) return;
        window.setTimeout(refreshReportCharts, 50);
    });

    document.body.addEventListener('submit', (event) => {
        const form = event.target.closest('form[data-report-api-form]');
        if (!form) return;

        event.preventDefault();
        loadReportsFromApi(form);
    });

    document.body.addEventListener('click', (event) => {
        const clear = event.target.closest('[data-report-api-clear]');
        if (!clear) return;

        const form = clear.closest('#reportes')?.querySelector('form[data-report-api-form]');
        if (!form) return;

        event.preventDefault();
        form.reset();
        form.querySelectorAll('input, select, textarea').forEach((field) => {
            if (field.type === 'checkbox' || field.type === 'radio') {
                field.checked = false;
                return;
            }
            field.value = field.tagName === 'SELECT' ? field.querySelector('option')?.value || '' : '';
        });
        loadReportsFromApi(form);
    });

    if (dashboardDataNode) {
        try {
            dashboardWeekly = JSON.parse(dashboardDataNode.dataset.weekly || '[]');
        } catch (error) {
            dashboardWeekly = [];
        }
    }
    initializeDashboardCharts();
    loadDashboardFromApi();

    initializeReportCharts();
});
