# Используем официальный образ Gradle для сборки
FROM gradle:7.6.1-jdk17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем только файлы, необходимые для загрузки зависимостей
COPY build.gradle.kts /app/build.gradle.kts
COPY settings.gradle.kts /app/settings.gradle.kts

# Копируем файлы, связанные с зависимостями (если есть)
COPY gradle /app/gradle
COPY gradlew /app/gradlew

# Загружаем зависимости (они будут закешированы в слое Docker)
# Даем права на выполнение скрипта gradlew
RUN chmod +x gradlew

# Скачиваем зависимости (без сборки проекта)
RUN ./gradlew dependencies --no-daemon --refresh-dependencies

# Копируем исходный код
COPY src src

# Собираем приложение
RUN ./gradlew build --no-daemon -x test

# Используем легкий образ для запуска приложения
FROM eclipse-temurin:17-jre-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR-файл из этапа сборки
COPY --from=build app/build/libs/MessageHandler-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт, на котором будет работать приложение
EXPOSE 8091

# Команда для запуска приложения
CMD ["java", "-jar", "app.jar"]