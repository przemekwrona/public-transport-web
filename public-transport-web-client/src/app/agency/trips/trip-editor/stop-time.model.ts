export interface StopTimeModel {
    stopId?: number;
    stopName?: string;
    lon?: number;
    lat?: number;
    arrivalTime?: number;
    departureTime?: number;
    meters?: number;
    calculatedSeconds?: number;
    customizedMinutes?: number;
    bdot10k?: boolean;
}
