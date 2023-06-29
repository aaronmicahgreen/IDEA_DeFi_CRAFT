
fileName <- "rawRepays.csv"
lastTimestamp = getLastDate(fileName)
newFile = FALSE

if(is.null(lastTimestamp)){
  lastTimestamp = 1556683200000 # this is the timestamp for May 1, 2019. This is before compoundV2 launched, so the first few days might not return data.
  newFile = TRUE
  possibleDuplicates = NULL
}else{
  possibleDuplicates = fread(paste0(dataPath, fileName)) %>% tail(1000) %>%
    filter(timestamp == lastTimestamp)
}


repeat{
  
  if(is.infinite(lastTimestamp) || lastTimestamp/1e3 > now()){
    break # If we've collected all the data up through today, we are done.
  }
  
  queryString <- list(
    startDate = lastTimestamp,
    endDate = lastTimestamp + 5184000000, # We need to provide an end date for the API call, and the API will max at returning 60 days of data at a time. Let's add 60 days (in milliseconds) to the startDate
    direction = "ascending",
    timeFormat = "milliseconds",
    action = "Repay"
  )
  
  response <- VERB("GET", apiURL, 
                   add_headers('x-api-key' = apiKey), 
                   query = queryString, 
                   content_type("application/octet-stream"), 
                   accept("application/json"))
  
  tryCatch({
    responseData <- fromJSON(content(response, "text"))$payload$data
  },
  error = function(cond) {
    message("Bad Gateway. Trying request again.")
    next
  })
  if(!is.null(response$errors)){
    next
  }
  if(length(responseData$timestamp) == 0){
    lastTimestamp = lastTimestamp + 5184000000
    next
  }

  responseData[responseData == "NULL"] <- NA
  
  if(!is.null(possibleDuplicates)){
    responseData <- responseData %>%
      filter(transactionHash %notin% possibleDuplicates$transactionHash)
  }
  
  
  write_csv(responseData, paste0(dataPath, fileName), append = !newFile)
  
  
  lastTimestamp = max(responseData$timestamp) # We use the last transaction's timestamp as the starting timestamp for the next call.
  
  possibleDuplicates <- responseData %>%
    filter(timestamp == lastTimestamp)
  
  
  newFile = FALSE
  
}


