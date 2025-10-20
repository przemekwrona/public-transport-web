export interface StopTimeModel {
    stopId?: number;
    stopName?: string;
    lon?: number;
    lat?: number;
    arrivalTime?: number;
    departureTime?: number;
    meters?: number;
    seconds?: number;
    minutes?: number;
    bdot10k?: boolean;
}
