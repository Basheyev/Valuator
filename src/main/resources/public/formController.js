//=======================================================================================
//
//  PRIVATE COMPANY VALUATOR
//  Form controller script
//
// (C) 2024 Axiom Capital, Bolat Basheyev
//=======================================================================================

const VALUATION_SERVICE_URL = "/valuate";
const DEFAULT_COMPANY_NAME = "A Company Making Everything (ACME)"
const DEFAULT_COUNTRY_CODE = "KZ";
const DEFAULT_YEARS_FORECAST = 3;
const DEFAULT_VENTURE_RATE = 40;
const THOUSANDS_SEPARATOR = " ";


//---------------------------------------------------------------------------------------
// Initializes form form and adds listeners to user actions
//---------------------------------------------------------------------------------------
function initialize() {

    initializeFields();

    document.getElementById("dataFirstYear").addEventListener("change", onPeriodChange);
    document.getElementById("forecastHorizon").addEventListener("change", onPeriodChange);
    document.getElementById("form").addEventListener("submit", onSubmit);
    document.getElementById("form").addEventListener("change", onFormChange);

    document.getElementById('equity').addEventListener('input', onCurrencyInput);
    document.getElementById('debt').addEventListener('input', onCurrencyInput);
    document.getElementById('cash').addEventListener('input', onCurrencyInput);

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
            reportField.innerHTML = report;
        }
    } else {
        let currentYear = new Date().getFullYear();
        form.name.value = DEFAULT_COMPANY_NAME;
        form.countryCode.value = DEFAULT_COUNTRY_CODE;
        form.dataFirstYear.value = currentYear;
        form.forecastHorizon = DEFAULT_YEARS_FORECAST;
        form.ventureExitYear.value = currentYear;
        form.ventureRate.value = DEFAULT_VENTURE_RATE;
        form.marketShare.value = 1;
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
        let revenueValue = extractNumber(document.getElementById("r" + i + "c2").value);
        let ebitdaValue = extractNumber(document.getElementById("r" + i + "c3").value);
        let cashflowValue = extractNumber(document.getElementById("r" + i + "c4").value);
        revenue.push(revenueValue);
        ebitda.push(ebitdaValue);
        cashflow.push(cashflowValue);
    }

    // Adjust rates from number to percents
    let equityRate = parseFloat(form.equityRate.value) / 100.0;
    let debtRate = parseFloat(form.debtRate.value) / 100.0;
    let ventureRate = parseFloat(form.ventureRate.value) / 100.0;
    let marketShare = parseFloat(form.marketShare.value) / 100.0;

    // build JSON object
    const data = {
        name: form.name.value,                                 // get company name
        country: form.countryCode.value,                       // get country code
        dataFirstYear: Number(form.dataFirstYear.value),       // get data first year (base year of financial)
        revenue: revenue,                                      // get revenue numbers array by periods
        ebitda: ebitda,                                        // get ebitda numbers array by periods
        freeCashFlow: cashflow,                                // get free cash flow numbers array by periods
        cash: extractNumber(form.cash.value),                  // get cash & equivalents amount
        equity: extractNumber(form.equity.value),              // get equity invested
        equityRate: equityRate,                                // get equity interest rate
        debt: extractNumber(form.debt.value),                  // get debt borrowed
        debtRate: debtRate,                                    // get debt interest rate
        marketShare: Number(marketShare),                      // get market share
        comparableStock: form.comparableStock.value,           // get comparable stock value
        ventureRate: ventureRate,                              // get venture interest rate
        ventureExitYear: Number(form.ventureExitYear.value)    // get venture exit year
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
            document.getElementById("r" + i + "c2").value = addThousandsSeparators(String(savedForm.revenue[i-1]));
            document.getElementById("r" + i + "c3").value = addThousandsSeparators(String(savedForm.ebitda[i-1]));
            document.getElementById("r" + i + "c4").value = addThousandsSeparators(String(savedForm.freeCashFlow[i-1]));
        }
    }
    if ("cash" in savedForm) form.cash.value = addThousandsSeparators(savedForm.cash);
    if ("equity" in savedForm) form.equity.value = addThousandsSeparators(savedForm.equity);
    if ("equityRate" in savedForm) form.equityRate.value = Math.round(savedForm.equityRate * 100.0);
    if ("debt" in savedForm) form.debt.value = addThousandsSeparators(savedForm.debt);
    if ("debtRate" in savedForm) form.debtRate.value = Math.round(savedForm.debtRate * 100.0);
    if ("marketShare" in savedForm) form.marketShare.value = Math.round(savedForm.marketShare * 100.0);
    if ("comparableStock" in savedForm) form.comparableStock.value = savedForm.comparableStock;
    if ("ventureExitYear" in savedForm) form.ventureExitYear.value = savedForm.ventureExitYear;
    if ("ventureRate" in savedForm) form.ventureRate.value = Math.round(savedForm.ventureRate * 100.0);

    // todo call thousands separator
}


