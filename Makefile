default: run

.PHONY: start-services
start-services:
	docker-compose up -d

.PHONY: purge-services
purge-services:
	docker-compose down --volumes

.PHONY: run
run: start-services
	./gradlew bootRun

.PHONY: load-test
load-test: start-services
	./gradlew runJMeter

.PHONY: test
test: start-services
	./gradlew test

.PHONY: clean
clean: purge-services
	./gradlew clean