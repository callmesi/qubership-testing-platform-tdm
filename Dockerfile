FROM artifactory-service-address/path-to-java-image

LABEL maintainer="our-team@some-domain"
LABEL atp.service="atp-tdm"

ENV HOME_EX=/atp-tdm
ENV TDM_DB_USER=tdmadmin
ENV TDM_DB_PASSWORD=tdmadmin
ENV JDBC_URL=jdbc:postgresql://localhost:5432/atptdm

WORKDIR $HOME_EX

COPY --chmod=775 dist/atp /atp/
COPY --chown=atp:root build/atp-tdm $HOME_EX/

RUN apk add --update --no-cache fontconfig ttf-dejavu && \
    rm -rf /var/cache/apk/* && \
    find $HOME_EX -type f -name '*.sh' -exec chmod a+x {} + && \
    find $HOME_EX -type d -exec chmod 777 {} \;

EXPOSE 8080 9000

USER atp

CMD ["./run.sh"]
