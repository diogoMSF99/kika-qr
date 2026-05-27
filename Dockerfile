FROM maven:3.9-eclipse-temurin-22

WORKDIR /app

COPY . .

RUN mvn clean package

CMD ["java", "-jar", "target/kika_qr-1.0-SNAPSHOT.jar"]