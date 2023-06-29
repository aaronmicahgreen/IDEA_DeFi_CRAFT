# Prerequisites

Prior to using this guide, please ensure you have the following installed:
1) [r-lang](https://cran.r-project.org/bin/)
2) [r-studio](https://animation--rstudio-com.netlify.app/products/rstudio/download/)

Also make sure you are connected to the RPI network, such that you can ping the address `defi-de.idea.rpi.edu` and get a response.

# Setup

Before we start we need to retrieve the necessary files from the repository. They can be found [here](https://github.rpi.edu/DataINCITE/IDEA-DeFi-CRAFT/tree/main/R-Code-Samples). In this repository there are three files:

- `DataEnginePrimaryFunctions.Rmd`: This file contains the main function for interacting with the data engine which will be explained later.
- `GetTransactions.Rmd`: This file contains the functions required to retrieve and parse transaction data from the data engine.
- `ExampleUserClusteringStarter.Rmd`: A sample user clustering example using the data parsed from the `GetTransactions.Rmd` file.

Only `GetTransactions.Rmd` and `ExampleUserClusteringStart.Rmd` are needed for this example however you can download all three files if wanted. Download these files and navigate to the directory they are located in.

# GetTransactions Overview

Open `GetTransactions.Rmd` and `ExampleUserClusteringStarter.Rmd`. This file contains three functions (excluding the `request()` function). These functions are as follows:

- `get_users()`: This function loads all user data from The Graph using the request() function defined above. Only one call needs to be made to this function so long as the data is loaded in the cache. Since it is a process-intensive call, it is recommended to only call this once and then pass the output as a parameter to the `get_data()` function in the `users` parameter.
- `get_data(startdate, enddate, users)`: This function will request all necessary data to compute the transaction data-frame. This includes multiple calls to The Graph which can be seen below.
- `get_transactions()`: This function takes input returned from the `get_data(startdate, enddate, users)` function to parse a table with all properly formatted transaction data.

# Quickstart Steps

1) Run `GetTransactions.Rmd`. After finishing this will produce a data frame called `transactions` which can then be used in the `ExampleUserClusteringStarter.Rmd`. The `transactions` data frame should look similar to below:
<p align="center"><img width="720" src="https://media.github.rpi.edu/user/441/files/0441d72b-5733-48e2-8911-21aac2983d97"></p>

2) Run `ExampleUserClusteringStarter.Rmd`. This will use the `transactions` variable produced by `GetTransactions.Rmd`. Once it finishes you should see various plots that should look similar to:

<p align="center"><img width="720" src="https://media.github.rpi.edu/user/441/files/7dbccbac-64a1-4a56-bd9e-ef11ca7aef18"></p>
<p align="center"><img width="720" src="https://media.github.rpi.edu/user/441/files/170c959a-30ef-4f24-b579-293d933f541b"></p>

# Errors and Edge Cases

## Non-Standard Code Response

If you get a response with an empty data frame, review the code returned and compare it to the codes listed in the wiki [here](https://github.rpi.edu/DataINCITE/IDEA-DeFi-CRAFT/wiki/Response-Codes). Code severity can vary, with outfacing codes being limited to a select few.


## Heartbeat Parsing Error

Occasionally the `request()` function will return the error below. This is a known error which means the function had an issue parsing and capturing the heartbeat confirmation text. A solution has most likely been found and applied to the code, however since this bug cannot be reproduced it is not known if it is fully patched. If it occurs, please re-run the method it occurred in.
![image](https://media.github.rpi.edu/user/441/files/cc5ee92e-6536-496f-9872-e670d05c9da0)
