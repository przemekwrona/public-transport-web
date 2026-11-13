import {Injectable} from "@angular/core";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";
import {MatDialog} from "@angular/material/dialog";
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {size} from "lodash";
import {debounceTime, forkJoin, map, Observable, of, pairwise, startWith, Subject, tap} from "rxjs";
import {
    Point2D,
    Stop,
    StopTime,
    TrafficMode,
    TripDistanceMeasuresService,
    TripMeasure,
    TripProfile,
    TripsDetails
} from "../../../generated/public-transport-api";
import {
    AgencyStopNameDialogData,
    AgencyStopNameDialogResult,
    AgencyStopNameModalComponent
} from "../../agency-stops/agency-stop-name-modal/agency-stop-name-modal.component";
import {stopInsertIndex} from "../trip-editor/stop-insert-index";

@Injectable()
export class TripStopListStore {
    readonly trafficModes: TrafficMode[] = [TrafficMode.Normal, TrafficMode.Traffic];
    readonly forceRefreshSubject = new Subject<boolean>();
    readonly forceRefresh$ = this.forceRefreshSubject.asObservable();
    readonly geometryChange = new Subject<Point2D[]>();
    readonly focus$ = new Subject<[number, number]>();

    profiles: FormArray = this.formBuilder.array([], [Validators.required, Validators.minLength(1)]);
    geometry: Point2D[] = [];
    activeTrafficMode: TrafficMode = TrafficMode.Normal;
    editMode = false;
    isRefreshExpanded = false;
    isRefreshingExpanded = false;
    initialCenter: [number, number] = [50.613531, 20.743607];
    initialZoom = 14;
    focusTarget: [number, number] | null = null;

    private isShiftingFollowingStopTimes = false;

    constructor(
        private formBuilder: FormBuilder,
        private tripDistanceMeasuresService: TripDistanceMeasuresService,
        private dialog: MatDialog
    ) {
    }

    load(trip: TripsDetails): void {
        this.geometry = trip.geometry ?? [];
        const profileControls = (trip.tripProfiles || []).map((profile: TripProfile) => this.createProfile(profile));
        this.profiles = this.formBuilder.array(profileControls, [Validators.required, Validators.minLength(1)]);
        if (!this.hasProfileForTrafficMode(this.activeTrafficMode) && this.profiles.length > 0) {
            this.activeTrafficMode = this.profiles.at(0)?.get('trafficMode')?.value ?? TrafficMode.Normal;
        }
    }

    focus(center: [number, number]): void {
        this.focusTarget = center;
        this.initialCenter = center;
        this.initialZoom = 15;
        this.focus$.next(center);
    }

    clearPendingRefresh(): void {
        this.forceRefreshSubject.next(false);
    }

    getStops(profile: FormGroup): FormArray<FormGroup> {
        return profile.get('stops') as FormArray<FormGroup>;
    }

    getActiveProfile(): FormGroup | null {
        return this.profiles.controls.find(profile => profile.get('trafficMode')?.value === this.activeTrafficMode) as FormGroup | null;
    }

    addStop(stop: Stop): void {
        for (const profile of this.profiles.controls) {
            const profileGroup = profile as FormGroup;
            const stopControl = this.createStop(stop);
            this.subscribeToStopTimeChanges(profileGroup, stopControl);
            const stops = this.getStops(profileGroup);
            const insertAt = this.editMode
                ? stopInsertIndex(stops.controls.map(control => ({
                    lat: control.get('lat')?.value,
                    lon: control.get('lon')?.value
                })), {lat: stop.lat, lon: stop.lon})
                : stops.length;
            if (insertAt < stops.length) {
                stops.insert(insertAt, stopControl);
            } else {
                stops.push(stopControl);
            }
        }
    }

    drop(event: CdkDragDrop<string[]>): void {
        for (const profile of this.profiles.controls) {
            moveItemInArray(this.getStops(profile as FormGroup).controls, event.previousIndex, event.currentIndex);
        }
        this.approximateDistances();
        this.forceRefreshIn10seconds();
    }

    remove(index: number): void {
        for (const profile of this.profiles.controls) {
            this.getStops(profile as FormGroup).removeAt(index);
        }
        this.forceRefreshIn10seconds();
    }

    approximateDistances(): void {
        for (const profile of this.profiles.controls) {
            this.approximateDistance(profile as FormGroup);
        }
    }

    measureDistance(): Observable<TripMeasure> {
        const primaryProfile = this.getActiveProfile();
        const profiles = this.profiles.controls.filter(profile =>
            profile === primaryProfile || this.getStops(profile as FormGroup).length > 1
        ) as FormGroup[];
        if (profiles.length === 0) {
            return of({} as TripMeasure);
        }

        return forkJoin(profiles.map(profile =>
            this.tripDistanceMeasuresService.measureDistance(this.buildTripsMeasureRequest(profile)).pipe(
                tap(response => {
                    this.applyMeasureResponseToCurrentStops(response, profile);
                    if (profile === primaryProfile && response.geometry) {
                        this.geometry = response.geometry;
                        this.geometryChange.next(response.geometry);
                    }
                })
            )
        )).pipe(
            map(responses => {
                const primaryIndex = profiles.indexOf(primaryProfile as FormGroup);
                return responses[primaryIndex >= 0 ? primaryIndex : 0];
            })
        );
    }

