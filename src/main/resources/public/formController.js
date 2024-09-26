//=======================================================================================
//
//  PRIVATE COMPANY VALUATOR
//  Form controller script
//
// (C) 2024 Axiom Capital, Bolat Basheyev
//=======================================================================================


const DEFAULT_COMPANY_NAME = "A Company Making Everything (ACME)"
const DEFAULT_COUNTRY_CODE = "KZ";
const DEFAULT_YEARS_FORECAST = 3;


//---------------------------------------------------------------------------------------
// Initializes form form and adds listeners to user actions
//---------------------------------------------------------------------------------------
function initialize() {
    initializeFields();
    document.getElementById("dataFirstYear").addEventListener("change", onPeriodChange);
    document.getElementById("forecastHorizon").addEventListener("change", onPeriodChange);
    document.getElementById("form").addEventListener("submit", onSubmit);
    document.getElementById("form").addEventListener("change", onFormChange);
    window.addEventListener('beforeunload', onFormChange);
}


//---------------------------------------------------------------------------------------
// Loads from data from local storage or initializes fields of form
//---------------------------------------------------------------------------------------
function initializeFields() {
    if (localStorage.getItem("SavedForm")) {
        let jsonString = localStorage.getItem("SavedForm");
        let jsonObject = JSON.parse(jsonString);
        jsonToForm(jsonObject);
        let report = localStorage.getItem("valuationReport");
        if (report) {
            const reportField = document.getElementById('valuationReport');
            reportField.textContent = report;
        }
    } else {
        form.name.value = DEFAULT_COMPANY_NAME;
        form.countryCode.value = DEFAULT_COUNTRY_CODE;
        form.dataFirstYear.value = new Date().getFullYear();
        form.forecastHorizon = DEFAULT_YEARS_FORECAST;
    }
    // Adjust rows in financials table
    adjustRows();
}


//---------------------------------------------------------------------------------------
// Builds JSON from form values
//---------------------------------------------------------------------------------------
function formToJSON() {

    // retrieve financials data from table to arrays
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

    // Adjust rates from number to percents
    equityRate = parseFloat(form.equityCost.value) / 100.0;
    debtRate = parseFloat(form.debtCost.value) / 100.0;

    // build JSON object
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


//---------------------------------------------------------------------------------------
// Parse JSON object and fill form values
//---------------------------------------------------------------------------------------
function jsonToForm(savedForm) {
    console.log("Loading form data from local storage:\n" + JSON.stringify(savedForm, null, 4));
    if ("name" in savedForm) form.name.value = savedForm.name;
    if ("country" in savedForm) form.countryCode.value = savedForm.country;
    if ("dataFirstYear" in savedForm) form.dataFirstYear.value = savedForm.dataFirstYear;
    if ("revenue" in savedForm) {
        let arrayLength = savedForm.revenue.length;
        form.forecastHorizon.value = arrayLength;
        adjustRows();
        for (i=1; i <= arrayLength; i++) {
            document.getElementById("r" + i + "c2").value = savedForm.revenue[i-1];
            document.getElementById("r" + i + "c3").value = savedForm.ebitda[i-1];
            document.getElementById("r" + i + "c4").value = savedForm.freeCashFlow[i-1];
        }
    }
    if ("cash" in savedForm) form.cash.value = Number(savedForm.cash);
    if ("equity" in savedForm) form.equity.value = savedForm.equity;
    if ("equityRate" in savedForm) form.equityCost.value = savedForm.equityRate * 100.0;
    if ("debt" in savedForm) form.debt.value = savedForm.debt;
    if ("debtRate" in savedForm) form.debtCost.value = savedForm.debtRate * 100.0;
    if ("isLeader" in savedForm) form.isLeader.checked = savedForm.isLeader;
    if ("comparableStock" in savedForm) form.comparableStock.value = savedForm.comparableStock;
}

//---------------------------------------------------------------------------------------
// Adjust table rows to specified number (forecast period) and update years labels
//---------------------------------------------------------------------------------------
function adjustRows() {
    console.log("Rows adjusting started");
    // calculate required rows and difference
    const table = document.getElementById("financials");
    let requiredRows = Number(form.forecastHorizon.value);
    let currentRows = Number(table.rows.length) - 1;
    let difference = requiredRows - currentRows;
    console.log("Rows required: " + requiredRows);
    console.log("Current rows: " + currentRows);
    console.log("Rows change: " + difference);

    // adjust: add or remove rows
    if (difference > 0)
        addRows(table, difference);
    else if (difference < 0)
        removeRows(table, 1-difference);

    // enumerate years labels in table after adjustments
    currentRows = Number(table.rows.length) - 1;
    let baseYear = Number(form.dataFirstYear.value);
    for (i = 1; i<= currentRows; i++) {
        let rowYear = baseYear + Number(i - 1);
        table.rows[i].cells[0].innerHTML = rowYear;
    }
}


//---------------------------------------------------------------------------------------
// Add specified amount of rows to form financials table
//---------------------------------------------------------------------------------------
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


//---------------------------------------------------------------------------------------
// Remove specified amount of rows to form financials table
//---------------------------------------------------------------------------------------
function removeRows(table, amount) {
    console.log("Remove rows: " + amount);
    for (i=1; i<amount; i++) {
        const rowCount = table.rows.length;
        if (rowCount > 0) table.deleteRow(rowCount - 1);
    }
}


//------------------------------------------------------------------------------------
// Event listener when base year or forecast period changed to adjust table rows
//------------------------------------------------------------------------------------
function onPeriodChange(event) {
    let baseYear = Number(form.dataFirstYear.value);
    let totalYears = Number(form.forecastHorizon.value);
    let lastYear = baseYear + totalYears - 1;
    console.log("Time period changed: " + baseYear + " till " + lastYear);
    adjustRows();
}


//------------------------------------------------------------------------------------
// Event listener on form change to save values in local storage
//------------------------------------------------------------------------------------
function onFormChange(event) {
    let jsonForm = formToJSON();
    let jsonString = JSON.stringify(jsonForm);
    localStorage.setItem("SavedForm", jsonString);
    let report = valuationReport.value;
    localStorage.setItem("valuationReport", report);
    console.log("Form saved to local storage:\n" + jsonString);
    console.log("Valuation report:\n" + report);
}


//------------------------------------------------------------------------------------
// Event listener on when submit button pressed - sends request to back-end service
//------------------------------------------------------------------------------------
function onSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const reportField = document.getElementById('valuationReport');
    // Retrieve data from form
    const companyData = formToJSON();
    // Show request body in report field
    reportField.textContent = JSON.stringify(companyData, null, 2);
    // Send request 
    fetch("/valuate",
        {
            method: "POST",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(companyData)
        }
    ).then(
        response => response.text()
    ).then((response) =>  {
        console.log(response);
        reportField.textContent = response;
    }).catch (error => {
        console.error("Fetch failed: ", error)
    })

}


//------------------------------------------------------------------------------------
// Entry point
//------------------------------------------------------------------------------------
initialize()

