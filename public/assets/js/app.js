document.addEventListener('DOMContentLoaded', () => {
    const dashboardDataNode = document.getElementById('dashboardData');
    let dashboardWeekly = [];
    if (dashboardDataNode) {
        try {
            dashboardWeekly = JSON.parse(dashboardDataNode.dataset.weekly || '[]');
        } catch (error) {
            dashboardWeekly = [];
        }
    }
    const dashboardWeeklyChart = document.getElementById('dashboardWeeklyChart');
    if (dashboardWeeklyChart && window.Chart) {
        new Chart(dashboardWeeklyChart, {
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
    if (dashboardChart && window.Chart) {
        new Chart(dashboardChart, {
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

    const reportsNode = document.getElementById('reportsData');
    const reports = reportsNode ? JSON.parse(reportsNode.dataset.reports || '{}') : null;
    const chartRows = (rows, fallbackLabel = 'Sin datos') => {
        if (!Array.isArray(rows) || rows.length === 0) {
            return { labels: [fallbackLabel], values: [0] };
        }
        return {
            labels: rows.map((row) => row.label),
            values: rows.map((row) => Number(row.value || 0))
        };
    };

    const compliance = document.getElementById('reportComplianceChart');
    if (compliance && window.Chart && reports) {
        new Chart(compliance, {
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
    if (time && window.Chart && reports) {
        const data = chartRows(reports.timeByMonth, 'Sin meses');
        new Chart(time, {
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
    if (platforms && window.Chart && reports) {
        const data = chartRows(reports.platforms, 'Sin plataformas');
        new Chart(platforms, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{ label: 'Retos', data: data.values, backgroundColor: '#0d6efd' }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }

    const languages = document.getElementById('reportLanguagesChart');
    if (languages && window.Chart && reports) {
        const data = chartRows(reports.languages, 'Sin lenguajes');
        new Chart(languages, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{ label: 'Retos', data: data.values, backgroundColor: '#20c997' }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }

    const punctuality = document.getElementById('reportPunctualityChart');
    if (punctuality && window.Chart && reports) {
        new Chart(punctuality, {
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
    if (history && window.Chart && reports) {
        new Chart(history, {
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
});
