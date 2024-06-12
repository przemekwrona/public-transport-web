import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import * as L from 'leaflet';
import {DivIcon, LatLng, LatLngBounds, Map, Marker, Polyline, PolyUtil} from "leaflet";
import {Itinerary, Leg, TripPoint} from "../../../../../../generated";
import moment from "moment";
import {decode, encode} from "@googlemaps/polyline-codec";
import {first, last} from "lodash";

import {Stop} from "../../../../http/stop.service";


@Component({
  selector: 'app-itinerary',
  templateUrl: './itinerary.component.html',
  styleUrl: './itinerary.component.scss'
})
export class ItineraryComponent implements OnInit, OnDestroy {

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
      this.showItineraryOnMap();
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
    return moment((this.itinerary?.legs || [])[0].startTime).diff(moment(), 'minutes');
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

  public showItineraryOnMap(): void {
    this.polyline.forEach(polyline => polyline.removeFrom(this.map));
    this.polyline = (this.itinerary.legs || [])
      .map(leg => {
        let config = {};

        if (leg.mode == 'WALK') {
          config = {
            weight: 8,
            color: "#181818",
            dashArray: "1 15"
          }
        }

        if (leg.mode == 'BUS') {
          config = {
            weight: 7,
            color: "#2D9FFD"
          }
        }

        if (leg.mode == 'TRAM') {
          config = {
            weight: 7,
            color: "#631F18"
          }
        }

        return L.polyline(decode(leg.legGeometry?.points || '', 5), config)
      })
    this.polyline.forEach(polyline => polyline.addTo(this.map));

    this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));
    this.firstAndLatStops = (this.itinerary.legs || [])
      .map(leg => [leg?.from, leg?.to])
      .flat()
      .map(stop => {
        const marker = L.marker([stop?.lat || 0, stop?.lon || 0], {icon: this.STOP_CIRCLE});
        const tooltip = marker.bindTooltip(`<div>${stop?.name || ''}</div>`, {
          direction: 'right',
          permanent: true,
          sticky: false,
          className: 'leaflet-tooltip-igeolab'
        });
        return marker;
      });
    this.firstAndLatStops.forEach(stop => stop.addTo(this.map));

    this.intermediateStops.forEach(stop => stop.removeFrom(this.map));

    const stopCount = (this.itinerary.legs || [])
      .map(leg => (leg.intermediateStops || []))
      .flat().length;

    this.intermediateStops = (this.itinerary.legs || [])
      .map(leg => (leg.intermediateStops || []))
      .flat()
      .map(stop => {
        const marker = L.marker([stop?.lat || 0, stop?.lon || 0], {icon: this.STOP_CIRCLE});

        if (stopCount < 15) {
          marker.bindTooltip(`<div>${stop.name}</div>`, {
            direction: 'right',
            permanent: true,
            sticky: false,
            className: 'leaflet-tooltip-igeolab'
          });
        }

        return marker;
      });
    this.intermediateStops.forEach(stop => stop.addTo(this.map));
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

  public hideItineraryOnMap(): void {
    if (this.isOpen) {

    } else {
      this.polyline.forEach(polyline => polyline.removeFrom(this.map));
      this.polyline = [];

      this.intermediateStops.forEach(stop => stop.removeFrom(this.map));
      this.intermediateStops = [];

      this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));
      this.firstAndLatStops = [];
    }
  }

  ngOnDestroy(): void {
    this.polyline.forEach(polyline => polyline.removeFrom(this.map));
    this.polyline = [];

    this.intermediateStops.forEach(stop => stop.removeFrom(this.map));
    this.intermediateStops = [];

    this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));
    this.firstAndLatStops = [];
  }

}
