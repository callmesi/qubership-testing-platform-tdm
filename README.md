# Qubership Testing Platform TDM

The main goal of the TDM simplify test data usage and management on the project for Standalone/End-to-End testing manual and automated.

The concept of test data management assumes usage of TDM tool as one centralized data storage, for creation, update  and tracking of test data usage on different environments.

This approach gives user one entry point for test data usage on different environment, new scripts for test data collection or update can be performed in few clicks on different servers.

# How to prepare local DB before application running.
1. Download and install [PostgreSQL](https://www.postgresql.org/download/)
2. Create database
 ```sh 
 CREATE DATABASE qstptdm; 
 ```
3. Create user tdmadmin (User name and below password are example ones; please change them, and change service configuration variables TDM_DB_USER and TDM_DB_PASSWORD accordingly)
 ```sh 
 CREATE USER tdmadmin WITH PASSWORD 'tdmadmin';  
 ```
4. Grant privileges on database to user
 ```sh 
 GRANT ALL PRIVILEGES ON DATABASE "qstptdm" to tdmadmin;
 ``` 
5. To setup local copy of database you need to build project by maven "clean" and "package".
   Please notice that Maven must be configured with `Profiles -> migration-on-build-pg` property or it WILL NOT setup basic DB strusture. If you had already set up DB structure, don't use this property.
   Basic structure of DB (constraints and tables) is scripted in migration module (src/main/scripts/install.xml)

# How to start Backend

1. Main class `org.qubership.atp.tdm.Main`
2. VM options (contains links, can be edited in parent-db pom.xml):
   `
   -Dspring.config.location=C:\qstp-tdm\qubership-atp-tdm-backend\target\config\application.properties
   -Dspring.cloud.bootstrap.location=C:\qstptdm\qubership-atp-tdm-backend\target\config\bootstrap.properties
   -Dfeign.atp.catalogue.url=https://atp-catalogue:8080
   -Dfeign.atp.environments.url=https://environments:8080
   `
3. Select "Working directory" `$MODULE_WORKING_DIRS$`

Just run Main#main with args from step above

# How to start Tests with PostgreSql
1. PostgreSQL installed local

1.2. Download and install [PostgreSQL](https://www.postgresql.org/download/).

1.2. Create database: qstptdmtest

1.3. Port: 5433

1.4. Create user and pass tdmadmin / tdmadmin (User name and below password are example ones; please change them, and change service configuration variables TDM_DB_USER and TDM_DB_PASSWORD accordingly)

1.5. Grant privileges on database to user

# How to start Tests with Docker
2. Docker installed local

1.2. VM options: -DLOCAL_DOCKER_START=true

# How to run backend

1. Build project: build by maven "clean" and "package", run as backend on port 8080.

# How to deploy tool

1. Build snaphot (artifacts and docker image) of https://github.com/Netcracker/qubership-testing-platform-tdm in GitHub
2. Clone repository to a place, available from your openshift/kubernetes where you need to deploy the tool to
3. Navigate to <repository-root>/deployments/charts/atp-tdm folder
4. Check/change configuration parameters in the ./values.yaml file according to your services installed
5. Execute the command: helm install qstp-tdm
6. After installation is completed, check deployment health
