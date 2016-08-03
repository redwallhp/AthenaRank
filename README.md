# AthenaRank

Ranking tracker for AthenaGM. Tracks kills, deaths and captures in a MySQL database. Handles the generation of a leaderboard in the Hub.

## Example Config

```
database:
  url: "jdbc:mysql://localhost:3306/athenarank"
  username: archer
  password: guest

leaderboard:
  enabled: false
  heads:
  - "0,0,0"
  - "0,0,0"
  - "0,0,0"
  signs:
  - "0,0,0"
  - "0,0,0"
  - "0,0,0"
```

Heads and signs may be arranged however you wish. The only limits are:

* Heads and signs are processed in order, with the first one being the #1 ranked player.

* You can have no more than 10 head/sign pairs, as only ten players are loaded from the database.

## Ranking Formula

The following query determines the player rank. To implement a web page, this should be used:

```
SELECT *, (kills + captures) / GREATEST(30, kills + captures + deaths) AS 'rank' FROM rankings ORDER BY rank DESC;
```
