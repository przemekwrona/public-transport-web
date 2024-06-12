import {Component, Input} from '@angular/core';
import * as L from 'leaflet';
import {DivIcon, LatLngBounds, Map, Marker, Polyline} from "leaflet";
import {OtpService} from "../../../http/otp.service";
import moment from "moment";
import {Itinerary, RoutesResponse} from "../../../../../generated";
import {Leg} from "../../../../../generated/models/Leg";
import {decode} from "@googlemaps/polyline-codec";

export class OtpPoint {
  name: string = '';
  lat: number;
  lon: number;
}

@Component({
  selector: 'app-otp',
  templateUrl: './otp.component.html',
  styleUrl: './otp.component.scss'
})
export class OtpComponent {
  private STOP_CIRCLE: DivIcon = L.divIcon({
    html: '<i class="fa fa-circle-o " style="background-color: white; border-radius: 2em;"></i>',
    className: 'stop-circle'
  });

  @Input() startPoint: OtpPoint;
  @Input() map: Map;

  private _endPoint: OtpPoint;

  @Input() set endPoint(endPoint: OtpPoint) {
    this._endPoint = endPoint;
    if (this.wasPlanned) {
      this.plan();
    }
  }

  get endPoint(): OtpPoint {
    return this._endPoint;
  }

  public routeResponse: RoutesResponse | null;
  public panelOpenState: boolean = false;

  private wasPlanned: boolean = false;

  private polyline: Polyline[] = [];
  private intermediateStops: Marker[] = [];
  private firstAndLatStops: Marker[] = [];
  public isExpanded: boolean = true;

  constructor(private otpService: OtpService) {
  }

  public plan(): void {
    this.wasPlanned = true;
    this.otpService.plan(this.startPoint.lat, this.startPoint.lon, this.endPoint.lat, this.endPoint.lon, moment()).subscribe(routeResponse => this.routeResponse = routeResponse);
  }

  public hasPlan(): boolean {
    return this.routeResponse != null;
  }

  public isItinerary(itinerary: Itinerary): boolean {
    return (itinerary.legs || []).length > 1;
  }

  public isWalk(itinerary: Itinerary): boolean {
    return (itinerary.legs || []).length == 1 && (itinerary.legs || [])[0]?.mode == 'WALK';
  }

  public scrollTop(): void {
  }

  public expand(): void {
    this.isExpanded = true;

  }

  public collapse(): void {
    this.isExpanded = false;
  }

  public showItineraryOnMap(itinerary: Itinerary): void {
    this.polyline.forEach(polyline => polyline.removeFrom(this.map));
    this.polyline = (itinerary.legs || [])
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
    this.firstAndLatStops = (itinerary.legs || [])
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

    const stopCount = (itinerary.legs || [])
      .map(leg => (leg.intermediateStops || []))
      .flat().length;

    this.intermediateStops = (itinerary.legs || [])
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

  public hideItineraryOnMap(): void {
    if (!this.isExpanded) {
      this.polyline.forEach(polyline => polyline.removeFrom(this.map));
      this.polyline = [];

      this.intermediateStops.forEach(stop => stop.removeFrom(this.map));
      this.intermediateStops = [];

      this.firstAndLatStops.forEach(stop => stop.removeFrom(this.map));
      this.firstAndLatStops = [];
    }
  }

  public zoomToItinerary(): void {
    const bounds: LatLngBounds = this.polyline
      .map(poly => poly.getBounds())
      .reduce((prev: LatLngBounds, curr: LatLngBounds) => prev.extend(curr));

    this.map.fitBounds(bounds);
  }

}
