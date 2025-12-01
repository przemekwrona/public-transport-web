import {AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from "leaflet";
import {LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline, Popup} from "leaflet";
import {find, findIndex, last, size} from "lodash";
import {
    ErrorResponse,
    Point2D,
    RouteDetails, RouteId,
    Stop, StopsService,
    StopTime,
    TrafficMode,
    TripDistanceMeasuresService,
    TripId, TripMeasure,
    TripMode,
    TripsDetails,
    TripService,
    UpdateTripDetailsRequest
} from "../../../generated/public-transport-api";
import {ActivatedRoute, Data, Router} from "@angular/router";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {debounceTime, map, Observable, Subject} from "rxjs";
import {TripEditorComponentMode} from "./trip-editor-component-mode";
import {ViewportScroller} from "@angular/common";
import {BusStopSelectorData} from "../../shared/bus-stop-selector/bus-stop-selector.component";
import {MatDialog} from "@angular/material/dialog";
import {
    BusStopModalEditorComponent,
    BusStopModalEditorData
} from "../../shared/bus-stop-modal-editor/bus-stop-modal-editor.component";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {StopTimeModel} from "./stop-time.model";
import {NotificationService} from "../../../shared/notification.service";
import {HttpErrorResponse} from "@angular/common/http";
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {TripIdExistenceValidator} from "./trip-id-existence/trip-id-existence.service";

@Component({
    selector: 'app-trip-editor',
    templateUrl: './trip-editor.component.html',
    styleUrl: './trip-editor.component.scss',
    animations: [
        trigger('simpleFadeAnimation', [
            state('in', style({opacity: 1})),
            transition(':enter', [style({opacity: 0}), animate(500)]),
            transition(':leave', animate(500, style({opacity: 0})))
        ]),
        trigger('heightCollapse', [
            // 1. Define the 'collapsed' state (the target style is 0 height)
            state('collapsed', style({
                height: '0',
                opacity: '0',
                paddingTop: '0',
                paddingBottom: '0'
            })),

            // 2. Define the 'expanded' state (the target style is the auto height)
            state('expanded', style({
                height: '*', // <-- The key: '*' calculates the content's natural height
                opacity: '1',
            })),

            // 3. Define the transition timing
            transition('collapsed <=> expanded', [
                animate('400ms ease-in-out') // 400 milliseconds transition time
            ])
        ])
    ]
})
export class TripEditorComponent implements OnInit, AfterViewInit {
    private BDOT10K_STOP = L.divIcon({
        html: `<div style="background-color: #0096FF; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    private OTP_STOP = L.divIcon({
        html: `<div style="background-color: #00395c; padding: 1px 0 0 1px; width: 20px; height: 20px; border-radius: 2px; color: whitesmoke"><img src="assets/bus-solid.svg"></div>`,
        className: 'stop-marker'
    });

    private map: Map;
    private stopMarkers: Marker[] = [];
    private routePolyline: Polyline;
    private popup: Popup;

    private communicationVelocitySubject = new Subject<number>();
    private previousVariantName = '';

    public state: { name: string, line: string, variant: string, mode: TripMode, trafficMode: TrafficMode };

    public tripModeSelectValue = TripMode;
    public tripEditorComponentMode: TripEditorComponentMode;

    public trafficModeSelectValue = TrafficMode

    public $tripDetails: TripsDetails = {};
    public $tripVariants: RouteDetails = {};

    public stopTimes: StopTimeModel[] = [];

    public forceRefreshSubject: Subject<boolean> = new Subject();
    public forceRefresh$: Observable<boolean> = this.forceRefreshSubject.asObservable();

    public isRefreshExpanded: boolean = false;
    public isRefreshingExpanded: boolean = false;

    public modelForm: FormGroup;

    constructor(private stopsService: StopsService, private tripService: TripService, private tripDistanceMeasuresService: TripDistanceMeasuresService, private agencyStorageService: AgencyStorageService, private router: Router, private _route: ActivatedRoute, private _viewportScroller: ViewportScroller, private dialog: MatDialog, private notificationService: NotificationService, private formBuilder: FormBuilder, private tripIdExistenceValidator: TripIdExistenceValidator) {
        this.communicationVelocitySubject.pipe(debounceTime(1000)).subscribe(() => this.approximateDistance());
    }

    ngOnInit(): void {
        this._route.data.subscribe((data: Data) => this.tripEditorComponentMode = data['mode']);

        this._route.queryParams.subscribe(params => this.state = params as {
            line: string,
            name: string,
            variant: string,
            mode: TripMode,
            trafficMode: TrafficMode
        });

        this.modelForm = this.formBuilder.group({
            isMainVariant: [true, [Validators.required]],
            tripVariantName: [this.state.variant, [Validators.required]],
            tripVariantMode: [this.state.mode, [Validators.required]],
            tripTrafficMode: [this.state.trafficMode, [Validators.required]],

            variantDesignation: ['', [Validators.required]],
            variantDescription: ['', [Validators.required]],

            origin: ['', [Validators.required]],
            destination: ['', [Validators.required]],
            headsign: ['', [Validators.required]],

            calculatedCommunicationVelocity: [null, [Validators.required, Validators.min(0)]],

            isCustomized: [false, [Validators.required]],
            stops: this.formBuilder.array([], [Validators.required, Validators.minLength(2)])
        }, {
            // asyncValidators: this.tripIdExistenceValidator.variantExistsValidator(this.state.line, this.state.name, 'tripVariantName', 'tripVariantMode', 'tripTrafficMode')
        });

        this.modelForm.get('calculatedCommunicationVelocity')!.valueChanges.subscribe((value: number) => this.onCommunicationVelocityChange(value));

        this._route.data.pipe(map((data: Data) => data['trip'])).subscribe((tripDetails: TripsDetails) => {
            this.$tripDetails = tripDetails;
            this.modelForm.controls['isMainVariant'].setValue(tripDetails.isMainVariant);
            this.modelForm.controls['isCustomized'].setValue(tripDetails.isCustomized);
            this.modelForm.controls['calculatedCommunicationVelocity'].setValue(tripDetails.calculatedCommunicationVelocity);

            this.modelForm.controls['variantDesignation'].setValue(tripDetails.variantDesignation);
            this.modelForm.controls['variantDescription'].setValue(tripDetails.variantDescription);

            this.modelForm.controls['origin'].setValue(tripDetails.originStopName);
            this.modelForm.controls['destination'].setValue(tripDetails.destinationStopName);
            this.modelForm.controls['headsign'].setValue(tripDetails.headsign);

            const stopArrayControl: FormArray = this.modelForm.get('stops') as FormArray;
            (this.$tripDetails.stops || []).forEach((stopTime: StopTime) => {
                const stopTimeModel: StopTimeModel = {} as StopTimeModel;
                stopTimeModel.stopId = stopTime.stopId;
                stopTimeModel.stopName = stopTime.stopName;
                stopTimeModel.lon = stopTime.lon;
                stopTimeModel.lat = stopTime.lat;
                stopTimeModel.meters = stopTime.meters;
                stopTimeModel.calculatedSeconds = stopTime.calculatedSeconds;
                stopTimeModel.customizedMinutes = stopTime.customizedSeconds / 60;
                stopTimeModel.bdot10k = stopTime.bdot10k;

                const customizedMinutesControl: FormControl<number> = this.formBuilder.control(stopTime.customizedSeconds / 60, [Validators.required]);
                stopTimeModel.customizedMinutesControl = customizedMinutesControl
                this.stopTimes.push(stopTimeModel);

                stopArrayControl.push(stopTimeModel.customizedMinutesControl);
            });

            this.stopTimes.forEach((stopTime: StopTimeModel, index: number): void => {
                stopTime.customizedMinutesControl.valueChanges.subscribe((value: number) => {
                    this.onChangeDeparture(stopTime, index, value);
                });
            });

            this._route.data.pipe(map((data: Data) => data['variants'])).subscribe((tripVariants: RouteDetails) => {
                this.$tripVariants = tripVariants;

                if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
                    if (this.$tripVariants?.trips.length || 0 == 0) {
                        this.$tripDetails.isMainVariant = true;
                        this.$tripDetails.tripId.variantName = "MAIN";
                        this.$tripDetails.tripId.variantMode = TripMode.Front;
                        this.$tripDetails.tripId.trafficMode = TrafficMode.Normal;
                        this.$tripDetails.originStopName = tripVariants.route.originStop.name;
                        this.$tripDetails.destinationStopName = tripVariants.route.destinationStop.name;
                        this.$tripDetails.headsign = tripVariants.route.destinationStop.name;

                        const stopTimeModel: StopTimeModel = {} as StopTimeModel;
                        stopTimeModel.stopId = tripVariants.route.originStop.id;
                        stopTimeModel.stopName = tripVariants.route.originStop.name;
                        stopTimeModel.lon = tripVariants.route.originStop.lon;
                        stopTimeModel.lat = tripVariants.route.originStop.lat;
                        stopTimeModel.calculatedSeconds = 0;
                        stopTimeModel.customizedMinutes = 0;
                        this.stopTimes.push(stopTimeModel);

                    } else if (this.$tripVariants.trips.length == 1 && this.$tripVariants.trips[0].isMainVariant && this.$tripVariants.trips[0].mode === TripMode.Front) {
                        this.$tripDetails.isMainVariant = true;
                        this.$tripDetails.tripId.variantName = "MAIN";
                        this.$tripDetails.tripId.variantMode = TripMode.Back;
                        this.$tripDetails.tripId.trafficMode = TrafficMode.Normal;
                        this.$tripDetails.originStopName = tripVariants.route.destinationStop.name;
                        this.$tripDetails.destinationStopName = tripVariants.route.originStop.name;
                        this.$tripDetails.headsign = tripVariants.route.originStop.name;

                        const stopTimeModel: StopTimeModel = {} as StopTimeModel;
                        stopTimeModel.stopId = tripVariants.route.originStop.id;
                        stopTimeModel.stopName = tripVariants.route.originStop.name;
                        stopTimeModel.lon = tripVariants.route.originStop.lon;
                        stopTimeModel.lat = tripVariants.route.originStop.lat;
                        stopTimeModel.calculatedSeconds = 0;
                        stopTimeModel.customizedMinutes = 0;
                        this.stopTimes.push(stopTimeModel);
                    }
                }
            });
        });
        this.$tripDetails.calculatedCommunicationVelocity = this.$tripDetails.calculatedCommunicationVelocity || 30;
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
            this.map.flyTo([this.$tripVariants.route.originStop.lat, this.$tripVariants.route.originStop.lon], 15)
        }
        this.drawPolyline(this.$tripDetails.geometry || []);
        this.zoomPolyline();
        this.reloadStops(this.map);
        this.onZoomEnd(this.map);
        this.onMoveEnd(this.map);
    }

    public onChangeDeparture(currentStopTime: StopTimeModel, no: number, value: number): void {
        const timeDifference: number = value - currentStopTime.customizedMinutes;
        currentStopTime.customizedMinutes = value;
        this.stopTimes.forEach((model: StopTimeModel, index: number) => {
            if (no < index) {
                model.customizedMinutes = model.customizedMinutes + timeDifference;
                model.customizedMinutesControl.setValue(model.customizedMinutes);
            }
        });
    }

    private initMap(): Map {
        const map: Map = L.map('map', {
            center: [50.613531, 20.743607],
            // center: [52.2321, 20.0559],
            // zoom: 7,
            zoom: 14,
            zoomControl: false
        });

        L.control.zoom({
            position: 'bottomright'
        }).addTo(map);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            minZoom: 3,
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(map);

        return map;
    }

    private onMoveEnd(map: Map): void {
        map.on('moveend', (event: LeafletEvent) => this.reloadStops(map));
    }

    private onZoomEnd(map: Map): void {
        map.on('zoomend', (event: LeafletEvent) => this.reloadStops(map));
    }

    private reloadStops(map: Map) {
        if (map.getZoom() > 12) {
            const bounds = map.getBounds();
            this.stopsService.getStopsInArea(bounds.getNorth(), bounds.getWest(), bounds.getSouth(), bounds.getEast()).subscribe(response => {
                const stopMarkers: Marker[] = response.stops?.map((stop: Stop) => L.marker([stop?.lat || 0.0, stop?.lon || 0.0], {icon: stop.isBdot10k ? this.BDOT10K_STOP : this.OTP_STOP})
                    .on('click', (event: LeafletMouseEvent) => {
                        const stopTime: StopTimeModel = {} as StopTimeModel;
                        stopTime.stopId = stop.id;
                        stopTime.stopName = stop.name;
                        stopTime.lon = stop.lon;
                        stopTime.lat = stop.lat;
                        stopTime.calculatedSeconds = 0;
                        stopTime.customizedMinutes = 0;

                        // const orderedStops: StopTimeModel[] = this.stopTimes
                        //     .map((stop: StopTimeModel) => {
                        //         const distance: number = haversine(
                        //             {latitude: stopTime.lat, longitude: stopTime.lon},
                        //             {latitude: stop.lat, longitude: stop.lon});
                        //         return {stopTime: stop, distance: distance};
                        //     }).sort((current: { stopTime: StopTimeModel, distance: number }, prev: {
                        //         stopTime: StopTimeModel,
                        //         distance: number
                        //     }): number => {
                        //         return current.distance - prev.distance;
                        //     })
                        //     .map((stop: { stopTime: StopTimeModel, distance: number }) => stop.stopTime);

                        // this.stopTimes.splice(this.stopTimes.indexOf(orderedStops[0]), 0, stopTime);

                        this.stopTimes.push(stopTime);
                        this.forceRefreshIn10seconds();

                        this.approximateDistance();

                        this._viewportScroller.scrollToAnchor('map');

                    })
                    .on('mouseover', (event: LeafletMouseEvent) => {
                        this.popup = L.popup({closeButton: false})
                            .setLatLng(event.latlng)
                            .setContent(stop.name)
                            .openOn(map);
                    })
                    .on('mouseout', (event: LeafletMouseEvent) => {
                        if (this.popup) {
                            this.popup.closePopup();
                            this.popup.removeFrom(map);
                        }
                    })) || []

                stopMarkers.forEach(marker => marker.addTo(map));

                for (let marker of this.stopMarkers) {
                    marker.removeFrom(map);
                }
                this.stopMarkers = stopMarkers;
            })
        } else {
            this.stopMarkers.forEach(marker => marker.removeFrom(map));
            this.stopMarkers = [];
        }
    }

    private approximateDistance(zoom: boolean = false) {
        const trips: TripMeasure = this.buildTripsMeasureRequest();

        this.tripDistanceMeasuresService.approximateDistance(trips).subscribe(response => {
            for (const index in response.stops) {
                const stopTime: StopTime = (response.stops || [])[Number(index)];
                this.stopTimes[Number(index)].meters = stopTime.meters;
                this.stopTimes[Number(index)].calculatedSeconds = stopTime.calculatedSeconds;
                if (this.stopTimes[Number(index)].customizedMinutes == 0) {
                    this.stopTimes[Number(index)].customizedMinutes = Math.ceil(stopTime.calculatedSeconds / 60);
                }
            }

            this.drawPolyline(response.geometry);

            if (zoom) {
                this.zoomPolyline();
            }
        });
    }

    private buildTripsMeasureRequest(): TripMeasure {
        const tripId: TripId = {
            routeId: {
                line: this.state.line,
                name: this.state.name
            },
            variantMode: this.state.mode,
            trafficMode: this.state.trafficMode
        };
        const tripMeasure: TripMeasure = {
            tripId: tripId,
            velocity: this.$tripDetails.calculatedCommunicationVelocity
        };
        tripMeasure.stops = this.stopTimes.map((stop: StopTimeModel): StopTime => {
            const stopTime: StopTime = {};
            stopTime.stopId = stop.stopId;
            stopTime.stopName = stop.stopName;
            stopTime.lon = stop.lon;
            stopTime.lat = stop.lat;
            return stopTime;
        });
        return tripMeasure;
    }

    public clickCreateOrEdit(model: FormGroup): void {
        const routeId: RouteId = {};
        routeId.name = this.state.name;
        routeId.line = this.state.line;

        const tripId: TripId = {};
        tripId.routeId = routeId;
        tripId.variantName = this.state.variant;
        tripId.variantMode = this.state.mode;
        tripId.trafficMode = this.state.trafficMode;

        const tripDetailsRequest: UpdateTripDetailsRequest = {};
        tripDetailsRequest.tripId = tripId;

        const updatedTripId: TripId = {};
        updatedTripId.routeId = routeId;
        updatedTripId.variantName = this.modelForm.controls['tripVariantName'].value;
        updatedTripId.variantMode = this.modelForm.controls['tripVariantMode'].value;
        updatedTripId.trafficMode = this.modelForm.controls['tripTrafficMode'].value;

        tripDetailsRequest.body = this.$tripDetails;
        tripDetailsRequest.body.tripId = updatedTripId;
        tripDetailsRequest.body.isMainVariant = this.modelForm.controls['isMainVariant'].value
        tripDetailsRequest.body.isCustomized = this.modelForm.controls['isCustomized'].value

        tripDetailsRequest.body.variantDesignation = this.modelForm.controls['variantDesignation'].value
        tripDetailsRequest.body.variantDescription = this.modelForm.controls['variantDescription'].value

        tripDetailsRequest.body.originStopName = this.modelForm.controls['origin'].value
        tripDetailsRequest.body.destinationStopName = this.modelForm.controls['destination'].value
        tripDetailsRequest.body.headsign = this.modelForm.controls['headsign'].value

        tripDetailsRequest.body.calculatedCommunicationVelocity = this.modelForm.controls['calculatedCommunicationVelocity'].value;
        tripDetailsRequest.body.customizedCommunicationVelocity = Math.round(this.customizedCommunicationVelocity());

        tripDetailsRequest.body.stops = this.stopTimes.map(stopTimeModel => {
            const stopTime: StopTime = {};
            stopTime.stopId = stopTimeModel.stopId;
            stopTime.stopName = stopTimeModel.stopName;
            stopTime.lat = stopTimeModel.lat;
            stopTime.lon = stopTimeModel.lon;
            stopTime.calculatedSeconds = stopTimeModel.calculatedSeconds;
            stopTime.customizedSeconds = 60 * stopTimeModel.customizedMinutesControl.value;
            stopTime.meters = stopTimeModel.meters;

            return stopTime;
        });

        if (this.tripEditorComponentMode == TripEditorComponentMode.CREATE) {
            this.tripService.createTrip(this.agencyStorageService.getInstance(), tripDetailsRequest).subscribe({
                next: () => {
                    this.notificationService.showSuccess(`Linia ${this.state.line} ${this.state.name} została utworzona`);
                    this.router.navigate(['/agency/trips'], {
                        queryParams: {
                            line: this.state.line,
                            name: this.state.name
                        }
                    }).then();
                },
                error: (response: HttpErrorResponse) => {
                    const payload: ErrorResponse = response.error;
                    this.notificationService.showError(`${payload.errorCode}`);
                }
            });
        } else if (this.tripEditorComponentMode == TripEditorComponentMode.EDIT) {
            this.tripService.updateTrip(this.agencyStorageService.getInstance(), tripDetailsRequest).subscribe({
                next: () => {
                    this.notificationService.showSuccess(`Linia ${this.state.line} ${this.state.name} została zaktualizowana`);
                    this.router.navigate(['/agency/trips'], {
                        queryParams: {
                            line: this.state.line,
                            name: this.state.name
                        }
                    }).then();
                },
                error: (response: HttpErrorResponse) => {
                    const payload: ErrorResponse = response.error;
                    console.log(payload);
                }
            });
        }

    }

    public drawPolyline(geometry: Point2D[]) {
        const latLngPoints = (geometry || []).map(stopTime => new L.LatLng(stopTime.lat || 0.0, stopTime.lon || 0.0));
        const polyline = L.polyline(latLngPoints, {
            color: '#416AB6',
            weight: 8,
            opacity: 0.9,
            smoothFactor: 1
        });

        if (this.routePolyline == null) {
            this.routePolyline = polyline;
        } else {
            this.routePolyline.removeFrom(this.map);
            this.routePolyline = polyline;
        }
        polyline.addTo(this.map);
    }

    public drop(event: CdkDragDrop<string[]>) {
        moveItemInArray(this.stopTimes, event.previousIndex, event.currentIndex);
        this.approximateDistance();
        this.forceRefreshIn10seconds();
    }

    public remove(stopTime: StopTime) {
        const index = findIndex(this.stopTimes, {stopId: stopTime.stopId});
        this.stopTimes.splice(index, 1);
        this.forceRefreshIn10seconds();
    }

    public measureDistance(): void {
        this.tripDistanceMeasuresService.measureDistance(this.buildTripsMeasureRequest()).subscribe(response => {
            for (const index in response.stops) {
                const stopTime: StopTime = (response.stops || [])[Number(index)];
                this.stopTimes[Number(index)].meters = stopTime.meters;
                this.stopTimes[Number(index)].calculatedSeconds = stopTime.calculatedSeconds;
                if (this.stopTimes[Number(index)].customizedMinutes == 0) {
                    this.stopTimes[Number(index)].customizedMinutes = Math.ceil(stopTime.calculatedSeconds / 60);
                }
            }

            this.drawPolyline(response.geometry);
            this.$tripDetails.geometry = response.geometry;
        });

    }

    public onCommunicationVelocityChange(value: number): void {
        if (size(this.stopTimes) > 1) {
            this.communicationVelocitySubject.next(value);
            this.forceRefreshIn10seconds();
        }
    }

    public getLastStop(): StopTimeModel {
        return last(this.stopTimes);
    }

    public zoomPolyline(): void {
        if (this.routePolyline.getBounds().isValid()) {
            this.map.fitBounds(this.routePolyline.getBounds());
        }
    }

    public clickIsMainVariant(): void {
        const isMainVariant: boolean = this.modelForm.controls['isMainVariant'].value as boolean;

        if (isMainVariant) {
            this.modelForm.controls['tripVariantName'].setValue(this.previousVariantName);
        } else {
            this.previousVariantName = this.modelForm.controls['tripVariantName'].value;
            this.modelForm.controls['tripVariantName'].setValue('MAIN');
            this.modelForm.controls['variantDesignation'].setValue('');
            this.modelForm.controls['variantDescription'].setValue('');
        }

    }

    openDialogEditStop(stopTime: StopTime): void {
        const data: BusStopModalEditorData = {} as BusStopModalEditorData;
        data.stopId = stopTime.stopId;
        data.stopName = stopTime.stopName;
        data.lat = stopTime.lat;
        data.lon = stopTime.lon;

        const dialogRef = this.dialog.open(BusStopModalEditorComponent, {
            data: data,
        });

        dialogRef.afterClosed().subscribe((busStopSelectorData: BusStopSelectorData | undefined) => {
            if (busStopSelectorData !== undefined) {
                const stopTime: StopTime = find(this.$tripDetails.stops, {stopId: busStopSelectorData.stopId});
                stopTime.stopName = busStopSelectorData.stopName;
            }
        });
    }

    public refreshMap(): void {
        this.measureDistance();
        this.isRefreshingExpanded = false;
        this.isRefreshExpanded = true;
    }

    private forceRefreshIn10seconds() {
        this.isRefreshingExpanded = true;
        this.isRefreshExpanded = false;
        this.forceRefreshSubject.next(true);
    }

    public isMainVariant(): boolean {
        return this.modelForm.controls['isMainVariant'].value;
    }

    public isCustomized(): boolean {
        return this.modelForm.controls['isCustomized'].value;
    }

    public customizedCommunicationVelocity(): number {
        const lastStop: StopTimeModel = this.getLastStop();
        const velocityMetersPerSeconds: number = lastStop.meters / (60 * lastStop.customizedMinutes);
        return velocityMetersPerSeconds * 3600 / 1000;
    }
}
