# 第一階段：編譯階段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. 緩存依賴
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. 複製原始碼
COPY src ./src

# 3. 執行打包
RUN mvn clean package -DskipTests

# 第二階段：運行階段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 修正時區
RUN apk add --no-cache tzdata
ENV TZ=UTC

# 4. 複製編譯好的 jar 
# 改為 jar 後，檔名通常會是 sts_portfolio-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]