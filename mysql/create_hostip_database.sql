-- Create HostIp database
-- 1) gunzip hostip_current.sql.gz
-- 2) file hostip_current.sql
--   ( Expected result: hostip_current.sql: ASCII text, with very long lines )
-- 3) Create database
    mysql -u root -p -e "create database XCURE_HOSTIP_DB;"
-- 4) Upload data to the database
    mysql -u root -p XCURE_HOSTIP_DB < hostip_current.sql
-- 5) Allow access t the database by the user
    mysql -u root -p -e "GRANT SELECT ON XCURE_HOSTIP_DB.* TO 'xcure-server'@'localhost';"
-- 6) Get required data from the database
    SELECT cityByCountry.name as city, cityByCountry.state as state,
    countries.name as country, countries.code as country_code,
    cityByCountry.lat as lat, cityByCountry.lng as lon
    FROM ?, countries, cityByCountry
    WHERE ?.city = cityByCountry.city AND
    ?.country = cityByCountry.country AND
    ?.country = countries.id AND b=? AND c=?;
-- Here parameters 1--4 are formed like "ip4_" + the first 3 IP number
-- parameters 5--6 are the second and the third IP number correspondingly
--      IP: ???.???.???.???
-- Numbers:  1   2   3   4

