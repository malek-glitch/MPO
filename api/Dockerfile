FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

# Debian-based JRE: scrimage-webp ships glibc cwebp binaries (Alpine won't work)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/install/api /app
EXPOSE 8080
CMD ["/app/bin/api"]
