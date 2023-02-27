select * from messages;
update messages set status='PENDING', retry_count=1, trigger_time='2023-02-20 20:08:41.740000' where id in (9,10,11,12);
delete from messages;

curl -X POST \
http://localhost:8080/messages/create \
-H 'Content-Type: application/json' \
-d '{
"url": "https://worldtimeapi.org/api/ip",
"httpMethod": "GET",
"status": "PENDING",
"triggerTime": "2023-02-20T10:00:00"
}'



curl -X POST   http://localhost:8080/messages/create   
-H 'Content-Type: application/json'   
-d '{
"url": "localhost:8080/messages/create",
"httpMethod": "POST",
"headers": {
"Content-Type": "application/json"
},
"body": {
"name": "John Doe",
"age": "30"
},
"status": "PENDING",
"triggerTime": "2023-02-20T10:00:00"
}'

curl -X POST \
-H 'Content-Type: application/json' \
-d '{
"url": "https://httpbin.org/post",
"httpMethod": "POST",
"headers": {
"Content-Type": "application/json"
},
"body": {
"name": "John Doe",
"age": "30"
},
"status": "PENDING",
"triggerTime": "2023-02-20T10:00:00"
}' \
http://localhost:8080/messages/create 
