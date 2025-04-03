import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {CalendarPayload, Status} from "../../generated/public-transport";
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
}
