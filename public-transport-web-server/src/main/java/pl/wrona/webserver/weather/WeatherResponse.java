package pl.wrona.webserver.weather;

public record WeatherResponse(Location location, Current current) {
}
