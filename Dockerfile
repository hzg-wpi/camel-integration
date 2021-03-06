FROM adoptopenjdk/openjdk11:alpine-jre

#TODO build libpniio

RUN apk add dumb-init
MAINTAINER igor.khokhriakov@hzg.de

ARG JAR_FILE
ADD target/${JAR_FILE} /app/bin/camel.jar

RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
RUN chown -R javauser /app

USER javauser

WORKDIR /app

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD java -jar -server -DTANGO_HOST=$TANGO_HOST /app/bin/camel.jar dev