## java-filmorate
#### Hi! It's my first backend Spring project on Java - Filmorate.

See at the database diagram below:

![alt tag](/diagram.png)

Request examples: **get users name, birthday**:
```sql
SELECT 
       u.first_name AS user_name,
       u.birthday   AS user_birthday
  FROM user AS u
 LIMIT 10
;
```

Request examples: **get films id, name, duration, rating**:
```sql
SELECT 
       f.film_id,
       f.name,
       f.duration
       r.name
  FROM film AS f
  LEFT JOIN rating_mpa AS r
    ON f.rating_id = r.rating_id
;
```