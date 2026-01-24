import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {DivIcon, LatLngBounds, Map, Marker, Polyline} from "leaflet";
import * as L from "leaflet";
import moment from "moment";
import {Itinerary, Leg} from "../../../../generated/public-transport-planner-api";

@Component({
    selector: 'app-itinerary-header',
    templateUrl: './itinerary-header.component.html',
    styleUrl: './itinerary-header.component.scss',
    standalone: false
})
export class ItineraryHeaderComponent implements OnInit, OnDestroy {

  private STOP_CIRCLE: DivIcon = L.divIcon({
    html: '<i class="fa fa-circle-o " style="background-color: white; border-radius: 2em;"></i>',
    className: 'stop-circle'
  });

  @Input() itinerary: Itinerary;
  @Input() map: Map;
  @Input() itineraryCount: number = 0;

  public isOpen: boolean = false;

  private polyline: Polyline[] = [];
  private intermediateStops: Marker[] = [];
  private firstAndLatStops: Marker[] = [];

  ngOnInit(): void {
    if (this.itineraryCount == 1) {
      this.zoomToItinerary();
    }
  }

  public getLegsWithoutWalk(): Leg[] {
    return (this.itinerary?.legs || []).filter(leg => !this.isWalk(leg));
  }

  public getTotalTime(): number {
    if (this.itinerary == null || this.itinerary.duration == null) {
      return 0;
    }
    return this.itinerary?.duration / 60
  }

  public getFirst(): Leg {
    return (this.itinerary?.legs || [])[0];
  }

  public getFirstNonWalk(): Leg {
    return this.getLegsWithoutWalk()[0];
  }

  public getLast(): Leg {
    const legs: Leg[] = this.itinerary?.legs || [];
    return legs[legs.length - 1];
  }

  public startTime(): number {
    const firstLeg = (this.itinerary?.legs || [])[0];
    return moment(moment(new Date(Number(firstLeg.startTime)))).diff(moment(), 'minutes');
  }

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

  public zoomToItineraryIfOpen(): void {
    if (this.isOpen) {
      this.zoomToItinerary();
    }
  }

  public zoomToItinerary(): void {
    const bounds: LatLngBounds = this.polyline
      .map(poly => poly.getBounds())
      .reduce((prev: LatLngBounds, curr: LatLngBounds) => prev.extend(curr));

    this.map.fitBounds(bounds);
  }

  ngOnDestroy(): void {
    this.polyline.forEach(polyline => polyline.removeFrom(this.map));
    this.polyline = [];

    this.intermediateStops.forEach(stop => stop.removeFrom(this.map));
    this.intermediateStops = [];

    this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));
    this.firstAndLatStops = [];
  }

  public getRouteColor(leg: Leg) {
    return `#${leg.routeColor}`;
  }
}
