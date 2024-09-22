
// todo: save form entries to LocalStorage

function initialize() {
    initializeFields();
    document.getElementById("dataFirstYear").addEventListener("change", onPeriodChange);
    document.getElementById("forecastHorizon").addEventListener("change", onPeriodChange);
    document.getElementById("form").addEventListener("submit", onSubmit);
}


function initializeFields() {
    const currentYear = new Date().getFullYear();
    const DEFAULT_YEARS = 3;
    form.name.value = "ACME";
    form.countryCode.value = "KZ";
    form.dataFirstYear.value = currentYear;
    form.forecastHorizon = DEFAULT_YEARS;
    adjustRows();
}


function adjustRows() {
    console.log("Rows adjusting started");

    const table = document.getElementById("financials");
    let requiredRows = Number(form.forecastHorizon.value);
    let currentRows = Number(table.rows.length) - 1;
    let difference = requiredRows - currentRows;
    console.log("Rows requered: " + requiredRows);
    console.log("Current rows: " + currentRows);
    console.log("Rows change: " + difference);
    if (difference > 0) addRows(table, difference);
    else if (difference < 0) removeRows(table, 1-difference);

    // enumarate years
    currentRows = Number(table.rows.length) - 1;
    let baseYear = Number(form.dataFirstYear.value);
    for (i = 1; i<= currentRows; i++) {
        let rowYear = baseYear + Number(i - 1);
        table.rows[i].cells[0].innerHTML = rowYear;
    }
}


function addRows(table, amount) {
    const lastIndex = table.rows.length - 1;
    for (i = 1; i <= amount; i++) {
        let newRow = table.insertRow();
        let cell1 = newRow.insertCell(0);
        let cell2 = newRow.insertCell(1);
        let cell3 = newRow.insertCell(2);
        let cell4 = newRow.insertCell(3);
        let rowID = "r" + (lastIndex + i);
        cell1.innerHTML = "";
        cell2.innerHTML = "<input type=\"number\" class=\"form-control text-end\" id=\"" + rowID
                          + "c2\" value=\"0\">";
        cell3.innerHTML = "<input type=\"number\" class=\"form-control text-end\" id=\"" + rowID
                          + "c3\" value=\"0\">";
        cell4.innerHTML = "<input type=\"number\" class=\"form-control text-end\" id=\"" + rowID
                          + "c4\" value=\"0\">";
    }
}


function removeRows(table, amount) {
    console.log("Remove rows: " + amount);
    for (i=1; i<amount; i++) {
        const rowCount = table.rows.length;
        if (rowCount > 0) table.deleteRow(rowCount - 1);
    }
}

//------------------------------------------------------------------------------------
function JSONBuilder() {

    // collect financials data
    const table = document.getElementById("financials");
    currentRows = Number(table.rows.length) - 1;
    const revenue = [];
    const ebitda = [];
    const cashflow = [];
    for (i=1; i<=currentRows; i++) {
        let revenueValue = Number(document.getElementById("r" + i + "c2").value);
        let ebitdaValue = Number(document.getElementById("r" + i + "c3").value);
        let cashflowValue = Number(document.getElementById("r" + i + "c4").value);
        revenue.push(revenueValue);
        ebitda.push(ebitdaValue);
        cashflow.push(cashflowValue);
    }

    equityRate = parseFloat(form.equityCost.value) / 100.0;
    debtRate = parseFloat(form.debtCost.value) / 100.0;


    // Получаем данные из формы
    const data = {
        name: form.name.value,
        country: form.countryCode.value,
        dataFirstYear: Number(form.dataFirstYear.value),
        revenue: revenue,
        ebitda: ebitda,
        freeCashFlow: cashflow,
        cash: Number(form.cash.value),
        equity: Number(form.equity.value),
        equityRate: equityRate,
        debt: Number(form.debt.value),
        debtRate: debtRate,
        isLeader: form.isLeader.checked,
        comparableStock: form.comparableStock.value
    };

    return data;
}



//------------------------------------------------------------------------------------

function onPeriodChange(event) {
    let baseYear = Number(form.dataFirstYear.value);
    let totalYears = Number(form.forecastHorizon.value);
    let lastYear = baseYear + totalYears - 1;
    console.log("Time period changed: " + baseYear + " till " + lastYear);
    adjustRows();
}


function onSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const reportField = document.getElementById('valuationReport');
    // Получаем данные из формы
    const data = JSONBuilder();
    // Показываем JSON на странице
    reportField.textContent = JSON.stringify(data, null, 2);
}


initialize()

