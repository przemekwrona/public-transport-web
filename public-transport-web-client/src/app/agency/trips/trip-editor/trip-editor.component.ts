import {AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from "leaflet";
import {LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline, Popup} from "leaflet";
import {find, size} from "lodash";
import {
    ErrorResponse,
    Point2D,
    RouteDetails,
    RouteId,
    Stop,
    StopsService,
    StopTime,
    TrafficMode,
    TripDistanceMeasuresService,
    TripId,
    TripMeasure,
    TripMode,
    TripsDetails,
    TripService,
    UpdateTripDetailsRequest
} from "../../../generated/public-transport-api";
import {ActivatedRoute, Data, Router} from "@angular/router";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {debounceTime, map, Observable, pairwise, startWith, Subject} from "rxjs";
import {TripEditorComponentMode} from "./trip-editor-component-mode";
import {ViewportScroller} from "@angular/common";
import {MatDialog} from "@angular/material/dialog";
import {
    BusStopModalEditorComponent,
    BusStopModalEditorData
} from "../../shared/bus-stop-modal-editor/bus-stop-modal-editor.component";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {NotificationService} from "../../../shared/notification.service";
import {HttpErrorResponse} from "@angular/common/http";
import {AbstractControl, FormArray, FormBuilder, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {TripIdExistenceValidator} from "./trip-id-existence/trip-id-existence.service";
import {AllValidationErrors, FormUtils} from "../../../shared/form.utils";
import {BusStopData} from "../../shared/bus-stop-modal-selector/bus-stop-modal-selector.component";

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

    public state: {
        name: string,
        line: string,
        version: number,
        variant: string,
        mode: TripMode,
        trafficMode: TrafficMode
    };

    public tripModeSelectValue = TripMode;
    public tripEditorComponentMode: TripEditorComponentMode;

    public trafficModeSelectValue = TrafficMode

    public geometry: Array<Point2D> = [];
    public $tripVariants: RouteDetails = {};

    public forceRefreshSubject: Subject<boolean> = new Subject();
    public forceRefresh$: Observable<boolean> = this.forceRefreshSubject.asObservable();

    public isRefreshExpanded: boolean = false;
    public isRefreshingExpanded: boolean = false;

    public modelForm: FormGroup;
    public isSubmited: boolean = false;

    get stops(): FormArray<FormGroup> {
        return this.modelForm.get('stops') as FormArray;
    }

    createStop(stop: Stop): FormGroup {
        const stopControl: FormGroup = this.formBuilder.group({
            id: [stop.id],
            name: [stop.name],
            lon: [stop.lon],
            lat: [stop.lat],
            meters: [null],
            calculatedSeconds: [null],
            customizedMinutes: [null, [Validators.required, Validators.min(0)]],
            bdot10k: [stop.isBdot10k]
        });

        this.appendValueChangesOnCustomizedMinutes(stopControl);

        return stopControl;
    }

    private appendValueChangesOnCustomizedMinutes(stopControl: FormGroup<any>) {
        const customizedMinutesControl: AbstractControl<number> = stopControl.controls["customizedMinutes"];

        customizedMinutesControl.valueChanges.pipe(startWith(customizedMinutesControl.value), pairwise()).subscribe(([prev, next]: [number, number]) => {
            const index: number = this.stops.controls.indexOf(stopControl);
            this.onChangeDeparture(index, next - prev);
        });
    }

    createStopFromStopTimeModel(stop: StopTime): FormGroup {
        const stopControl: FormGroup = this.formBuilder.group({
            id: [stop.stopId],
            name: [stop.stopName],
            lon: [stop.lon],
            lat: [stop.lat],
            meters: [stop.meters],
            calculatedSeconds: [stop.calculatedSeconds],
            customizedMinutes: [stop.customizedSeconds / 60, [Validators.required, Validators.min(0)]],
            bdot10k: [stop.bdot10k]
        });
        this.appendValueChangesOnCustomizedMinutes(stopControl);
        return stopControl;
    }

    constructor(private stopsService: StopsService, private tripService: TripService, private tripDistanceMeasuresService: TripDistanceMeasuresService, private agencyStorageService: AgencyStorageService, private router: Router, private _route: ActivatedRoute, private _viewportScroller: ViewportScroller, private dialog: MatDialog, private notificationService: NotificationService, private formBuilder: FormBuilder, private tripIdExistenceValidator: TripIdExistenceValidator) {
        this.communicationVelocitySubject.pipe(debounceTime(1000)).subscribe(() => this.approximateDistance());
    }

    ngOnInit(): void {
        this._route.data.subscribe((data: Data) => this.tripEditorComponentMode = data['mode']);

        this._route.queryParams.subscribe(params => this.state = params as {
            line: string,
            name: string,
            version: number,
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
            },
            {
                asyncValidators: this.tripIdExistenceValidator.variantExistsValidator(this.state.line, this.state.name, this.state.name, this.state.mode, this.state.trafficMode)
            });

        this.modelForm.get('calculatedCommunicationVelocity')!.valueChanges.subscribe((value: number) => this.onCommunicationVelocityChange(value));
        this.modelForm.get('isMainVariant').valueChanges.pipe(pairwise()).subscribe(([prev, next]: [boolean, boolean]) => this.clickIsMainVariant(next));
        this.modelForm.get('tripVariantMode').valueChanges.subscribe((value: TripMode) => this.onChangeVariantMode(value));

        this._route.data.pipe(map((data: Data) => data['trip'])).subscribe((tripDetails: TripsDetails) => {
            this.modelForm.controls['isMainVariant'].setValue(tripDetails.isMainVariant);
            this.modelForm.controls['isCustomized'].setValue(tripDetails.isCustomized);
            this.modelForm.controls['calculatedCommunicationVelocity'].setValue(tripDetails.calculatedCommunicationVelocity || 30);

            if (tripDetails.isMainVariant) {
                this.modelForm.controls['tripVariantName'].disable();
                this.modelForm.controls['tripVariantName'].setValue('MAIN');
            }

            this.modelForm.controls['variantDesignation'].setValue(tripDetails.variantDesignation);
            this.modelForm.controls['variantDescription'].setValue(tripDetails.variantDescription);

            if (tripDetails.isMainVariant) {
                this.modelForm.controls["variantDesignation"].setValidators(null);
                this.modelForm.controls["variantDescription"].setValidators(null);
            }

            this.modelForm.controls['origin'].setValue(tripDetails.originStopName);
            this.modelForm.controls['destination'].setValue(tripDetails.destinationStopName);
            this.modelForm.controls['headsign'].setValue(tripDetails.headsign);

            this.geometry = tripDetails.geometry;
            (tripDetails.stops || []).forEach((stopTime: StopTime) => this.stops.push(this.createStopFromStopTimeModel(stopTime)));

            this._route.data.pipe(map((data: Data) => data['variants'])).subscribe((tripVariants: RouteDetails) => {
                this.$tripVariants = tripVariants;

                if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
                    if (size(this.$tripVariants?.trips) === 0) {
                        this.modelForm.controls["isMainVariant"].setValue(true);
                        this.modelForm.controls["tripVariantName"].setValue("MAIN");
                        this.modelForm.controls["tripVariantMode"].setValue(TripMode.Front);
                        this.modelForm.controls["tripTrafficMode"].setValue(TrafficMode.Normal);
                        this.modelForm.controls["variantDesignation"].setValidators(null);
                        this.modelForm.controls["variantDescription"].setValidators(null);

                        this.modelForm.controls["origin"].setValue(tripVariants.route.originStop.name);
                        this.modelForm.controls["destination"].setValue(tripVariants.route.destinationStop.name);
                        this.modelForm.controls["headsign"].setValue(tripVariants.route.destinationStop.name);
                    }
                }
            });
        });
    }

    ngAfterViewInit(): void {
        this.map = this.initMap();
        if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
            this.map.flyTo([this.$tripVariants.route.originStop.lat, this.$tripVariants.route.originStop.lon], 15)
        }
        this.drawPolyline(this.geometry || []);
        this.zoomPolyline();
        this.reloadStops(this.map);
        this.onZoomEnd(this.map);
        this.onMoveEnd(this.map);
    }

    public onChangeDeparture(no: number, timeDifference: number): void {
        this.stops.controls.forEach((stopTimeControl: FormGroup, index: number) => {
            if (no < index) {
                const customizedMinutesControl: AbstractControl<number> = stopTimeControl.controls["customizedMinutes"];
                const value: number = customizedMinutesControl.value + timeDifference;
                customizedMinutesControl.setValue(value, {emitEvent: false});
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
                        const stopControl: FormGroup = this.createStop(stop);

                        this.stops.push(stopControl);

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
            this.stops.controls.forEach((formGroup: FormGroup, index: number) => {
                const stopResponse: StopTime = response.stops[index];
                if (!stopResponse) return;
                formGroup.controls["meters"].setValue(stopResponse.meters);
                formGroup.controls["calculatedSeconds"].setValue(stopResponse.calculatedSeconds);
                formGroup.controls["customizedMinutes"].setValue(Math.ceil(stopResponse.calculatedSeconds / 60));
            });

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
                name: this.state.name,
                version: this.state.version
            },
            variantMode: this.state.mode,
            trafficMode: this.state.trafficMode
        };
        const tripMeasure: TripMeasure = {
            tripId: tripId,
            velocity: this.modelForm.controls["calculatedCommunicationVelocity"].value
        };
        tripMeasure.stops = this.stops.controls.map((stop: FormGroup): StopTime => {
            const stopTime: StopTime = {};
            stopTime.stopId = stop.controls["id"].value;
            stopTime.stopName = stop.controls["name"].value;
            stopTime.lon = stop.controls["lon"].value;
            stopTime.lat = stop.controls["lat"].value;
            return stopTime;
        });
        return tripMeasure;
    }

    public clickCreateOrEdit(): void {
        const errors: AllValidationErrors[] = FormUtils.getFormValidationErrors(this.modelForm);
        console.log(errors);

        this.isSubmited = true;
        if (this.modelForm.invalid) {
            this.notificationService.showError('Formularz jest niepoprawny uzupełnij braki');
            this.scrollToFirstError();
            return
        }

        const routeId: RouteId = {};
        routeId.name = this.state.name;
        routeId.line = this.state.line;
        routeId.version = this.state.version;

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

        tripDetailsRequest.body = {};
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

        tripDetailsRequest.body.stops = this.stops.controls.map((stopTimeFormGroup: FormGroup) => {
            const stopTime: StopTime = {};
            stopTime.stopId = stopTimeFormGroup.controls["id"].value;
            stopTime.stopName = stopTimeFormGroup.controls["name"].value;
            stopTime.lat = stopTimeFormGroup.controls["lat"].value;
            stopTime.lon = stopTimeFormGroup.controls["lon"].value;
            stopTime.calculatedSeconds = stopTimeFormGroup.controls["calculatedSeconds"].value;
            if (this.modelForm.controls['isCustomized'].value) {
                stopTime.customizedSeconds = 60 * stopTimeFormGroup.controls["customizedMinutes"].value;
            } else {
                stopTime.customizedSeconds = 60 * stopTimeFormGroup.controls["calculatedSeconds"].value;
            }
            stopTime.meters = stopTimeFormGroup.controls["meters"].value;

            return stopTime;
        });
        tripDetailsRequest.body.geometry = this.geometry;

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
                            name: this.state.name,
                            version: this.state.version
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
        moveItemInArray(this.stops.controls, event.previousIndex, event.currentIndex);
        this.approximateDistance();
        this.forceRefreshIn10seconds();
    }

    public remove(index: number) {
        this.stops.removeAt(index);
        this.forceRefreshIn10seconds();
    }

    public measureDistance(): void {
        this.tripDistanceMeasuresService.measureDistance(this.buildTripsMeasureRequest()).subscribe(response => {
            this.stops.controls.forEach((formGroup: FormGroup, index: number) => {
                const stopResponse: StopTime = response.stops[index];
                if (!stopResponse) return;
                formGroup.controls["meters"].setValue(stopResponse.meters);
                formGroup.controls["calculatedSeconds"].setValue(stopResponse.calculatedSeconds);
                if (formGroup.controls["customizedMinutes"].value == 0) {
                    formGroup.controls["customizedMinutes"].setValue(Math.ceil(stopResponse.calculatedSeconds / 60));
                }
            });

            this.drawPolyline(response.geometry);
            this.geometry = response.geometry;
        });

    }

    public onCommunicationVelocityChange(value: number): void {
        if (size(this.stops.controls) > 1) {
            this.communicationVelocitySubject.next(value);
            this.forceRefreshIn10seconds();
        }
    }

    public getLastStop(): FormGroup | null {
        if (this.stops.length === 0) {
            return null;
        }
        return this.stops.at(this.stops.length - 1);
    }

    public zoomPolyline(): void {
        if (this.routePolyline.getBounds().isValid()) {
            this.map.fitBounds(this.routePolyline.getBounds());
        }
    }

    clickIsMainVariant(isMainVariant: boolean): void {
        const tripVariantNameControl: AbstractControl = this.modelForm.get('tripVariantName');
        const variantDesignationControl: AbstractControl = this.modelForm.get('variantDesignation');
        const variantDescriptionControl: AbstractControl = this.modelForm.get('variantDescription');

        if (isMainVariant) {
            tripVariantNameControl.setValue(this.previousVariantName);
            tripVariantNameControl?.disable();

            variantDesignationControl.setValue('');
            variantDesignationControl.setValidators([]);
            variantDesignationControl.updateValueAndValidity({emitEvent: false});

            variantDescriptionControl.setValue('');
            variantDescriptionControl.setValidators([]);
            variantDescriptionControl.updateValueAndValidity({emitEvent: false});
        } else {
            this.previousVariantName = tripVariantNameControl.value;
            tripVariantNameControl.setValue('MAIN');

            variantDesignationControl.setValue('');
            variantDesignationControl.setValidators([Validators.required]);
            variantDesignationControl.updateValueAndValidity({emitEvent: false});

            variantDescriptionControl.setValue('');
            variantDescriptionControl.setValidators([Validators.required]);
            variantDescriptionControl.updateValueAndValidity({emitEvent: false});

            tripVariantNameControl?.enable();
        }
    }

    openDialogEditStop(stopTime: FormGroup): void {
        const data: BusStopModalEditorData = {} as BusStopModalEditorData;
        data.stopId = stopTime.controls["id"].value;
        data.stopName = stopTime.controls["name"].value;
        data.lat = stopTime.controls["lon"].value;
        data.lon = stopTime.controls["lat"].value;

        const dialogRef = this.dialog.open(BusStopModalEditorComponent, {
            data: data,
        });

        dialogRef.afterClosed().subscribe((busStopSelectorData: BusStopData | undefined) => {
            if (busStopSelectorData !== undefined) {
                const stopTimeControl: FormGroup = this.stops.controls.find((formGroup: FormGroup) => formGroup.controls["id"].value === busStopSelectorData.stopId)
                stopTimeControl.controls["name"].setValue(busStopSelectorData.stopName);
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

    public customizedCommunicationVelocity(): number | null {
        const lastStop: FormGroup | null = this.getLastStop();

        if (lastStop == null) {
            return null;
        }

        const distanceInMeters: number = lastStop.controls["meters"].value;
        const timeInMinutes: number = 60 * lastStop.controls["customizedMinutes"].value;
        const velocityMetersPerSeconds: number = distanceInMeters / timeInMinutes;
        return velocityMetersPerSeconds * 3600 / 1000;
    }

    public validControl(controlName: string): ValidationErrors | null {
        return this.isSubmited && this.modelForm?.controls[controlName]?.errors;
    }

    public checkControlHasError(controlName: string, errorName: string): boolean {
        return this.isSubmited && this.validControl(controlName)?.[errorName] || false;
    }

    public canCheckErrors(controlName: string): boolean {
        return this.isSubmited && this.validControl(controlName) != null;
    }

    public scrollToFirstError(): void {
        setTimeout(() => {
            const invalidControl = document.querySelector('.text-danger');
            invalidControl?.scrollIntoView({behavior: 'smooth', block: 'center'});
        });
    }

    public onChangeVariantMode(tripMode: TripMode): void {
        if (this.stops.length === 0) {
            if (tripMode === TripMode.Front) {
                this.map.flyTo([this.$tripVariants.route.originStop.lat, this.$tripVariants.route.originStop.lon], 15)
            }
            if (tripMode === TripMode.Back) {
                this.map.flyTo([this.$tripVariants.route.destinationStop.lat, this.$tripVariants.route.destinationStop.lon], 15)
            }
        }
    }
}
