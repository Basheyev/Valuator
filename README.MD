
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


### Architecture
##### 1.1. CompanyData
Contains following private company data: legal entity name, country,
revenue, EBITDA, free cash flow forecasts dataset for 2-5 years, 
equity invested and its interest rate, debt borrowed and its interest 
rate, cash and equivalents, venture exit year and venture interest 
rate, market share and public comparable company stock ticker.



### Things to do:
1. Add more verification on input data for front-end and back-end
2. Add currency selection (national or USD) and currency tag in input fields
3. Add thousands space separator in input fields
4. Add more country codes
5. Find up-to-date tax rates and base rates data source
