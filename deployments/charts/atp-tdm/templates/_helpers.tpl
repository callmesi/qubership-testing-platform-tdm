{{/* Helper functions, do NOT modify */}}
{{- define "env.default" -}}
{{- $ctx := get . "ctx" -}}
{{- $def := get . "def" | default $ctx.Values.SERVICE_NAME -}}
{{- $pre := get . "pre" | default (eq $ctx.Values.PAAS_PLATFORM "COMPOSE" | ternary "" $ctx.Release.Namespace) -}}
{{- get . "val" | default ((empty $pre | ternary $def (print $pre "_" (trimPrefix "atp-" $def))) | nospace | replace "-" "_") -}}
{{- end -}}

{{- define "env.factor" -}}
{{- $ctx := get . "ctx" -}}
{{- get . "def" | default (eq $ctx.Values.PAAS_PLATFORM "COMPOSE" | ternary "1" (default "3" $ctx.Values.KAFKA_REPLICATION_FACTOR)) -}}
{{- end -}}

{{- define "env.compose" }}
{{- range $key, $val := merge (include "env.lines" . | fromYaml) (include "env.secrets" . | fromYaml) }}
{{ printf "- %s=%s" $key $val }}
{{- end }}
{{- end }}

{{- define "env.cloud" }}
{{- range $key, $val := (include "env.lines" . | fromYaml) }}
{{ printf "- name: %s" $key }}
{{ printf "  value: \"%s\"" $val }}
{{- end }}
{{- $keys := (include "env.secrets" . | fromYaml | keys | uniq | sortAlpha) }}
{{- if eq (default "" .Values.ENCRYPT) "secrets" }}
{{- $keys = concat $keys (list "ATP_CRYPTO_KEY" "ATP_CRYPTO_PRIVATE_KEY") }}
{{- end }}
{{- range $keys }}
{{ printf "- name: %s" . }}
{{ printf "  valueFrom:" }}
{{ printf "    secretKeyRef:" }}
{{ printf "      name: %s-secrets" $.Values.SERVICE_NAME }}
{{ printf "      key: %s" . }}
{{- end }}
{{- end }}

{{- define "env.host" -}}
{{- $url := .Values.ATP_TDM_URL -}}
{{- if $url -}}
{{- regexReplaceAll "http(s)?://(.*)" $url "${2}" -}}
{{- else -}}
{{- $hosts := dict "KUBERNETES" "atp2k8.managed.some-domain.cloud" "OPENSHIFT" "dev-atp-cloud.some-domain.com" -}}
{{- print .Values.SERVICE_NAME "-" .Release.Namespace "." (.Values.CLOUD_PUBLIC_HOST | default (index $hosts .Values.PAAS_PLATFORM)) -}}
{{- end -}}
{{- end -}}

{{- define "securityContext.pod" -}}
runAsNonRoot: true
seccompProfile:
  type: "RuntimeDefault"
{{- with .Values.podSecurityContext }}
{{ toYaml . }}
{{- end -}}
{{- end -}}

{{- define "securityContext.container" -}}
allowPrivilegeEscalation: false
capabilities:
  drop: ["ALL"]
{{- with .Values.containerSecurityContext }}
{{ toYaml . }}
{{- end -}}
{{- end -}}
{{/* Helper functions end */}}

