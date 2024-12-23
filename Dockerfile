FROM eclipse-temurin:21-jre

ARG VERSION=0.0.0
ARG COMMMIT_ID=HEAD

LABEL maintainer="Aquerr & Brancu"
LABEL description="Wolfie discord bot, for TIS Arma group"
LABEL version="${VERSION}-${COMMMIT_ID}"

WORKDIR /opt/app

RUN groupadd -g 10001 wolfie && useradd -u 10000 -g wolfie wolfie && chown -R wolfie:wolfie /opt/app
USER wolfie:wolfie

COPY target/wolfie-0.0.1-SNAPSHOT.jar/ ./wolfie.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "/opt/app/wolfie.jar"]