FROM openjdk:11
ARG JAR_FILE=build/libs/Key-Manager-0.1-all.jar
COPY ${JAR_FILE} app.jar
CMD ["java","-jar","app.jar"]
EXPOSE 50051