{{/* Environment variables to be used AS IS */}}
{{- define "env.lines" }}
ATP_HTTP_LOGGING: "{{ .Values.ATP_HTTP_LOGGING }}"
ATP_HTTP_LOGGING_HEADERS: "{{ .Values.ATP_HTTP_LOGGING_HEADERS }}"
ATP_HTTP_LOGGING_HEADERS_IGNORE: "{{ .Values.ATP_HTTP_LOGGING_HEADERS_IGNORE }}"
ATP_HTTP_LOGGING_URI_IGNORE: "{{ .Values.ATP_HTTP_LOGGING_URI_IGNORE }}"
ATP_INTERNAL_GATEWAY_ENABLED: "{{ .Values.ATP_INTERNAL_GATEWAY_ENABLED }}"
ATP_SERVICE_PATH: "{{ .Values.ATP_SERVICE_PATH }}"
ATP_SERVICE_PUBLIC: "{{ .Values.ATP_SERVICE_PUBLIC }}"
AUDIT_LOGGING_ENABLE: "{{ .Values.AUDIT_LOGGING_ENABLE }}"
AUDIT_LOGGING_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.AUDIT_LOGGING_TOPIC_NAME "def" "audit_logging_topic") }}"
AUDIT_LOGGING_TOPIC_PARTITIONS: '{{ .Values.AUDIT_LOGGING_TOPIC_PARTITIONS }}'
AUDIT_LOGGING_TOPIC_REPLICAS: "{{ include "env.factor" (dict "ctx" . "def" .Values.AUDIT_LOGGING_TOPIC_REPLICAS) }}"
CONSUL_ENABLED: "{{ .Values.CONSUL_ENABLED }}"
CONSUL_HEALTH_CHECK_ENABLED: "{{ .Values.CONSUL_HEALTH_CHECK_ENABLED }}"
CONSUL_PORT: "{{ .Values.CONSUL_PORT }}"
CONSUL_PREFIX: "{{ .Values.CONSUL_PREFIX }}"
CONSUL_TOKEN: "{{ .Values.CONSUL_TOKEN }}"
CONSUL_URL: "{{ .Values.CONSUL_URL }}"
CONTENT_SECURITY_POLICY: "{{ .Values.CONTENT_SECURITY_POLICY }}"
EI_GRIDFS_DB: "{{ include "env.default" (dict "ctx" . "val" .Values.EI_GRIDFS_DB "def" "atp-ei-gridfs") }}"
ENVIRONMENTS_CACHE_DURATIONS: "{{ .Values.ENVIRONMENTS_CACHE_DURATIONS }}"
ENVIRONMENTS_SPRING_CACHE_TYPE: "{{ .Values.ENVIRONMENTS_SPRING_CACHE_TYPE }}"
EUREKA_CLIENT_ENABLED: "{{ .Values.EUREKA_CLIENT_ENABLED }}"
EXTERNAL_QUERY_DEFAULT_TIMEOUT: "{{ .Values.EXTERNAL_QUERY_DEFAULT_TIMEOUT }}"
EXTERNAL_QUERY_MAX_TIMEOUT: "{{ .Values.EXTERNAL_QUERY_MAX_TIMEOUT }}"
FEIGN_ATP_CATALOGUE_NAME: "{{ .Values.FEIGN_ATP_CATALOGUE_NAME }}"
FEIGN_ATP_CATALOGUE_ROUTE: "{{ .Values.FEIGN_ATP_CATALOGUE_ROUTE }}"
FEIGN_ATP_CATALOGUE_URL: "{{ .Values.FEIGN_ATP_CATALOGUE_URL }}"
FEIGN_ATP_EI_NAME: "{{ .Values.FEIGN_ATP_EI_NAME }}"
FEIGN_ATP_EI_ROUTE: "{{ .Values.FEIGN_ATP_EI_ROUTE }}"
FEIGN_ATP_EI_URL: "{{ .Values.FEIGN_ATP_EI_URL }}"
FEIGN_ATP_ENVIRONMENTS_NAME: "{{ .Values.FEIGN_ATP_ENVIRONMENTS_NAME }}"
FEIGN_ATP_ENVIRONMENTS_ROUTE: "{{ .Values.FEIGN_ATP_ENVIRONMENTS_ROUTE }}"
FEIGN_ATP_ENVIRONMENTS_URL: "{{ .Values.FEIGN_ATP_ENVIRONMENTS_URL }}"
FEIGN_ATP_HIGHCHARTS_NAME: "{{ .Values.FEIGN_ATP_HIGHCHARTS_NAME }}"
FEIGN_ATP_HIGHCHARTS_ROUTE: "{{ .Values.FEIGN_ATP_HIGHCHARTS_ROUTE }}"
FEIGN_ATP_HIGHCHARTS_URL: "{{ .Values.FEIGN_ATP_HIGHCHARTS_URL }}"
FEIGN_ATP_INTERNAL_GATEWAY_NAME: "{{ .Values.FEIGN_ATP_INTERNAL_GATEWAY_NAME }}"
FEIGN_ATP_MAILSENDER_NAME: "{{ .Values.FEIGN_ATP_MAILSENDER_NAME }}"
FEIGN_ATP_MAILSENDER_ROUTE: "{{ .Values.FEIGN_ATP_MAILSENDER_ROUTE }}"
FEIGN_ATP_MAILSENDER_URL: "{{ .Values.FEIGN_ATP_MAILSENDER_URL }}"
FEIGN_ATP_USERS_NAME: "{{ .Values.FEIGN_ATP_USERS_NAME }}"
FEIGN_ATP_USERS_ROUTE: "{{ .Values.FEIGN_ATP_USERS_ROUTE }}"
FEIGN_ATP_USERS_URL: "{{ .Values.FEIGN_ATP_USERS_URL }}"
FEIGN_CONNECT_TIMEOUT: {{ .Values.FEIGN_CONNECT_TIMEOUT | int | quote }}
FEIGN_READ_TIMEOUT: {{ .Values.FEIGN_READ_TIMEOUT | int | quote }}
FROM_EMAIL_ADDRESS: "{{ .Values.FROM_EMAIL_ADDRESS }}"
GRIDFS_DB_ADDR: "{{ .Values.GRIDFS_DB_ADDR }}"
GRIDFS_DB_PORT: "{{ .Values.GRIDFS_DB_PORT }}"
IDENTITY_PROVIDER_URL: "{{ default .Values.IDENTITY_PROVIDER_URL .Values.ATP_TDM_URL }}"
JAVA_OPTIONS: "{{ if .Values.HEAPDUMP_ENABLED }}-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/diagnostic{{ end }} -Djdbc.Url=jdbc:postgresql://{{ .Values.PG_DB_ADDR }}:{{ .Values.PG_DB_PORT }}/{{ include "env.default" (dict "ctx" . "val" .Values.TDM_DB "def" .Values.SERVICE_NAME ) }} -Dcom.sun.management.jmxremote={{ .Values.JMX_ENABLE }} -Dcom.sun.management.jmxremote.port={{ .Values.JMX_PORT }} -Dcom.sun.management.jmxremote.rmi.port={{ .Values.JMX_RMI_PORT }} -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false {{ .Values.ADDITIONAL_JAVA_OPTIONS }}"
KAFKA_CLIENT_ID: "{{ .Values.SERVICE_NAME }}"
KAFKA_ENABLE: "{{ .Values.KAFKA_ENABLE }}"
KAFKA_ENVIRONMENTS_EVENT_CONSUMER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_ENVIRONMENTS_EVENT_CONSUMER_TOPIC_NAME "def" "environments_notification_topic") }}"
KAFKA_GROUP_ID: "{{ .Values.SERVICE_NAME }}"
KAFKA_PROJECT_EVENT_CONSUMER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_PROJECT_EVENT_CONSUMER_TOPIC_NAME "def" "catalog_notification_topic") }}"
KAFKA_REPORTING_SERVERS: "{{ .Values.KAFKA_REPORTING_SERVERS }}"
KAFKA_SERVERS: "{{ .Values.KAFKA_SERVERS }}"
KAFKA_SERVICE_ENTITIES_TOPIC: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_SERVICE_ENTITIES_TOPIC "def" "service_entities") }}"
KAFKA_SERVICE_ENTITIES_TOPIC_PARTITIONS: "{{ .Values.KAFKA_SERVICE_ENTITIES_TOPIC_PARTITIONS }}"
KAFKA_SERVICE_ENTITIES_TOPIC_REPLICATION_FACTOR: "{{ include "env.factor" (dict "ctx" . "def" .Values.KAFKA_SERVICE_ENTITIES_TOPIC_REPLICATION_FACTOR) }}"
KAFKA_SYSTEMS_EVENT_CONSUMER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_SYSTEMS_EVENT_CONSUMER_TOPIC_NAME "def" "systems_notification_topic") }}"
KEYCLOAK_AUTH_URL: "{{ .Values.KEYCLOAK_AUTH_URL }}"
KEYCLOAK_ENABLED: "{{ .Values.KEYCLOAK_ENABLED }}"
KEYCLOAK_REALM: "{{ .Values.KEYCLOAK_REALM }}"
LIQUIBASE_ENABLED: "{{ .Values.LIQUIBASE_ENABLED }}"
LIQUIBASE_LAUNCH_ENABLED: "{{ .Values.LIQUIBASE_LAUNCH_ENABLED }}"
LOCALE_RESOLVER: "{{ .Values.LOCALE_RESOLVER }}"
LOCK_DEFAULT_DURATION_SEC: "{{ .Values.LOCK_DEFAULT_DURATION_SEC }}"
LOCK_RETRY_PACE_SEC: "{{ .Values.LOCK_RETRY_PACE_SEC }}"
LOCK_RETRY_TIMEOUT_SEC: "{{ .Values.LOCK_RETRY_TIMEOUT_SEC }}"
LOG_GRAYLOG_HOST: "{{ .Values.GRAYLOG_HOST }}"
LOG_GRAYLOG_ON: "{{ .Values.GRAYLOG_ON }}"
LOG_GRAYLOG_PORT: "{{ .Values.GRAYLOG_PORT }}"
LOG_LEVEL: "{{ .Values.LOG_LEVEL }}"
MAIL_SENDER_ENABLE: "{{ .Values.MAIL_SENDER_ENABLE }}"
MAIL_SENDER_ENDPOINT: "{{ .Values.MAIL_SENDER_ENDPOINT }}"
MAIL_SENDER_PORT: "{{ .Values.MAIL_SENDER_PORT }}"
MAIL_SENDER_URL: "{{ .Values.MAIL_SENDER_URL }}"
MAX_FILE_SIZE: "{{ .Values.MAX_FILE_SIZE }}"
MAX_RAM: "{{ .Values.MAX_RAM }}"
MAX_REQUEST_SIZE: "{{ .Values.MAX_REQUEST_SIZE }}"
MICROSERVICE_NAME: "{{ .Values.SERVICE_NAME }}"
PROFILER_ENABLED: "{{ .Values.PROFILER_ENABLED }}"
PROJECT_INFO_ENDPOINT: "{{ .Values.PROJECT_INFO_ENDPOINT }}"
REMOTE_DUMP_HOST: "{{ .Values.REMOTE_DUMP_HOST }}"
REMOTE_DUMP_PORT: "{{ .Values.REMOTE_DUMP_PORT }}"
SERVICE_ENTITIES_MIGRATION_ENABLED: "{{ .Values.SERVICE_ENTITIES_MIGRATION_ENABLED }}"
SERVICE_REGISTRY_URL: "{{ .Values.SERVICE_REGISTRY_URL }}"
SPRING_PROFILES: "{{ .Values.SPRING_PROFILES }}"
SWAGGER_ENABLED: "{{ .Values.SWAGGER_ENABLED }}"
VAULT_ENABLE: "{{ .Values.VAULT_ENABLE }}"
VAULT_NAMESPACE: "{{ .Values.VAULT_NAMESPACE }}"
VAULT_ROLE_ID: "{{ .Values.VAULT_ROLE_ID }}"
VAULT_URI: "{{ .Values.VAULT_URI }}"
ZIPKIN_ENABLE: "{{ .Values.ZIPKIN_ENABLE }}"
ZIPKIN_PROBABILITY: "{{ .Values.ZIPKIN_PROBABILITY }}"
ZIPKIN_URL: "{{ .Values.ZIPKIN_URL }}"
{{- end }}

