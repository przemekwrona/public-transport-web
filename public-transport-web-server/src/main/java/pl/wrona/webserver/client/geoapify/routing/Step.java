package pl.wrona.webserver.client.geoapify.routing;

public record Step(
        int from_index,
        int to_index,
        double distance,
        double time,
        Instruction instruction
) {}
