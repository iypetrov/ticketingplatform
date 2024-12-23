populate:
    @curl -X 'POST' \
        'http://localhost:8081/event/' \
        -H 'accept: */*' \
        -H 'Content-Type: application/json' \
        -d '{
        "name": "rock and roll",
        "description": "nice concert",
        "location": "sofia",
        "starttime": "2025-01-10 20:00:00",
        "endtime": "2025-01-10 23:00:00",
        "numberticketsfirstclass": 20,
        "pricefirstclass": 150,
        "numberticketssecondclass": 100,
        "pricesecondclass": 90,
        "numberticketsthirdclass": 150,
        "pricethirdclass": 60
        }'

build:
	@kafka-topics --bootstrap-server localhost:9092 --create --topic events-topic --partitions 1 --replication-factor 1
	@kafka-topics --bootstrap-server localhost:9092 --create --topic payments-topic --partitions 1 --replication-factor 1