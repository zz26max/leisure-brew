# ---- 构建阶段 ----
FROM maven:3.8-openjdk-8 AS builder

WORKDIR /app

# 使用阿里云 Maven 镜像加速
RUN mkdir -p /root/.m2 && \
    echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><name>Aliyun Maven</name><url>https://maven.aliyun.com/repository/central</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 先复制 pom.xml，利用 Docker 缓存加速依赖下载
COPY pom.xml .
COPY sky-common/pom.xml sky-common/
COPY sky-pojo/pom.xml sky-pojo/
COPY sky-server/pom.xml sky-server/
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY . .
RUN mvn clean package -DskipTests -B

# ---- 运行阶段 ----
FROM openjdk:8-jre-slim

WORKDIR /app
COPY --from=builder /app/sky-server/target/sky-server-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
