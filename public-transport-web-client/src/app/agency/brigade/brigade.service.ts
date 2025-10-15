import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {
    BrigadeBody,
    BrigadePatchBody,
    GetAllTripsResponse,
    GetBrigadeResponse,
    Status
} from "../../generated/public-transport-api";

@Injectable({
    providedIn: 'root'
})
export class BrigadeService {

    constructor(private httpClient: HttpClient) {
    }

    public getRoutes(lineOrName: string): Observable<GetAllTripsResponse> {
        let params = new HttpParams();
        params = params.set('filter', lineOrName);

        return this.httpClient.get<GetAllTripsResponse>(`/api/v1/trips`, {params: params});
    }

    public getAllBrigades(): Observable<GetBrigadeResponse> {
        return this.httpClient.get<GetBrigadeResponse>(`/api/v1/brigades`);
    }

    public getBrigadeByBrigadeName(brigadeName: string): Observable<BrigadeBody> {
        return this.httpClient.post<BrigadeBody>(`/api/v1/brigades/details`, { brigadeName: brigadeName });
    }

    public saveBrigade(brigadeBody: BrigadeBody): Observable<Status> {
        return this.httpClient.post<Status>(`/api/v1/brigades`, brigadeBody);
    }

    public putBrigade(brigadePatchBody: BrigadePatchBody): Observable<Status> {
        return this.httpClient.put<Status>(`/api/v1/brigades`, brigadePatchBody);
    }
}
