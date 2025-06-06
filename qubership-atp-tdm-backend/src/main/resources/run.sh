#!/bin/sh

cat >./web/assets/routes.json <<EOF
{
  "loginRequired": false,
  "idp": {
    "realm": "${KEYCLOAK_REALM:?}",
    "url": "${KEYCLOAK_AUTH_URL%/*}",
    "loginEndPoint": "/auth/realms/${KEYCLOAK_REALM}/protocol/openid-connect/auth",
    "logoutEndPoint": "/auth/realms/${KEYCLOAK_REALM}/protocol/openid-connect/logout"
  },
  "services": [
    {
      "name": "tdm",
      "url": "${IDENTITY_PROVIDER_URL:?}"
    }
  ]
}
EOF

if [ "${ATP_INTERNAL_GATEWAY_ENABLED:-false}" = "true" ]; then
  echo "Internal gateway integration is enabled."
  FEIGN_ATP_CATALOGUE_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_ENVIRONMENTS_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_EI_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_USERS_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_MAILSENDER_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_HIGHCHARTS_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
else
  echo "Internal gateway integration is disabled."
  FEIGN_ATP_CATALOGUE_ROUTE=
  FEIGN_ATP_ENVIRONMENTS_ROUTE=
  FEIGN_ATP_EI_ROUTE=
  FEIGN_ATP_USERS_ROUTE=
  FEIGN_ATP_MAILSENDER_ROUTE=
  FEIGN_ATP_HIGHCHARTS_URL=
fi

JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.devtools.add-properties=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dlog.graylog.on=${LOG_GRAYLOG_ON:-false}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Djdbc.MinIdle=20 -Djdbc.MaxPoolSize=50"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.config.location=file:./config/application.properties"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.cloud.bootstrap.location=file:./config/bootstrap.properties"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.database=${EI_GRIDFS_DB:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.host=${EI_GRIDFS_DB_ADDR:-$GRIDFS_DB_ADDR}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.port=${EI_GRIDFS_DB_PORT:-$GRIDFS_DB_PORT}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Datp.audit.logging.topic.name=${AUDIT_LOGGING_TOPIC_NAME:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Datp.audit.logging.enable=${AUDIT_LOGGING_ENABLE:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Datp.audit.logging.topic.partitions=${AUDIT_LOGGING_TOPIC_PARTITIONS:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Datp.audit.logging.topic.replicas=${AUDIT_LOGGING_TOPIC_REPLICAS:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Datp.reporting.kafka.producer.bootstrap.server=${KAFKA_REPORTING_SERVERS:?}"

/usr/bin/java ${JAVA_OPTIONS} -XX:+PrintFlagsFinal -XX:MaxRAM=${MAX_RAM:-1024m} -cp "./config/:./lib/*" org.qubership.atp.tdm.Main
