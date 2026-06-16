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

    initializeReportCharts();
});
