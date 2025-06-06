FROM artifactory-service-address/path-to-image

ENV POSTGRES_USER=tdmadmin \
POSTGRES_PASSWORD=tdmadmin \
POSTGRES_DB=atptdm

COPY create_extension.sql /docker-entrypoint-initdb.d/create_extension.sql

EXPOSE 5432
