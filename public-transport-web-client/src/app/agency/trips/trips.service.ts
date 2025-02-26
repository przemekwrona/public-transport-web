import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {
    Route,
    Trips,
    Trip,
    Status,
    TripMode,
    TripsDetails,
    UpdateTripDetailsRequest
} from "../../generated/public-transport";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class TripsService {

    constructor(private httpClient: HttpClient) {
    }

    public create(trips: UpdateTripDetailsRequest): Observable<Trip> {
        return this.httpClient.post<Trip>(`/api/v1/trips`, trips);
    }

    public update(trips: UpdateTripDetailsRequest): Observable<Status> {
        return this.httpClient.put<Status>(`/api/v1/trips`, trips);
    }

    public delete(line: string, name: string, variant: string, mode: TripMode): Observable<Status> {
        const payload = {line: line, name: name, variant: variant, mode: mode};
        return this.httpClient.delete<Status>(`/api/v1/trips`, {body: payload});
    }

    public getTrips(line: string, name: string): Observable<Trips> {
        return this.httpClient.post<Trips>(`/api/v1/trips/details`, {name: name, line: line});
    }

    public getTripsByVariant(line: string, name: string, variant: string, mode: TripMode): Observable<TripsDetails> {
        return this.httpClient.post<TripsDetails>(`/api/v1/trips/variant-details`, {name: name, line: line, variant: variant, mode: mode});
    }

    public measureDistance(trips: Trip): Observable<Trip> {
        return this.httpClient.post<Trip>(`/api/v1/trips/measure-distance`, trips);
    }
}
