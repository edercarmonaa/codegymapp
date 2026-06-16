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

            window.location.reload();
        } finally {
            if (submitButton) submitButton.disabled = false;
        }
    };
    const loadTablePanel = (url) => {
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
        const select = event.target.closest('#tablePanel .table-per-page');
        if (!select) return;
        event.preventDefault();
        loadTablePanel(select.value);
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
