FROM eclipse-temurin:21-jre

ARG VERSION=0.0.0
ARG COMMMIT_ID=HEAD

LABEL maintainer="Aquerr & Brancu"
LABEL description="Wolfie discord bot, for TIS Arma group"
LABEL version="${VERSION}-${COMMMIT_ID}"

WORKDIR /opt/app

COPY target/wolfie-0.0.1-SNAPSHOT.jar ./wolfie.jar

RUN groupadd wolfie  \
    && useradd --system -g wolfie wolfie

RUN mkdir /opt/app/config  \
    && mkdir /opt/app/data  \
    && mkdir /opt/app/logs  \
    && chown -R wolfie:wolfie /opt/app \
    && chmod -R 755 /opt/app

USER wolfie

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/opt/app/wolfie.jar"]