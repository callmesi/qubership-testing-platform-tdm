#!/usr/bin/env sh

if [ ! -f ./atp-common-scripts/openshift/common.sh ]; then
  echo "ERROR: Cannot locate ./atp-common-scripts/openshift/common.sh"
  exit 1
fi

. ./atp-common-scripts/openshift/common.sh

_ns="${NAMESPACE}"
TDM_DB="$(env_default "${TDM_DB}" "${SERVICE_NAME}" "${_ns}")"
TDM_DB_USER="$(env_default "${TDM_DB_USER}" "${SERVICE_NAME}" "${_ns}")"
TDM_DB_PASSWORD="$(env_default "${TDM_DB_PASSWORD}" "${SERVICE_NAME}" "${_ns}")"
EI_GRIDFS_DB="$(env_default "${EI_GRIDFS_DB}" "atp-ei-gridfs" "${_ns}")"
EI_GRIDFS_USER="$(env_default "${EI_GRIDFS_USER}" "atp-ei-gridfs" "${_ns}")"
EI_GRIDFS_PASSWORD="$(env_default "${EI_GRIDFS_PASSWORD}" "atp-ei-gridfs" "${_ns}")"

echo "***** Initializing databases ******"
PG_EXTENSIONS="pgcrypto"
init_pg "${PG_DB_ADDR}" "${TDM_DB}" "${TDM_DB_USER}" "${TDM_DB_PASSWORD}" "${PG_DB_PORT}" "${pg_user}" "${pg_pass}"
init_mongo "${EI_GRIDFS_DB_ADDR:-$GRIDFS_DB_ADDR}" "${EI_GRIDFS_DB}" "${EI_GRIDFS_USER}" "${EI_GRIDFS_PASSWORD}" "${EI_GRIDFS_DB_PORT:-$GRIDFS_DB_PORT}" "${ei_gridfs_user}" "${ei_gridfs_pass}"

echo "***** Setting up encryption *****"
encrypt "${ENCRYPT}" "${SERVICE_NAME}"
