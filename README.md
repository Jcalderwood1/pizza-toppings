# pizza-toppings

pizza-toppings is a Kotlin + Spring Boot webserver made out of a desire to learn about Kotlin and the state of web development on the JVM.

## Prerequisites

- Java 17
- Docker Desktop
- Make

## Setup 

```bash
# start up the postgres docker container, build and run the app
> make

# after the app is finished building and starting, run load tests in another terminal/shell process
> make load-tests

# tear down the app, remove postgres volumes (start from scratch)
> make clean
```

## API
### Submit pizza toppings you are interested in:
```bash
curl --location --request POST 'localhost:8080/toppings' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "foo@bar.com",
    "toppings": ["cheese", "cheese", "olives", "pineapple"]
}'
```
### Get a breakdown of topping votes:
```bash
curl --location --request GET 'localhost:8080/toppingVotes'
# returns:
[
   {
      "topping": "pepperoni",
      "votes": 907
   },
   {
      "topping": "cheese",
      "votes": 1043
   },
   ...
]
```
## Features
### Sort Topping Vote Summary
Clients can pass query paramters to choose to sort the topping vote summary alphabetically or by number of votes, ascending or descending.
```bash
# Sort by topping name A-Z
localhost:8080/toppingVotes?sortBy=asc(topping)
# Sort by topping name Z-A
localhost:8080/toppingVotes?sortBy=desc(topping)

# Sort by number of votes low to high
localhost:8080/toppingVotes?sortBy=asc(votes)
# Sort by number of votes high to low
localhost:8080/toppingVotes?sortBy=desc(votes)
```
### Postgres Materialized View
The topping vote summary is powered by a postgres materialized view. In order to keep the response times snappy, the materialzed view is refreshed on a 3000ms schedule. This allows users to submit thier votes without having to wait for the materialized view to refresh.

Querying for the vote summary had a max response time of 400ms under heavy load during my local testing on my MacBook Pro:
### JMeter Load Tests
```bash
summary +     1 in 00:00:00 =    3.0/s Avg:  51 Min:  51 Max:  51 Err: 0
summary + 40840 in 00:00:24 = 1697.1/s Avg: 116 Min:   2 Max: 378 Err: 0
summary = 40841 in 00:00:24 = 1673.8/s Avg: 116 Min:   2 Max: 378 Err: 0
summary + 39159 in 00:00:24 = 1661.5/s Avg: 118 Min:   1 Max: 392 Err: 0
summary = 80000 in 00:00:48 = 1667.7/s Avg: 117 Min:   1 Max: 392 Err: 0
```


## License

[MIT](https://choosealicense.com/licenses/mit/)
