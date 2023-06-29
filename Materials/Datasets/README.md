# Overview
This project relies on data from two primary sources: (1) The Graph; and (2) Amberdata. The Graph provides data for free, and we've used it to acquire the bulk of the transaction-level data for Aave's various deployments. Amberdata requires an API key. The data collection and pre-processing code is provided here, mostly segmented into two folders indicating where the data are coming from in each file. A hierarchy of the data collected here is as follows:

## Data From The Graph:

### Aave:
  We collect transaction-level data for Aave V2 and V3 from the following deployments: 
  * Ethereum (Mainnet) V2
  * Avalanche V2
  * Polygon (Matic) V2
  * Avalanche V3
  * Arbitrum V3
  * Fantom V3
  * Harmony V3
  * Polygon V3
  * Optimism V3
  
  From each of these deployments, we query a number of tables from The Graph in order to get historical transactions of each transaction type and to get relevant data about the users and coins at the time of each transaction. Data from each individual table is acquired in its own file, and all of the data collection for a single deployment of Aave from The Graph can be done by running the "getRawData.R" script within the deployment's folder. 
  
## Amberdata:

Amberdata does not allow for free access to its API, but have been kind enough to provide us an API key for this project. We are providing the code we've written to pull data for three different lending protocols using Amberdata's API, but in order to run this code, you must use your own API key.

### Aave:
  From Amberdata, we collect Aave data for its two deployments on Ethereum (V2 and V3)
### Compound:
  We collect data from the lending protocol Compound using Amberdata.
### MakerDAO:
  We collect data from the lending protocol MakerDAO using Amberdata.