    refreshMap(): void {
        this.measureDistance().subscribe();
        this.isRefreshingExpanded = false;
        this.isRefreshExpanded = true;
    }

    openDialogEditStop(stopTime: FormGroup): void {
        const stopId = stopTime.get('id')?.value;
        if (stopId == null) {
            return;
        }
        const data: AgencyStopNameDialogData = {
            stopId,
            name: stopTime.get('name')?.value ?? '',
            lat: stopTime.get('lat')?.value,
            lon: stopTime.get('lon')?.value
        };

        this.dialog.open(AgencyStopNameModalComponent, {
            data,
            width: '960px',
            maxWidth: '95vw'
        }).afterClosed().subscribe((result: AgencyStopNameDialogResult | undefined) => {
            if (!result) {
                return;
            }
            this.profiles.controls.forEach(profile => {
                this.getStops(profile as FormGroup).controls.forEach(stop => {
                    if (stop.get('id')?.value === result.stopId) {
                        stop.get('name')?.setValue(result.name);
                    }
                });
            });
        });
    }

    isCustomized(profile: FormGroup): boolean {
        return !!this.isCustomizedControl(profile).value;
    }

    isCustomizedControl(profile: FormGroup): FormControl {
        return profile.get('isCustomized') as FormControl;
    }

    customizedCommunicationVelocity(profile: FormGroup): number | null {
        return profile.get('customizedCommunicationVelocity')?.value;
    }

    hasProfileForTrafficMode(trafficMode: TrafficMode): boolean {
        return this.profiles.controls.some(profile => profile.get('trafficMode')?.value === trafficMode);
    }

    selectProfileByTrafficMode(trafficMode: TrafficMode): void {
        if (this.hasProfileForTrafficMode(trafficMode)) {
            this.activeTrafficMode = trafficMode;
        }
    }

    addProfileForTrafficMode(trafficMode: TrafficMode): void {
        if (this.hasProfileForTrafficMode(trafficMode)) {
            return;
        }

        const sourceProfile = this.profiles.controls[0] as FormGroup | undefined;
        const copiedStops = sourceProfile ? this.mapStopsToStopTimes(sourceProfile) : [];
        const newProfile = this.createProfile({
            trafficMode,
            calculatedCommunicationVelocity: 50,
            customizedCommunicationVelocity: sourceProfile && this.isCustomized(sourceProfile) ? this.customizedCommunicationVelocity(sourceProfile) : 50,
            isCustomized: sourceProfile ? this.isCustomized(sourceProfile) : false,
            isDefault: false,
            travelTimeInSeconds: null,
            stops: copiedStops
        });

        this.profiles.push(newProfile);
        this.activeTrafficMode = trafficMode;
    }

    trafficModeIcon(trafficMode: TrafficMode): string {
        return trafficMode === TrafficMode.Traffic ? 'traffic_jam' : 'directions_bus';
    }

    buildTripProfiles(): TripProfile[] {
        return this.profiles.controls.map((profileFormGroup: FormGroup): TripProfile => {
            const profile: TripProfile = {};
            profile.trafficMode = profileFormGroup.get('trafficMode')?.value;
            profile.travelTimeInSeconds = profileFormGroup.get('travelTimeInSeconds')?.value;
            profile.calculatedCommunicationVelocity = profileFormGroup.get('calculatedCommunicationVelocity')?.value;
            profile.customizedCommunicationVelocity = profileFormGroup.get('customizedCommunicationVelocity')?.value;
            profile.isDefault = profileFormGroup.get('isDefault')?.value;
            profile.isCustomized = profileFormGroup.get('isCustomized')?.value;
            profile.stops = this.getStops(profileFormGroup).controls.map((stopTimeFormGroup: FormGroup): StopTime => {
                const stopTime: StopTime = {};
                stopTime.stopId = stopTimeFormGroup.get('id')?.value;
                stopTime.stopName = stopTimeFormGroup.get('name')?.value;
                stopTime.lat = stopTimeFormGroup.get('lat')?.value;
                stopTime.lon = stopTimeFormGroup.get('lon')?.value;
                stopTime.calculatedSeconds = stopTimeFormGroup.get('calculatedSeconds')?.value;
                if (profileFormGroup.get('isCustomized')?.value) {
                    stopTime.customizedSeconds = 60 * stopTimeFormGroup.get('customizedMinutes')?.value;
                } else {
                    stopTime.customizedSeconds = stopTimeFormGroup.get('calculatedSeconds')?.value;
                }
                stopTime.meters = stopTimeFormGroup.get('meters')?.value;
                return stopTime;
            });
            return profile;
        });
    }

    private createStop(stop: Stop): FormGroup {
        return this.formBuilder.group({
            id: [stop.id],
            name: [stop.name],
            lon: [stop.lon],
            lat: [stop.lat],
            meters: [null],
            calculatedSeconds: [null],
            customizedMinutes: [null, [Validators.required, Validators.min(0)]],
            bdot10k: [stop.isBdot10k]
        });
    }

