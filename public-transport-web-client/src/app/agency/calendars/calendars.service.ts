import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {CalendarPayload, GetCalendarsResponse, Status} from "../../generated/public-transport";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class CalendarsService {

    constructor(private httpClient: HttpClient) {
    }

    public createCalendar(payload: CalendarPayload): Observable<Status> {
        return this.httpClient.post<Status>(`/api/v1/calendar`, payload);
    }

    public getAllCalendars(): Observable<GetCalendarsResponse> {
        return this.httpClient.get<GetCalendarsResponse>(`/api/v1/calendar`);
    }
}
