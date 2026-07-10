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

    public getBrigadeByBrigadeName(brigadeName: string): Observable<BrigadeBody> {
        return this.httpClient.post<BrigadeBody>(`/api/v1/brigades/details`, { brigadeName: brigadeName });
    }

}