{{/* Sensitive data to be converted into secrets whenever possible */}}
{{- define "env.secrets" }}
EI_GRIDFS_PASSWORD: "{{ include "env.default" (dict "ctx" . "val" .Values.EI_GRIDFS_PASSWORD "def" "atp-ei-gridfs") }}"
EI_GRIDFS_USER: "{{ include "env.default" (dict "ctx" . "val" .Values.EI_GRIDFS_USER "def" "atp-ei-gridfs") }}"
TDM_DB_PASSWORD: "{{ include "env.default" (dict "ctx" . "val" .Values.TDM_DB_PASSWORD "def" .Values.SERVICE_NAME ) }}"
TDM_DB_USER: "{{ include "env.default" (dict "ctx" . "val" .Values.TDM_DB_USER "def" .Values.SERVICE_NAME ) }}"
KEYCLOAK_CLIENT_NAME: "{{ default "atp-tdm" .Values.KEYCLOAK_CLIENT_NAME }}"
KEYCLOAK_SECRET: "{{ default "10870611-a4a4-4ad1-acaa-b587f54ead40" .Values.KEYCLOAK_SECRET }}"
VAULT_SECRET_ID: "{{ default "" .Values.VAULT_SECRET_ID }}"
{{- end }}

{{- define "env.deploy" }}
ei_gridfs_pass: "{{ .Values.ei_gridfs_pass }}"
ei_gridfs_user: "{{ .Values.ei_gridfs_user }}"
pg_pass: "{{ .Values.pg_pass }}"
pg_user: "{{ .Values.pg_user }}"
PG_DB_ADDR: "{{ .Values.PG_DB_ADDR }}"
PG_DB_PORT: "{{ .Values.PG_DB_PORT }}"
SERVICE_NAME: "{{ .Values.SERVICE_NAME }}"
TDM_DB: "{{ include "env.default" (dict "ctx" . "val" .Values.TDM_DB "def" .Values.SERVICE_NAME ) }}"
{{- end }}