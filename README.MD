
## Private Company Valuator Service

### 1. Overview

The Valuator service performs company valuation using four main methods: 
EBITDA Multiple, Comparable Multiple, Discounted Cash Flow (DCF), 
and the VC Method.

To perform the calculations, service needs following private 
company data: revenue, EBITDA, free cash flow forecasts dataset 
for 2-5 years, equity invested and its interest rate, debt borrowed 
and its interest rate, cash and equivalents, venture exit year and 
venture interest rate, market share and public comparable company 
stock ticker.

World Bank API provides country GDP average growth, average inflation, 
corporate tax rate, base rate, and market return rate. Alpha Vantage 
API provides public comparable company stock data,


### 2. Back-end Architecture

##### 2.1. CompanyData class
Contains following private company data: legal entity name, country,
revenue, EBITDA, free cash flow forecasts dataset for 2-5 years, 
equity invested and its interest rate, debt borrowed and its interest 
rate, cash and equivalents, venture exit year and venture interest 
rate, market share and public comparable company stock ticker.

##### 2.2. CountryData class
Contains following country data: country code, currency,
GDP and Inflation values for specified period, average GDP growth rate,
average Inflation rate and values for corporate tax, central bank interest
rate (base rate), market return rate, currency formatter. Built-in 
integration with World Bank API and caching mechanism for faster response.

##### 2.3. StockData class
Contains following public company stock data: company legal name,
revenue for trailing twelve month (TTM), EBITDA, Operating margin (TTM),
gross profit (TTM), EV to Revenue, EV to EBITDA, Market Capitalization,
Enterprise Value (V), Earning Per Share (EPS), Revenue Per Share (TTM).
Built-in integration with Alpha Vantage API and caching mechanism 
for faster response and Alpha Vantage limitations (25 request a day).


##### 2.4. Cached Data class
Provides caching mechanism to store World Bank Country Data and
Public Company Stock Data in key/value embedded storage (MapDB).


##### 2.5. Valuator Engine class
Provides implementation of EBITDA Multiple, Comparable Multiple, 
Discounted Cash Flow (DCF), and the VC valuation methods. Generates
HTML or text valuation report.


##### 2.6. FinancialMath class
Implements financial math formulas to calculate CAGR, WACC, CAPM,
DCF, Terminal Value, Present Value.

##### 2.7. ValuationService class
REST end-point API that validates input data Company Data JSON object  
and builds HTML valuation report using Valuator Engine. Based on
Spark embedded microframework (jetty).

### 3. Front-end Architecture

#### 3.1. Index.html & formController.js
Provides company data form with format and logic validation (JS),
sends request to valuation service and shows valuation report HTML.

#### 3.2. Methodology.html
Valuation method statement and disclaimer.


### 4. Things to do:
1. Add verification on input data for front-end
2. Add verification on input data for back-end
3. Add currency selection (national or USD) 
4. Add currency tag in input fields
5. Find up-to-date tax rates and base rates data source
6. Save & load different companies data
7. Add more country codes
