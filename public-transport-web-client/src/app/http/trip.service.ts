import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

export class SimpleTripResponse {
  tripStops: TripStop[];
}

export class TripStop {
  stopId: string;
  stopName: string;
  stopLat: number;
  stopLon: number;
  stopSequence: number;
  departure: moment.Moment;
  tripId: string;
  shapeId: string;
  lines: string[];
  types: number[];
}

@Injectable({
  providedIn: 'root'
})
export class TripService {

  constructor(private httpClient: HttpClient) {
  }
}
