document.addEventListener('DOMContentLoaded', () => {
    const dashboardChart = document.getElementById('dashboardComplianceChart');
    if (dashboardChart && window.Chart) {
        new Chart(dashboardChart, {
            type: 'doughnut',
            data: {
                labels: ['Cumplidos', 'Vencidos'],
                datasets: [{
                    data: [
                        Number(dashboardChart.dataset.completed || 0),
                        Number(dashboardChart.dataset.expired || 0)
                    ],
                    backgroundColor: ['#198754', '#6c757d']
                }]
            },
            options: { responsive: true, maintainAspectRatio: false }
        });
    }

    const compliance = document.getElementById('reportComplianceChart');
    if (compliance && window.Chart) {
        new Chart(compliance, {
            type: 'bar',
            data: {
                labels: ['General', 'Puntual'],
                datasets: [{ label: 'Porcentaje', data: [0, 0], backgroundColor: ['#0d6efd', '#198754'] }]
            }
        });
    }

    const time = document.getElementById('reportTimeChart');
    if (time && window.Chart) {
        new Chart(time, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{ label: 'Minutos', data: [], borderColor: '#0d6efd' }]
            }
        });
    }
});

