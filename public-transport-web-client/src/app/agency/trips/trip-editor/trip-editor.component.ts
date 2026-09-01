import {AfterViewInit, Component, OnInit} from '@angular/core';
import * as L from "leaflet";
import {LeafletEvent, LeafletMouseEvent, Map, Marker, Polyline, Popup} from "leaflet";
import {find, size} from "lodash";
import {
    ErrorResponse,
    Point2D,
    RouteDetails, RouteId1,
    Stop,
    StopsService,
    StopTime,
    TrafficMode,
    TripDistanceMeasuresService,
    TripId,
    TripMeasure,
    TripMode, TripProfile,
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
import {
    AbstractControl,
    FormArray,
    FormBuilder,
    FormControl,
    FormGroup,
    ValidationErrors,
    Validators
} from "@angular/forms";
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
    ],
    standalone: false
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

    private previousVariantName = '';

    public tripModeSelectValue = TripMode;
    public tripEditorComponentMode: TripEditorComponentMode;

    public readonly trafficModes: TrafficMode[] = [TrafficMode.Normal, TrafficMode.Traffic];

    public geometry: Array<Point2D> = [];
    public $tripVariants: RouteDetails = {};
    public activeTrafficMode: TrafficMode = TrafficMode.Normal;

    public forceRefreshSubject: Subject<boolean> = new Subject();
    public forceRefresh$: Observable<boolean> = this.forceRefreshSubject.asObservable();

    public isRefreshExpanded: boolean = false;
    public isRefreshingExpanded: boolean = false;
    public isMainVariantDescriptionExpanded = false;

    public modelForm: FormGroup;
    public isSubmited: boolean = false;
    private isShiftingFollowingStopTimes = false;

    public tripDetails: TripsDetails = {} as TripsDetails;

    public routeCode: string = '';
    public tripCode: string = '';


    get profiles(): FormArray<FormGroup> {
        return this.modelForm.get('profiles') as FormArray;
    }

    public getStops(profile: FormGroup): FormArray<FormGroup> {
        return profile.get('stops') as FormArray<FormGroup>;
    }

    public get activeTrafficModeIndex(): number {
        return this.getProfileIndexByTrafficMode(this.activeTrafficMode);
    }

    public setActiveTrafficMode(trafficMode: TrafficMode): void {
        this.activeTrafficMode = trafficMode;
    }

    public onSelectedProfileIndexChange(index: number): void {
        const profile = this.profiles.controls[index];
        if (profile) {
            this.setActiveTrafficMode(profile.controls['trafficMode'].value);
        }
    }

    public getProfileIndexByTrafficMode(trafficMode: TrafficMode): number {
        if (!this.modelForm) {
            return 0;
        }

        const index = this.profiles.controls.findIndex(profile => profile.controls['trafficMode'].value === trafficMode);
        return index >= 0 ? index : 0;
    }

    constructor(private stopsService: StopsService, private tripService: TripService, private tripDistanceMeasuresService: TripDistanceMeasuresService, private agencyStorageService: AgencyStorageService, private router: Router, private _route: ActivatedRoute, private _viewportScroller: ViewportScroller, private dialog: MatDialog, private notificationService: NotificationService, private formBuilder: FormBuilder, private tripIdExistenceValidator: TripIdExistenceValidator) {
    }

    ngOnInit(): void {
        this.routeCode = this._route.snapshot.paramMap.get('routeCode')!;
        this.tripCode = this._route.snapshot.paramMap.get('tripCode')!;
        this.tripEditorComponentMode = this._route.snapshot.data['mode'];

        this._route.data.subscribe((data: Data) => this.tripEditorComponentMode = data['mode']);

        const resolvedTripDetails: TripsDetails = this._route.snapshot.data['trip'];
        const initialTripMode = this.resolveTripVariantMode(resolvedTripDetails);

        this.modelForm = this.formBuilder.group({
                isMainVariant: [true, [Validators.required]],
                tripVariantName: ['', [Validators.required]],
                tripVariantMode: [initialTripMode, [Validators.required]],

                variantDesignation: ['', [Validators.required]],
                variantDescription: ['', [Validators.required]],

                headsign: ['', [Validators.required]],

                profiles: this.formBuilder.array([], [Validators.required, Validators.minLength(1)])
            },
            {
                // asyncValidators: this.tripIdExistenceValidator.variantExistsValidator(this.state.line, this.state.name, this.state.variant, this.state.tripMode)
            });

        this.modelForm.get('isMainVariant').valueChanges.pipe(pairwise()).subscribe(([prev, next]: [boolean, boolean]) => this.clickIsMainVariant(next));
        this.modelForm.get('tripVariantMode').valueChanges.subscribe((value: TripMode) => this.onChangeVariantMode(value));

        this._route.data.pipe(map((data: Data) => data['trip'])).subscribe((tripDetails: TripsDetails) => {
            this.tripDetails = tripDetails;
            this.tripVariantModeControl.setValue(this.resolveTripVariantMode(tripDetails));

            this.modelForm.controls['isMainVariant'].setValue(tripDetails.isMainVariant);

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

            this.modelForm.controls['headsign'].setValue(tripDetails.headsign);

            this.geometry = tripDetails.geometry;

            const profileControls = (tripDetails.tripProfiles || []).map((profile: TripProfile) => this.createProfile(profile));
            this.modelForm.setControl(
                'profiles',
                this.formBuilder.array(profileControls, [Validators.required, Validators.minLength(1)])
            );

            this._route.data.pipe(map((data: Data) => data['routeDetails'])).subscribe((routeDetails: RouteDetails) => {
                this.$tripVariants = routeDetails;

                if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
                    if (size(this.$tripVariants?.trips) === 0) {
                        this.modelForm.controls["isMainVariant"].setValue(true);
                        this.modelForm.controls["tripVariantName"].setValue("MAIN");
                        this.modelForm.controls["variantDesignation"].setValidators(null);
                        this.modelForm.controls["variantDescription"].setValidators(null);

                        this.modelForm.controls["headsign"].setValue(this.$tripVariants.route.destinationStop.name);
                    }
                }

            });
        });
    }

    private resolveTripVariantMode(tripDetails?: TripsDetails): TripMode {
        return tripDetails?.tripId?.variantMode ?? TripMode.Front;
    }


    private createStop(stop: Stop): FormGroup {
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

        return stopControl;
    }

    private createStopFromStopTimeModel(stop: StopTime): FormGroup {
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
        return stopControl;
    }

    private createProfile(profile: Partial<TripProfile> = {}): FormGroup {
        const stopControls = (profile.stops || []).map((stop: StopTime) => this.createStopFromStopTimeModel(stop));
        const profileControl = this.formBuilder.group({
            trafficMode: [profile.trafficMode ?? profile.trafficMode ?? TrafficMode.Normal],
            calculatedCommunicationVelocity: [profile.calculatedCommunicationVelocity ?? 30, [Validators.required, Validators.min(0)]],
            customizedCommunicationVelocity: [profile.customizedCommunicationVelocity ?? null],
            isCustomized: [profile.isCustomized ?? false],
            isDefault: [profile.isDefault ?? false],
            travelTimeInSeconds: [profile.travelTimeInSeconds ?? null],
            stops: this.formBuilder.array(stopControls, [Validators.required, Validators.minLength(2)])
        });
        profileControl.get('calculatedCommunicationVelocity')!.valueChanges.pipe(debounceTime(1000)).subscribe((value: number) => {
            if (value == null || size(this.getStops(profileControl).controls) <= 1) {
                return;
            }

            this.approximateDistance(profileControl);
            this.forceRefreshIn10seconds();
        });


        this.getStops(profileControl).controls.forEach((stopControl: FormGroup) => {
            this.subscribeToStopTimeChanges(profileControl, stopControl);
        });

        return profileControl;
    }

    get tripVariantModeControl(): FormControl {
        return this.modelForm.get('tripVariantMode') as FormControl;
    }

    get tripVariantNameControl(): FormControl {
        return this.modelForm.get('tripVariantName') as FormControl;
    }

    get variantDesignationControl(): FormControl {
        return this.modelForm.get('variantDesignation') as FormControl;
    }

    get variantDescriptionControl(): FormControl {
        return this.modelForm.get('variantDescription') as FormControl;
    }

    get headsignControl(): FormControl {
        return this.modelForm.get('headsign') as FormControl;
    }

    get fromTerritoryName(): string {
        return (this.isFrontVariantMode()
            ? this.$tripVariants.route.originTerritory.name
            : this.$tripVariants.route.destinationTerritory.name) ?? '';
    }

    get toTerritoryName(): string {
        return (this.isFrontVariantMode()
            ? this.$tripVariants.route.destinationTerritory.name
            : this.$tripVariants.route.originTerritory.name) ?? '';
    }

    public isFrontVariantMode(): boolean {
        const variantMode = this.modelForm?.controls['tripVariantMode']?.value;
        return variantMode === TripMode.Front;
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

    private subscribeToStopTimeChanges(profileControl: FormGroup, stopControl: FormGroup): void {
        const customizedMinutesControl = stopControl.controls['customizedMinutes'];
        customizedMinutesControl.valueChanges.pipe(
            startWith(customizedMinutesControl.value),
            pairwise()
        ).subscribe(([prev, next]: [number, number]) => {
            if (this.isShiftingFollowingStopTimes) {
                return;
            }

            const index = this.getStops(profileControl).controls.indexOf(stopControl);
            if (index < 0) {
                return;
            }

            this.onChangeDeparture(profileControl, index, (next ?? 0) - (prev ?? 0));
        });
    }

    public onChangeDeparture(profile: FormGroup, no: number, timeDifference: number): void {
        if (!timeDifference) {
            return;
        }

        this.isShiftingFollowingStopTimes = true;
        try {
            this.getStops(profile).controls.forEach((stopTimeControl: FormGroup, index: number) => {
                if (no < index) {
                    const customizedMinutesControl: AbstractControl<number> = stopTimeControl.controls["customizedMinutes"];
                    const value: number = (customizedMinutesControl.value ?? 0) + timeDifference;
                    customizedMinutesControl.setValue(value);
                }
            });
        } finally {
            this.isShiftingFollowingStopTimes = false;
        }
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
                        for (const profile of this.profiles.controls) {
                            const stopControl: FormGroup = this.createStop(stop);
                            this.subscribeToStopTimeChanges(profile, stopControl);
                            this.getStops(profile).push(stopControl);
                        }

                        this.forceRefreshIn10seconds();
                        this.approximateDistances();

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

    private approximateDistances(zoom: boolean = false): void {
        for (const profile of this.profiles.controls) {
            this.approximateDistance(profile, zoom);
        }
    }

    private approximateDistance(profile: FormGroup, zoom: boolean = false) {
        const trips: TripMeasure = this.buildTripsMeasureRequest(profile);

        if (trips.stops.length <= 1) {
            return
        }

        this.tripDistanceMeasuresService.approximateDistance(trips).subscribe(response => {
            this.applyMeasureResponseToCurrentStops(response, profile);

            this.drawPolyline(response.geometry);

            if (zoom) {
                this.zoomPolyline();
            }
        });
    }

    private buildTripsMeasureRequest(profile: FormGroup | null = this.getActiveProfile()): TripMeasure {
        const measuredProfile = profile ?? this.getActiveProfile();
        const tripMeasure: TripMeasure = {
            velocity: measuredProfile?.controls["calculatedCommunicationVelocity"].value
        };
        tripMeasure.stops = measuredProfile ? this.getStops(measuredProfile).controls.map((stop: FormGroup): StopTime => {
            const stopTime: StopTime = {};
            stopTime.stopId = stop.controls["id"].value;
            stopTime.stopName = stop.controls["name"].value;
            stopTime.lat = stop.controls["lat"].value;
            stopTime.lon = stop.controls["lon"].value;
            stopTime.calculatedSeconds = stop.controls["calculatedSeconds"].value;
            if (measuredProfile.controls['isCustomized'].value) {
                stopTime.customizedSeconds = 60 * stop.controls["customizedMinutes"].value;
            } else {
                stopTime.customizedSeconds = stop.controls["calculatedSeconds"].value;
            }
            stopTime.meters = stop.controls["meters"].value;

            return stopTime;
        }) : [];

        return tripMeasure;
    }

    get formValidationErrors(): AllValidationErrors[] {
        if (!this.isSubmited || !this.modelForm) {
            return [];
        }
        return FormUtils.getFormValidationErrors(this.modelForm);
    }

    public getFormErrorMessage(error: AllValidationErrors): string {
        const label = this.getControlLabel(error.controlName);

        switch (error.errorName) {
            case 'required':
                if (error.controlName === 'stops') {
                    return 'Musisz dodać co najmniej 2 przystanki na trasie. Obecna liczba przystanków wynosi 0.';
                }
                return `${label} jest wymagane`;
            case 'min':
                return `${label}: wartość musi być większa lub równa ${error.errorValue?.min}. Bieżąca wartość to: ${error.errorValue?.actual}`;
            case 'minlength':
                return `${label}: musisz dodać co najmniej ${error.errorValue?.requiredLength} przystanki. Obecna liczba przystanków wynosi ${error.errorValue?.actualLength || 0}.`;
            case 'variantExists':
                return `Trasa ${this.tripDetails.tripId.routeId.line} ${this.tripDetails.tripId.routeId.name} posiada wskazany wariant.`;
            default:
                return `${label}: ${error.errorName}`;
        }
    }

    private getControlLabel(controlName: string): string {
        const customizedMinutesMatch = controlName.match(/(?:^|\.)stops\[(\d+)\]\.customizedMinutes$/);
        if (customizedMinutesMatch) {
            return `Czas przejazdu na przystanku ${Number(customizedMinutesMatch[1]) + 1}`;
        }

        const fieldName = controlName.includes('.') ? controlName.split('.').pop()! : controlName;

        const labels: Record<string, string> = {
            isMainVariant: 'Wariant podstawowy',
            tripVariantName: 'Nazwa wariantu',
            tripVariantMode: 'Kierunek wariantu',
            trafficMode: 'Ruch panujący na drodze',
            variantDesignation: 'Symbol kursu na rozkładzie jazdy',
            variantDescription: 'Opis oznaczenia',
            origin: 'Przystanek początkowy',
            destination: 'Przystanek końcowy',
            headsign: 'Kierunek na tablicy czołowej',
            calculatedCommunicationVelocity: 'Prędkość komunikacyjna',
            isCustomized: 'Ręczna korekta czasu przejazdu',
            stops: 'Przystanki autobusowe',
            form: 'Formularz'
        };

        return labels[fieldName] || controlName;
    }

    public clickCreateOrEdit(): void {
        this.isSubmited = true;
        if (this.modelForm.invalid) {
            this.notificationService.showError('Formularz jest niepoprawny uzupełnij braki');
            this.scrollToFirstError();
            return
        }

        this.measureDistance().subscribe(success => {
            this.forceRefreshSubject.next(false);
            const tripDetailsRequest: UpdateTripDetailsRequest = this.buildCreateOrUpdateTripRequest();

            if (this.tripEditorComponentMode == TripEditorComponentMode.CREATE) {
                this.tripService.createTrip(this.agencyStorageService.getInstance(), this.$tripVariants.route.routeId.routeCode, tripDetailsRequest).subscribe({
                    next: () => {
                        this.notificationService.showSuccess(`Linia ${this.tripDetails.tripId.routeId.line} ${this.tripDetails.tripId.routeId.name} została utworzona`);
                        this.router.navigate(['/agency/routes', this.routeCode, 'trips'], {}).then();
                    },
                    error: (response: HttpErrorResponse) => {
                        const payload: ErrorResponse = response.error;
                        this.notificationService.showError(`${payload.errorCode}`);
                    }
                });
            } else if (this.tripEditorComponentMode == TripEditorComponentMode.EDIT) {
                this.tripService.updateTrip(this.agencyStorageService.getInstance(), this.routeCode, this.tripCode, tripDetailsRequest).subscribe({
                    next: () => {
                        this.notificationService.showSuccess(`Linia ${this.tripDetails.tripId.routeId.line} ${this.tripDetails.tripId.routeId.name} została zaktualizowana`);
                        this.router.navigate(['/agency/routes', this.routeCode, 'trips'], {}).then();
                    },
                    error: (response: HttpErrorResponse) => {
                        const payload: ErrorResponse = response.error;
                        console.log(payload);
                    }
                });
            }
        });
    }

    private buildCreateOrUpdateTripRequest() {
        const routeId: RouteId1 = {};
        routeId.name = this.$tripVariants.route.routeId.name;
        routeId.line = this.$tripVariants.route.routeId.line;
        routeId.version = this.$tripVariants.route.routeId.version;
        routeId.routeCode = this.$tripVariants.route.routeId.routeCode;

        const tripId: TripId = {};
        tripId.routeId = routeId;
        tripId.variantName = this.modelForm.controls['tripVariantName'].value;
        tripId.variantMode = this.modelForm.controls['tripVariantMode'].value;

        const tripDetailsRequest: UpdateTripDetailsRequest = {};

        const updatedTripId: TripId = {};
        updatedTripId.routeId = routeId;
        updatedTripId.variantName = this.modelForm.controls['tripVariantName'].value;
        updatedTripId.variantMode = this.modelForm.controls['tripVariantMode'].value;

        tripDetailsRequest.body = {};
        tripDetailsRequest.body.tripId = updatedTripId;
        tripDetailsRequest.body.isMainVariant = this.modelForm.controls['isMainVariant'].value
        // tripDetailsRequest.body.isCustomized = this.modelForm.controls['isCustomized'].value

        tripDetailsRequest.body.variantDesignation = this.modelForm.controls['variantDesignation'].value
        tripDetailsRequest.body.variantDescription = this.modelForm.controls['variantDescription'].value

        tripDetailsRequest.body.headsign = this.modelForm.controls['headsign'].value

        tripDetailsRequest.body.tripProfiles = this.profiles.controls.map((profileFormGroup: FormGroup): TripProfile => {
            const profile: TripProfile = {};
            profile.trafficMode = profileFormGroup.controls['trafficMode'].value;
            profile.travelTimeInSeconds = profileFormGroup.controls['travelTimeInSeconds'].value;
            profile.calculatedCommunicationVelocity = profileFormGroup.controls['calculatedCommunicationVelocity'].value;
            profile.customizedCommunicationVelocity = profileFormGroup.controls['customizedCommunicationVelocity'].value;
            profile.isDefault = profileFormGroup.controls['isDefault'].value;
            profile.isCustomized = profileFormGroup.controls['isCustomized'].value;
            profile.stops = this.getStops(profileFormGroup).controls.map((stopTimeFormGroup: FormGroup): StopTime => {
                const stopTime: StopTime = {};
                stopTime.stopId = stopTimeFormGroup.controls["id"].value;
                stopTime.stopName = stopTimeFormGroup.controls["name"].value;
                stopTime.lat = stopTimeFormGroup.controls["lat"].value;
                stopTime.lon = stopTimeFormGroup.controls["lon"].value;
                stopTime.calculatedSeconds = stopTimeFormGroup.controls["calculatedSeconds"].value;
                if (profileFormGroup.controls['isCustomized'].value) {
                    stopTime.customizedSeconds = 60 * stopTimeFormGroup.controls["customizedMinutes"].value;
                } else {
                    stopTime.customizedSeconds = stopTimeFormGroup.controls["calculatedSeconds"].value;
                }
                stopTime.meters = stopTimeFormGroup.controls["meters"].value;

                return stopTime;
            });
            return profile;
        });

        tripDetailsRequest.body.geometry = this.geometry;
        return tripDetailsRequest;
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
        // moveItemInArray(this.stops.controls, event.previousIndex, event.currentIndex);
        // this.approximateDistance();
        // this.forceRefreshIn10seconds();
    }

    public remove(index: number) {
        for (const profile of this.profiles.controls) {
            this.getStops(profile).removeAt(index);
        }
        this.forceRefreshIn10seconds();
    }

    public measureDistance(): Observable<TripMeasure> {
        const primaryProfile = this.getActiveProfile();
        const refreshedStops: Observable<TripMeasure> = this.tripDistanceMeasuresService.measureDistance(this.buildTripsMeasureRequest(primaryProfile));
        refreshedStops.subscribe(response => {
            this.applyMeasureResponseToCurrentStops(response, primaryProfile);

            this.drawPolyline(response.geometry);
            this.geometry = response.geometry;
        });

        for (const profile of this.profiles.controls) {
            if (profile === primaryProfile || this.getStops(profile).length <= 1) {
                continue;
            }

            this.tripDistanceMeasuresService.measureDistance(this.buildTripsMeasureRequest(profile)).subscribe(response => {
                this.applyMeasureResponseToCurrentStops(response, profile);
            });
        }

        return refreshedStops;
    }

    public getLastStop(profile: FormGroup): FormGroup | null {
        const stops = this.getStops(profile);
        if (!stops || stops.length === 0) {
            return null;
        }
        return stops.at(stops.length - 1);
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
                // const stopTimeControl: FormGroup = this.stops.controls.find((formGroup: FormGroup) => formGroup.controls["id"].value === busStopSelectorData.stopId)
                // stopTimeControl.controls["name"].setValue(busStopSelectorData.stopName);
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

    public isCustomized(profile: FormGroup): boolean {
        return this.isCustomizedControl(profile).value;
    }

    public isCustomizedControl(profile: FormGroup): FormControl {
        return profile.get('isCustomized') as FormControl;
    }

    public customizedCommunicationVelocity(profile: FormGroup): number | null {
        return profile.controls['customizedCommunicationVelocity'].value;
    }

    public trafficModeIcon(trafficMode: TrafficMode): string {
        return trafficMode === TrafficMode.Traffic ? 'traffic_jam' : 'directions_bus';
    }

    public hasProfileForTrafficMode(trafficMode: TrafficMode): boolean {
        return this.profiles.controls.some(profile => profile.controls['trafficMode'].value === trafficMode);
    }

    public selectProfileByTrafficMode(trafficMode: TrafficMode): void {
        if (this.hasProfileForTrafficMode(trafficMode)) {
            this.setActiveTrafficMode(trafficMode);
        }
    }

    public addProfileForTrafficMode(trafficMode: TrafficMode): void {
        if (this.hasProfileForTrafficMode(trafficMode)) {
            return;
        }

        const sourceProfile = this.profiles.controls[0];
        const copiedStops = sourceProfile ? this.mapStopsToStopTimes(sourceProfile) : [];

        const newProfile = this.createProfile({
            trafficMode,
            calculatedCommunicationVelocity: 50,
            customizedCommunicationVelocity: null,
            isCustomized: sourceProfile ? this.isCustomized(sourceProfile) : false,
            isDefault: false,
            travelTimeInSeconds: null,
            stops: copiedStops
        });

        this.profiles.push(newProfile);
        this.setActiveTrafficMode(trafficMode);
    }

    private mapStopsToStopTimes(profile: FormGroup): StopTime[] {
        return this.getStops(profile).controls.map((stop: FormGroup): StopTime => ({
            stopId: stop.controls['id'].value,
            stopName: stop.controls['name'].value,
            lon: stop.controls['lon'].value,
            lat: stop.controls['lat'].value,
            meters: stop.controls['meters'].value,
            calculatedSeconds: stop.controls['calculatedSeconds'].value,
            customizedSeconds: (stop.controls['customizedMinutes'].value ?? 0) * 60,
            bdot10k: stop.controls['bdot10k'].value
        }));
    }

    private getActiveProfile(): FormGroup | null {
        return this.profiles.controls.find(profile => profile.controls['trafficMode'].value === this.activeTrafficMode);
    }

    private applyMeasureResponseToCurrentStops(response: TripMeasure, profile?: FormGroup): void {
        if (!response.stops) {
            return;
        }

        this.isShiftingFollowingStopTimes = true;
        try {
            this.getStops(profile).controls.forEach((formGroup: FormGroup, index: number) => {
                const stopResponse: StopTime = response.stops[index];
                if (!stopResponse) {
                    return;
                }

                formGroup.controls["meters"].setValue(stopResponse.meters);
                formGroup.controls["calculatedSeconds"].setValue(stopResponse.calculatedSeconds);

                const customizedMinutes = Math.ceil((stopResponse.calculatedSeconds || 0) / 60);
                if (profile.controls['isCustomized'].value) {
                    if (!formGroup.controls["customizedMinutes"].value) {
                        formGroup.controls["customizedMinutes"].setValue(customizedMinutes);
                    }
                } else {
                    formGroup.controls["customizedMinutes"].setValue(customizedMinutes);
                }
            });

            if (response.travelTimeInSeconds != null) {
                profile.controls["travelTimeInSeconds"].setValue(response.travelTimeInSeconds);
            }
        } finally {
            this.isShiftingFollowingStopTimes = false;
        }
    }

    public validControl(controlName: string, group: FormGroup = this.modelForm): ValidationErrors | null {
        return this.isSubmited && group?.controls[controlName]?.errors;
    }

    public checkControlHasError(controlName: string, errorName: string, group: FormGroup = this.modelForm): boolean {
        return this.isSubmited && this.validControl(controlName, group)?.[errorName] || false;
    }

    public canCheckErrors(controlName: string, group: FormGroup = this.modelForm): boolean {
        return this.isSubmited && this.validControl(controlName, group) != null;
    }

    public scrollToFirstError(): void {
        setTimeout(() => {
            const invalidControl = document.querySelector('#form-errors') || document.querySelector('.text-danger');
            invalidControl?.scrollIntoView({behavior: 'smooth', block: 'center'});
        });
    }

    public onChangeVariantMode(tripMode: TripMode): void {
        // if (this.stops.length === 0) {
        //     if (tripMode === TripMode.Front) {
        //         this.map.flyTo([this.$tripVariants.route.originStop.lat, this.$tripVariants.route.originStop.lon], 15)
        //     }
        //     if (tripMode === TripMode.Back) {
        //         this.map.flyTo([this.$tripVariants.route.destinationStop.lat, this.$tripVariants.route.destinationStop.lon], 15)
        //     }
        // }
    }
}
