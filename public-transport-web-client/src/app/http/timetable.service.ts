import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export class Departure {
  line: string;
  headsign: string;
  date: Date;
  shapeId: string;
}

export class TimetableDeparture {
  line: string;
  headsign: string;
  date: string;
  departure: Date;
  shapeId: string;
  tripId: string;
}

export class Shape {
  shapeId: string;
  shapePtLat: number;
  shapePtLon: number;
  shapeDistTraveled: number;
  shapePtSequence: number;
}

export class Timetables {
  lines: string[];
  departures: Departure[];
  timetable: { [line: string]: { [date: string]: TimetableDeparture[]; } };
  shapes: { [shapeId: string]: Shape[]; };
}

@Injectable({
  providedIn: 'root'
})
export class TimetableService {

  constructor(private httpClient: HttpClient) {
  }

  getTimetable(stopId: string): Observable<Timetables> {
    return this.httpClient.get<Timetables>(`/api/agency/WAWA/stops/${stopId}/timetables`);
  }
}