    private createStopFromStopTimeModel(stop: StopTime): FormGroup {
        return this.formBuilder.group({
            id: [stop.stopId],
            name: [stop.stopName],
            lon: [stop.lon],
            lat: [stop.lat],
            meters: [stop.meters],
            calculatedSeconds: [stop.calculatedSeconds],
            customizedMinutes: [stop.customizedSeconds / 60, [Validators.required, Validators.min(0)]],
            bdot10k: [stop.bdot10k]
        });
    }

    private createProfile(profile: Partial<TripProfile> = {}): FormGroup {
        const stopControls = (profile.stops || []).map((stop: StopTime) => this.createStopFromStopTimeModel(stop));
        const profileControl = this.formBuilder.group({
            trafficMode: [profile.trafficMode ?? TrafficMode.Normal],
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

    private subscribeToStopTimeChanges(profileControl: FormGroup, stopControl: FormGroup): void {
        const customizedMinutesControl = stopControl.get('customizedMinutes')!;
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

    private onChangeDeparture(profile: FormGroup, no: number, timeDifference: number): void {
        if (!timeDifference) {
            return;
        }
        this.isShiftingFollowingStopTimes = true;
        try {
            this.getStops(profile).controls.forEach((stopTimeControl: FormGroup, index: number) => {
                if (no < index) {
                    const customizedMinutesControl = stopTimeControl.get('customizedMinutes');
                    const value = (customizedMinutesControl?.value ?? 0) + timeDifference;
                    customizedMinutesControl?.setValue(value);
                }
            });
        } finally {
            this.isShiftingFollowingStopTimes = false;
        }
    }

    private approximateDistance(profile: FormGroup): void {
        const trips = this.buildTripsMeasureRequest(profile);
        if (trips.stops.length <= 1) {
            return;
        }
        this.tripDistanceMeasuresService.approximateDistance(trips).subscribe(response => {
            this.applyMeasureResponseToCurrentStops(response, profile);
            if (response.geometry) {
                this.geometryChange.next(response.geometry);
            }
        });
    }

    private buildTripsMeasureRequest(profile: FormGroup | null = this.getActiveProfile()): TripMeasure {
        const measuredProfile = profile ?? this.getActiveProfile();
        const tripMeasure: TripMeasure = {
            velocity: measuredProfile?.get('calculatedCommunicationVelocity')?.value
        };
        tripMeasure.stops = measuredProfile ? this.getStops(measuredProfile).controls.map((stop: FormGroup): StopTime => {
            const stopTime: StopTime = {};
            stopTime.stopId = stop.get('id')?.value;
            stopTime.stopName = stop.get('name')?.value;
            stopTime.lat = stop.get('lat')?.value;
            stopTime.lon = stop.get('lon')?.value;
            stopTime.calculatedSeconds = stop.get('calculatedSeconds')?.value;
            if (measuredProfile.get('isCustomized')?.value) {
                stopTime.customizedSeconds = 60 * stop.get('customizedMinutes')?.value;
            } else {
                stopTime.customizedSeconds = stop.get('calculatedSeconds')?.value;
            }
            stopTime.meters = stop.get('meters')?.value;
            return stopTime;
        }) : [];
        return tripMeasure;
    }

    private applyMeasureResponseToCurrentStops(response: TripMeasure, profile: FormGroup): void {
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
                formGroup.get('meters')?.setValue(stopResponse.meters);
                formGroup.get('calculatedSeconds')?.setValue(stopResponse.calculatedSeconds);
                const customizedMinutes = Math.ceil((stopResponse.calculatedSeconds || 0) / 60);
                if (profile.get('isCustomized')?.value) {
                    if (!formGroup.get('customizedMinutes')?.value) {
                        formGroup.get('customizedMinutes')?.setValue(customizedMinutes);
                    }
                } else {
                    formGroup.get('customizedMinutes')?.setValue(customizedMinutes);
                }
            });
            if (response.travelTimeInSeconds != null) {
                profile.get('travelTimeInSeconds')?.setValue(response.travelTimeInSeconds);
            }
        } finally {
            this.isShiftingFollowingStopTimes = false;
        }
    }

    private mapStopsToStopTimes(profile: FormGroup): StopTime[] {
        return this.getStops(profile).controls.map((stop: FormGroup): StopTime => ({
            stopId: stop.get('id')?.value,
            stopName: stop.get('name')?.value,
            lon: stop.get('lon')?.value,
            lat: stop.get('lat')?.value,
            meters: stop.get('meters')?.value,
            calculatedSeconds: stop.get('calculatedSeconds')?.value,
            customizedSeconds: (stop.get('customizedMinutes')?.value ?? 0) * 60,
            bdot10k: stop.get('bdot10k')?.value
        }));
    }

    forceRefreshIn10seconds(): void {
        this.isRefreshingExpanded = true;
        this.isRefreshExpanded = false;
        this.forceRefreshSubject.next(true);
    }
}
