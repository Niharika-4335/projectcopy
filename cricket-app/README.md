**Cricket Betting Application**

This project is a REST API-based Cricket Betting Application, built with Spring Boot.

**Features**

It provides a platform for:  

1.Creating players and admin users

2.Managing cricket matches

3.Allowing players to place bets on existing matches

4.Calculating the total losing pool after a match

5.Generating and distributing payouts to the winning players

**Technologies Used**

Java 23

Spring Boot 3.4.4

Spring Security (JWT authentication)

Spring Data JPA (Hibernate)

MySQL Database

OpenAPI for API Documentation

Maven for build and dependency management

Lombok

**Entity Descriptions**

Users -> Stores player and admin information

Wallet -> Manages user balances

WalletTransaction -> Logs all credits and debits

Match -> Represents cricket matches with statuses (Upcoming, Ongoing, Completed,AutoCompleted)

Bet -> Stores player bets placed on matches

Payout -> Handles the payout process and winning distributions

**Entity Relationships**

Users -> Wallet (One-to-One)

Wallet -> WalletTransaction (One-to-Many)

Match -> Bet (One-to-Many)

Users -> Bet (One-to-Many)


