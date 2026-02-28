FROM eclipse-temurin:25-jre
WORKDIR /app
COPY build/libs/server-all.jar app.jar
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]