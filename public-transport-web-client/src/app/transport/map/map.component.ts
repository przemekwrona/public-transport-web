import {AfterViewInit, Component} from '@angular/core';
import * as L from 'leaflet';
import {LatLng, LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline} from "leaflet";
import {Stop, StopService} from "../../http/stop.service";
import {Shape, Timetables, TimetableService} from "../../http/timetable.service";
import {TripService} from "../../http/trip.service";
import {DepartureMetadata} from "./timetable-content/timetable-content.component";
import {OtpPoint} from "./otp/otp.component";

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrl: './map.component.scss'
})
export class MapComponent implements AfterViewInit {
  private ZOOM: number = 16;

  private stopMarkers: Marker[] = [];
  private tripMarkers: Marker[] = [];
  private polyline: Polyline | null;

  public map: Map;

  public currentStop: Stop | null;
  public currentTimetables: Timetables | null;
  public currentLine: string | null;

  public startPoint: OtpPoint = new OtpPoint();
  public endPoint: OtpPoint = new OtpPoint();

  constructor(private stopService: StopService, private timetableService: TimetableService, private tripService: TripService) {
    this.getStops(52.23036781878572, 21.01080432114455);
  }

  ngAfterViewInit(): void {

    this.map = L.map('map', {
      center: [52.23210, 21.00585],
      zoom: this.ZOOM,
      zoomControl: false
    });

    L.control.zoom({
      position: 'bottomright'
    }).addTo(this.map);

    const tiles = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      minZoom: 3,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    });

    tiles.addTo(this.map);

    this.map.on('moveend', (event: LeafletEvent) => {
      if (this.map.getZoom() >= this.ZOOM) {
        this.getStops(this.map.getCenter().lat, this.map.getCenter().lng);
      }
    });

    this.map.on('zoomend', (event: LeafletEvent) => {
      if (this.map.getZoom() < this.ZOOM) {
        for (let marker of this.stopMarkers) {
          marker.removeFrom(this.map);
        }
        this.stopMarkers = [];
      }
    });

    this.map.on('click', (event: LeafletMouseEvent) => {

      // const o = event.

      if (this.startPoint.name == '') {
        this.startPoint = new OtpPoint();
        this.startPoint.name = `${event.latlng.lat.toFixed(5)},${event.latlng.lng.toFixed(5)}`;
        this.startPoint.lat = event.latlng.lat;
        this.startPoint.lon = event.latlng.lng;
      } else {
        this.endPoint = new OtpPoint();
        this.endPoint.name = `${event.latlng.lat.toFixed(5)},${event.latlng.lng.toFixed(5)}`;
        this.endPoint.lat = event.latlng.lat;
        this.endPoint.lon = event.latlng.lng;
      }

      if (this.currentLine == null) {
        this.currentStop = null;
        this.polyline?.removeFrom(this.map);
        this.polyline = null;
        this.tripMarkers.forEach(tripMaker => tripMaker.removeFrom(this.map));
        this.tripMarkers = [];
      }
      this.currentLine = null;
    });

  }

  getStops(latitude: number, longitude: number): void {
    this.stopService.getStops(latitude, longitude).subscribe(response => {
      const markers = [];

      for (let stop of response.stops) {
        const stopSubGroup = stop.stopId.slice(4);

        if (stopSubGroup.startsWith('m') && stopSubGroup != 'm') {
          continue;
        }

        const icon = L.divIcon({
          html: `<div style="background-color: #0096FF; padding: 1px 2px 6px 3px; width: 19px; height: 18px; border-radius: 2px; color: whitesmoke">${stopSubGroup}</div>`,
          className: 'stop-marker'
        })
        let stopMarker = L.marker([stop.stopLat, stop.stopLon], {icon: icon});

        if (stopSubGroup.startsWith('m')) {
          const iconMetro = L.icon({iconUrl: 'assets/warsaw_metro.png', iconSize: [25, 25]});
          stopMarker = L.marker([stop.stopLat, stop.stopLon], {icon: iconMetro});
        }

        stopMarker.addTo(this.map).on('click', (e) => {
          this.currentStop = stop;
          this.currentLine = null;

          if (this.startPoint.name == '') {
            this.startPoint = new OtpPoint();
            this.startPoint.name = stop.stopName;
            this.startPoint.lat = stop.stopLat;
            this.startPoint.lon = stop.stopLon;
          } else {
            this.endPoint = new OtpPoint();
            this.endPoint.name = stop.stopName;
            this.endPoint.lat = stop.stopLat;
            this.endPoint.lon = stop.stopLon;
          }


          this.timetableService.getTimetable(stop.stopId).subscribe(timetables => this.currentTimetables = timetables);
        });

        markers.push(stopMarker);
      }

      const removeMarkers = this.stopMarkers;
      this.stopMarkers = markers;
      for (let marker of removeMarkers) {
        marker.removeFrom(this.map);
      }
    });
  }

  onClickLine(line: string) {
    this.currentLine = line;
  }

  hasCurrentStop(): boolean {
    return this.currentStop != null;
  }

  hasCurrentLine(): boolean {
    return this.hasCurrentStop() && this.currentLine != null;
  }

  public showTrip(metadata: DepartureMetadata) {

    this.stopMarkers.forEach(stopMarker => stopMarker.removeFrom(this.map));

    this.tripService.getTrip(metadata.tripId).subscribe(response => {
      const fontAwesomeIcon = L.divIcon({
        html: '<i class="fa fa-circle-o " style="background-color: white; border-radius: 2em;"></i>',
        className: 'stop-circle'
      });

      const markers = [];
      for (let tripStop of response.tripStops) {
        const marker = L.marker([tripStop.stopLat, tripStop.stopLon], {icon: fontAwesomeIcon});
        marker.on('click', (e) => {
          const stop = new Stop();
          stop.id = tripStop.stopId;
          stop.stopId = tripStop.stopId;
          stop.stopName = tripStop.stopName;
          stop.stopLat = tripStop.stopLat;
          stop.stopLon = tripStop.stopLon;

          this.flyTo(stop.stopLat, stop.stopLon);

          this.currentStop = stop;
          this.timetableService.getTimetable(stop.stopId).subscribe(timetables => this.currentTimetables = timetables);
        });
        marker.addTo(this.map);
        const tooltip = marker.bindTooltip(`<div>${tripStop.stopName}</div>`, {
          direction: 'right',
          permanent: true,
          sticky: false,
          className: 'leaflet-tooltip-igeolab'
        });
        tooltip.on('click', function (e) {
          console.log("clicked", JSON.stringify(e.target.getLatLng()));
        });
        tooltip.openTooltip();
        markers.push(marker);
      }
      this.tripMarkers.forEach(marker => marker.removeFrom(this.map));
      this.tripMarkers = markers;
    });

    this.flyTo(this.currentStop?.stopLat || null, this.currentStop?.stopLon || null);

    const shape: Shape[] = this.currentTimetables?.shapes[metadata.shapeId] || [];
    const points: LatLng[] = shape
      .sort((prev: Shape, curr: Shape) => prev.shapePtSequence - curr.shapePtSequence)
      .map(point => new L.LatLng(point.shapePtLat, point.shapePtLon));

    const polyline = new L.Polyline(points, {
      color: '#0074c1',
      smoothFactor: 1,
      weight: 8,
      opacity: 0.9
    });

    if (this.polyline != null) {
      polyline.addTo(this.map);
      this.polyline.removeFrom(this.map);
      this.polyline = polyline;
    } else {
      polyline.addTo(this.map);
      this.polyline = polyline;
    }
  }

  public flyTo(lat: number | null, lng: number | null) {
    if (lat != null && lng != null) {
      this.map.flyTo([lat, lng - 0.01], 14);
    }
  }

  public clearTripStopMarkers() {
    this.tripMarkers.forEach(marker => marker.removeFrom(this.map));
    this.tripMarkers = [];
  }
}
