
.PHONY: restart
restart:
	mvn clean package
	docker-compose down --volumes
	docker-compose up -d --build

# Quick restart without rebuilding (preserves data)
.PHONY: quick-restart
quick-restart:
	docker-compose down
	docker-compose up -d

# Reset database only (keeps app running)
.PHONY: reset-db
reset-db:
	docker-compose stop mysql
	docker-compose rm -f mysql
	docker volume rm app-modernization-plants-by-websphere-jee6_mysql_data || true
	docker-compose up -d mysql --build

.PHONY: mysql-console
mysql-console:
	mysql -h 127.0.0.1 -P 3306 -u pbwuser -ppbwpass plantsdb
