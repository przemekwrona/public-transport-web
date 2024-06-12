import {Component, Input, OnDestroy} from '@angular/core';
import * as L from 'leaflet';
import {Map, Polyline, PolyUtil} from "leaflet";
import {Itinerary, Leg, TripPoint} from "../../../../generated";

@Component({
  selector: 'app-itinerary-details',
  templateUrl: './itinerary-details.component.html',
  styleUrl: './itinerary-details.component.scss'
})
export class ItineraryDetailsComponent implements OnDestroy {

  @Input() itinerary: Itinerary;
  @Input() map: Map;

  public isWalk(leg: Leg): boolean {
    return leg.mode == 'WALK';
  }

  public isBus(leg: Leg): boolean {
    return leg.mode == 'BUS';
  }

  public isTram(leg: Leg): boolean {
    return leg.mode == 'TRAM';
  }

  public isSubway(leg: Leg): boolean {
    return leg.mode == 'SUBWAY';
  }

  public isRail(leg: Leg): boolean {
    return leg.mode == 'RAIL';
  }

  public isBicycle(leg: Leg): boolean {
    return leg.mode == 'BICYCLE';
  }

  public zoomPath(leg: Leg): void {
    this.map.fitBounds([
      [leg.from?.lat || 0, leg.from?.lon || 0],
      [leg.to?.lat || 0, leg.to?.lon || 0]
    ])
  }

  public zoomPoint(tripPoint: TripPoint | undefined): void {
    if(tripPoint == undefined) {
      return;
    }
    this.map.setView([tripPoint.lat || 0, tripPoint.lon || 0], 16);
  }

  public getRouteColor(leg: Leg) {
    return leg.routeColor == null ? '#2790FF' : `#${leg.routeColor}`;
  }

  ngOnDestroy(): void {
    console.log("Destroy");
  }

}
