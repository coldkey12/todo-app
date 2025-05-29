FROM openjdk:21
EXPOSE 8080
ADD target/todo-app-0.0.1-SNAPSHOT.jar todo-app.jar
ENTRYPOINT ["java", "-jar", "todo-app.jar"]