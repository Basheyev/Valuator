


function onSubmit(event) {
        event.preventDefault();

        const form = event.target;

        // Получаем данные из формы
        const data = {
            name: form.name.value,
            country: form.country.value,
            dataFirstYear: Number(form.dataFirstYear.value),
            revenue: form.revenue.value.split(',').map(Number),
            ebitda: form.ebitda.value.split(',').map(Number),
            freeCashFlow: form.freeCashFlow.value.split(',').map(Number),
            debt: Number(form.debt.value),
            debtRate: parseFloat(form.debtRate.value),
            equity: Number(form.equity.value),
            equityRate: parseFloat(form.equityRate.value),
            cash: Number(form.cash.value),
            isLeader: form.isLeader.value,
            comparableStock: form.comparableStock.value
        };

        // Показываем JSON на странице
        document.getElementById('result').textContent = JSON.stringify(data, null, 2);
}


document.getElementById('jsonForm').addEventListener('submit', onSubmit);