//---------------------------------------------------------------------------------------
// Format and logic verification
//---------------------------------------------------------------------------------------
function formDataVerification() {
    const reportField = document.getElementById('valuationReport');
    const dataFirstYear = Number(form.dataFirstYear.value);
    const forecastHorizon = Number(form.forecastHorizon.value);
    const dataLastYear = (dataFirstYear + forecastHorizon) - 1;
    const exitYear = Number(form.ventureExitYear.value);

    console.log("Base year: " + dataFirstYear);
    console.log("Forecast horizon: " + forecastHorizon);
    console.log("Last year: " + dataLastYear);
    console.log("Exit year: " + exitYear);

    // Validate EBITDA inputs


    // Validate exit year and forecast horizon
    if (exitYear < dataFirstYear || (exitYear > dataLastYear)) {
        reportField.innerHTML =
            "Exit year (" + exitYear + ") can not exceed financials data period " +
            "(" + dataFirstYear + "-" + dataLastYear + ")";
        form.ventureExitYear.classList.add('is-invalid');
        return false;
    } else form.ventureExitYear.classList.remove('is-invalid');


    return true;
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
        cell2.innerHTML = "<input type=\"text\" class=\"form-control text-end\" id=\"" + rowID
                          + "c2\" value=\"0\" oninput=\"onCurrencyInput(event)\" required>";
        cell3.innerHTML = "<input type=\"text\" class=\"form-control text-end\" id=\"" + rowID
                          + "c3\" value=\"0\" oninput=\"onCurrencyInput(event)\" required>";
        cell4.innerHTML = "<input type=\"text\" class=\"form-control text-end\" id=\"" + rowID
                          + "c4\" value=\"0\" oninput=\"onCurrencyInput(event)\" required>";
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
// Save form & report to local storage
//------------------------------------------------------------------------------------
function saveForm() {
    let jsonForm = formToJSON();
    let jsonString = JSON.stringify(jsonForm);
    localStorage.setItem("SavedForm", jsonString);
    console.log("Form saved to local storage:\n" + jsonString);
}

function saveReport() {
    let reportField = document.getElementById('valuationReport');
    let report = reportField.innerHTML;
    localStorage.setItem("valuationReport", report);
    console.log("Valuation report:\n" + report);
}


//------------------------------------------------------------------------------------
// Event listener on numeric inputs for cash with thousands separator
//------------------------------------------------------------------------------------
function onCurrencyInput(event) {
      let value = event.target.value;

      const position = event.target.selectionStart;
      const oldLength = value.length;

      value = value.replace(/\D/g, '');
      value = value.replace(/\B(?=(\d{3})+(?!\d))/g, THOUSANDS_SEPARATOR);

      event.target.value = value;

      const newLength = value.length;
      const adjustment = newLength - oldLength;
      event.target.setSelectionRange(position + adjustment, position + adjustment);
}

//---------------------------------------------------------------------------------------
// Extracts number from text (remove thousands separator)
//---------------------------------------------------------------------------------------
function extractNumber(value) {
    let num = value.replace(/\D/g, '');
    return Number(num);
}

//---------------------------------------------------------------------------------------
// Add thousands separator
//---------------------------------------------------------------------------------------
function addThousandsSeparators(str) {
      // Remove all non-digit characters
      let value = String(str);
      value = value.replace(/\D/g, '');
      // Format the number with thousands separators
      return value.replace(/\B(?=(\d{3})+(?!\d))/g, THOUSANDS_SEPARATOR);
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
    saveForm();
    saveReport();
}


//------------------------------------------------------------------------------------
// Event listener on when submit button pressed - sends request to back-end service
//------------------------------------------------------------------------------------
function onSubmit(event) {
    event.preventDefault();
    const form = event.target;

    // Format and logic verification
    if (!formDataVerification()) return false;

    // Retrieve data from form
    const companyData = formToJSON();
    // Show request body in report field
    console.log("Request body:\n" + JSON.stringify(companyData, null, 2));
    // Save form to local storage
    saveForm();

    // Waiting message
    const reportField = document.getElementById('valuationReport');
    reportField.innerHTML = "Loading data...";

    // Disable submit button to privent multiple requests while waiting
    const submitButton = document.getElementById('submitButton');
    submitButton.disabled = true;

    let currentURL = window.location.href;

    // Send request 
    fetch(VALUATION_SERVICE_URL,
        {
            method: "POST",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(companyData)
        }
    ).then(
        response => response.text()
    ).then((reportContent) =>  {
        console.log(reportContent);
        reportField.innerHTML = reportContent;
        saveReport();
        submitButton.disabled = false;
    }).catch (error => {
        console.error("Fetch failed: ", error)
        reportField.innerHTML = "Request to " + currentURL + VALUATION_SERVICE_URL + " failed:<br>" + error;
        submitButton.disabled = false;
    })

}


//------------------------------------------------------------------------------------
// Entry point
//------------------------------------------------------------------------------------
initialize()

