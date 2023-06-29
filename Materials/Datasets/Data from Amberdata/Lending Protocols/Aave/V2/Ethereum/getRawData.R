library(ghql)
library(jsonlite)
library(stringr)
library(dplyr)
library(lubridate)
library(readr)
library(stringr)
library(urltools)
library(R.utils)
library(data.table)
library(httr)

apiURL <- "https://web3api.io/api/v2/defi/lending/aavev2/protocol"
apiKey = Sys.getenv("AMBERDATA_API_KEY")
dataPath = "~/data/IDEA_DeFi_Research/Data/Lending_Protocols/Aave/V2/Ethereum/Raw/"
filePath = "~/DeFi_Data_Collection/Lending_Protocols/Aave/V2/Ethereum/getRawTables/"
`%notin%` <- Negate(`%in%`)
getLastDate <- function(fileName){
  tryCatch({
    max(fread(paste0(dataPath, fileName), select = "timestamp")$timestamp)
  },
  error = function(cond) {
    message("Previous data not found.")
  })
}

source(paste0(filePath, "getRawBorrows.R"))
source(paste0(filePath, "getRawRepays.R"))
source(paste0(filePath, "getRawDeposits.R"))
source(paste0(filePath, "getRawWithdraws.R"))
source(paste0(filePath, "getRawLiquidations.R"))
source(paste0(filePath, "getRawCollaterals.R"))


