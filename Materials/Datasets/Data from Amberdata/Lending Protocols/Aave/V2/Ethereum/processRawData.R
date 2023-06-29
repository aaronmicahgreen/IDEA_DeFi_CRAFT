library(readr)
library(tidyr)
library(dplyr)
library(lubridate)
library(stringr)
library(stringi)
library(forcats)

dataPath = "~/data/IDEA_DeFi_Research/Data/Lending_Protocols/Aave/V2/Ethereum/"
rawDataPath = paste(dataPath, "Raw/", sep="")

## Helper functions
not_all_na <- function(x) any(!is.na(x))
`%notin%` <- Negate(`%in%`)


# The files for each transaction type:
borrowsFile = "rawBorrows.csv"
collateralsFile = "rawCollaterals.csv"
depositsFile = "rawDeposits.csv"
liquidationsFile = "rawLiquidations.csv"
redeemsFile = "rawWithdraws.csv"
repaysFile = "rawRepays.csv"


# Load the raw transaction tables:
rawBorrows <- read_csv(paste0(rawDataPath, borrowsFile))
rawCollaterals <- read_csv(paste0(rawDataPath, collateralsFile))
rawDeposits <- read_csv(paste0(rawDataPath, depositsFile))
rawLiquidations <- read_csv(paste0(rawDataPath, liquidationsFile))
rawWithdraws <- read_csv(paste0(rawDataPath, redeemsFile))
rawRepays <- read_csv(paste0(rawDataPath, repaysFile))

borrows <- rawBorrows %>%
  mutate(type = "borrow") %>%
  rename(onBehalfOf = caller) %>%
  distinct()

collaterals <- rawCollaterals %>%
  mutate(type = "collateral") %>%
  rename(toState = reserveAsCollateralEnabled) %>%
  distinct()

deposits <- rawDeposits %>%
  mutate(type = "deposit") %>%
  rename(account = user) %>%
  distinct()

liquidations <- rawLiquidations %>%
  mutate(type = "liquidation") %>%
  mutate(profitUSD = collateralAmountUSD - principalAmountUSD) %>%
  rename(account = liquidatee)

withdraws <- rawWithdraws %>%
  mutate(type = "withdraw") %>%
  rename(account = user,
         onBehalfOf = to) %>%
  distinct()

repays <- rawRepays %>%
  mutate(type = "repay") %>%
  rename(onBehalfOf = repayer,
         account = user) %>%
  distinct()


cleanedTransactions <- borrows %>%
  bind_rows(collaterals) %>%
  bind_rows(deposits) %>%
  bind_rows(liquidations) %>%
  bind_rows(withdraws) %>%
  bind_rows(repays) %>%
  rename(user = account)

#####
# Collect necessary addresses from Amberdata API and append column of address type to transaction data
#####

allLendingAddresses <- cleanedTransactions %>%
  select(user) %>%
  distinct() 
liquidatorAddresses <- cleanedTransactions %>%
  select(liquidator) %>%
  distinct() %>%
  rename(user = liquidator)
onBehalfOfAddresses <- cleanedTransactions %>%
  select(onBehalfOf) %>%
  distinct() %>%
  rename(user = onBehalfOf)

allLendingAddresses <- allLendingAddresses %>%
  bind_rows(liquidatorAddresses,
            onBehalfOfAddresses)

fileName <- "ethereumLendingAddresses.csv"
newFile = FALSE

collectedAddresses <- tryCatch({
  fread(paste0("~/data/IDEA_DeFi_Research/Data/Addresses/", fileName), select = "address")
},
error = function(cond) {
  message("Previous data not found.")
})

lendingAddressesToCollect <- allLendingAddresses %>%
  filter(user %notin% collectedAddresses$address) %>%
  drop_na()
url <- "https://web3api.io/api/v2/addresses"

for(address in lendingAddressesToCollect$user){
  
  queryString <- list(
    hash = address
  )
  
  response <- VERB("GET", url, 
                   add_headers('x-amberdata-blockchain-id' = 'ethereum-mainnet', 
                               'x-api-key' = Sys.getenv("AMBERDATA_API_KEY")), 
                   query = queryString, 
                   content_type("application/octet-stream"), 
                   accept("application/json"))
  
  response <- tryCatch({
    content(response, "text") %>% fromJSON() 
  },
  error = function(cond) {
    message("Bad Gateway. Trying request again.")
    NULL
  })
  if(is.null(response)){
    next
  }
  if(!is.null(response$errors)){
    next
  }
  if(response$payload$totalRecords == 0){
    next
  }
  addresses <- data.frame(response$payload)$records.hash$address
  responseData <- data.frame(response$payload) %>%
    select(-records.hash,
           -totalRecords) %>%
    rename(creator = records.creator,
           firstBlockNum = records.firstBlockNumber,
           firstTransactionHash = records.firstTransactionHash,
           timestamp = records.timestamp,
           addressType = records.type)
  
  responseData["address"] = addresses
  
  
  responseData[responseData == "NULL"] <- NA
  
  write_csv(responseData, paste0("~/data/IDEA_DeFi_Research/Data/Addresses/", fileName), append = !newFile)
  
  newFile = FALSE
  
}

addressTypes <- read_csv("~/data/IDEA_DeFi_Research/Data/Addresses/ethereumLendingAddresses.csv") %>%
  select(-timestamp, -firstBlockNum, -firstTransactionHash) 

cleanedTransactions <- cleanedTransactions %>%
  mutate(protocol = "Aave",
         version = "V2",
         deployment = "Ethereum") %>%
  select(-action, -blockNumber, -logIndex, -marketId, -assetId) %>%
  left_join(addressTypes, by = c("user" = "address")) %>%
  rename(userAddressType = addressType,
         userContractCreator = creator) %>%
  left_join(addressTypes, by = c("liquidator" = "address")) %>%
  rename(liquidatorAddressType = addressType,
         liquidatorContractCreator = creator) %>%
  left_join(addressTypes, by = c("onBehalfOf" = "address")) %>%
  rename(onBehalfOfAddressType = addressType,
         onBehalfOfContractCreator = creator) %>%
  mutate(timestamp = timestamp/1e3) %>%
  rename(reserve = assetSymbol,
         collateralReserve = collateralAssetSymbol,
         principalReserve = principalAssetSymbol)


saveRDS(cleanedTransactions, paste0(dataPath, "transactions.rds"))
