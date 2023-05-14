## java-filmorate
#### Hi! It's my first backend Spring project on Java - Filmorate.

See at the database diagram below:

![alt tag](/diagram.png)

Request examples: **get user name, birthday**:
```sql
SELECT 
       u.first_name AS user_name,
       u.birthday   AS user_birthday
  FROM user AS u
 LIMIT 10
;
```

Request examples: **get film id, name, duration, rating**:
```sql
SELECT 
       f.film_id,
       f.name     AS film_name,
       f.duration AS film_duration,
       r.name     AS film_rating
  FROM film AS f
  LEFT JOIN rating_mpa AS r
    ON f.rating_id = r.rating_id
;
```

Request examples: **get film name, count likes**:
```sql
SELECT 
       f.name            AS film_name,
       COUNT(fl.film_id) AS film_likes
  FROM film AS f
  LEFT JOIN film_likes AS fl
    ON f.film_id = fl.film_id
 GROUP 
    BY film_name
;
```