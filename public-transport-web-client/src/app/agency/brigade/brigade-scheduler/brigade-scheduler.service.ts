import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {DayPilot} from "@daypilot/daypilot-lite-angular";

@Injectable()
export class BrigadeSchedulerService {

    resources: any[] = [
        { name: "Brygada 1", id: "GA", expanded: true},
        { name: "Brygada 2", id: "GB", expanded: true}
    ];

    events: any[] = [
        {
            id: "1",
            resource: "GA",
            start: "2026-07-23T09:00:00",
            end: "2026-07-23T11:00:00",
            text: "Scheduler Event 1",
            color: "#e69138"
        },
        {
            id: "2",
            resource: "GB",
            start: "2026-07-23T04:00:00",
            end: "2026-07-23T09:15:00",
            text: "Scheduler Event 2",
            color: "#6aa84f"
        },
        {
            id: "3",
            resource: "GB",
            start: "2026-07-23T01:00:00",
            end: "2026-07-23T02:00:00",
            text: "Scheduler Event 3",
            color: "#3c78d8"
        }
    ];

    constructor(private http: HttpClient) {
    }

    getEvents(from: DayPilot.Date, to: DayPilot.Date): Observable<any[]> {

        // simulating an HTTP request
        return new Observable(observer => {
            setTimeout(() => {
                observer.next(this.events);
            }, 200);
        });

        // return this.http.get("/api/events?from=" + from.toString() + "&to=" + to.toString());
    }

    getResources(): Observable<any[]> {

        // simulating an HTTP request
        return new Observable(observer => {
            setTimeout(() => {
                observer.next(this.resources);
            }, 200);
        });

        // return this.http.get("/api/resources");
    }
}
