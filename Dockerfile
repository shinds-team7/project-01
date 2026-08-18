# petNow 운영 이미지.
#
# jar 를 이미지 안에서 다시 빌드하지 않고, 밖에서 만든 jar 를 그대로 복사한다.
# CD 워크플로가 S3 아티팩트 버킷에 올리는 jar 와 이미지 안의 jar 가
# 같은 파일이어야 롤백할 때 "그때 그 빌드"가 보장되기 때문이다.
#
# 로컬에서 직접 빌드할 때는 jar 를 먼저 만들어야 한다.
#   ./gradlew bootJar
#   docker build -t petnow:local .

FROM eclipse-temurin:17-jre-jammy

ARG JAR_FILE=build/libs/*.jar

WORKDIR /app

# root 로 돌리지 않는다.
RUN groupadd --system spring && useradd --system --gid spring spring

COPY ${JAR_FILE} app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

# 컨테이너에 준 메모리 기준으로 힙을 잡는다.
# t3.micro(1GB)에서 MariaDB 와 같이 뜨므로 힙을 과하게 잡으면 OOM 으로 죽는다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar"]
