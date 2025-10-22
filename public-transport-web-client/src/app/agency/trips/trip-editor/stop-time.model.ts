export interface StopTimeModel {
    stopId?: number;
    stopName?: string;
    lon?: number;
    lat?: number;
    arrivalTime?: number;
    departureTime?: number;
    meters?: number;
    calculatedSeconds?: number;
    minutes?: number;
    bdot10k?: boolean;
}
