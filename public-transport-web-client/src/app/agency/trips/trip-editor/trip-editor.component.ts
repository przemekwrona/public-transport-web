import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {size} from "lodash";
import {
    ErrorResponse,
    Point2D,
    RouteDetails, RouteId,
    Stop,
    TrafficMode,
    TripId,
    TripMeasure,
    TripMode, TripProfile,
    TripProfileService,
    TripsDetails,
    TripService,
    UpdateTripDetailsRequest,
    UpdateTripProfileRequest
} from "../../../generated/public-transport-api";
import {ActivatedRoute, Data, Router} from "@angular/router";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {map, Observable, pairwise, switchMap} from "rxjs";
import {TripEditorComponentMode} from "./trip-editor-component-mode";
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
import {TripStopListStore} from "../trip-map/trip-stop-list.store";

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
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false,
    providers: [TripStopListStore]
})
export class TripEditorComponent implements OnInit {
    public tripModeSelectValue = TripMode;
    public tripEditorComponentMode: TripEditorComponentMode;

    public readonly trafficModes: TrafficMode[] = [TrafficMode.Normal, TrafficMode.Traffic];

    public $tripVariants: RouteDetails = {};
    public isMainVariantDescriptionExpanded = false;

    public modelForm: FormGroup;
    public isSubmited: boolean = false;
    public isSaving: boolean = false;
    private submitScope: 'details' | 'stops' | 'all' = 'all';
    private readonly detailControlNames = ['isMainVariant', 'tripVariantMode', 'variantDesignation', 'variantDescription', 'headsign'];

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

    constructor(private tripService: TripService, private tripProfileService: TripProfileService, private agencyStorageService: AgencyStorageService, private router: Router, private _route: ActivatedRoute, private notificationService: NotificationService, private formBuilder: FormBuilder, private tripIdExistenceValidator: TripIdExistenceValidator, private stops: TripStopListStore) {
    }

    get geometry(): Point2D[] {
        return this.stops.geometry;
    }

    set geometry(value: Point2D[]) {
        this.stops.geometry = value ?? [];
    }

    get activeTrafficMode(): TrafficMode {
        return this.stops.activeTrafficMode;
    }

    set activeTrafficMode(value: TrafficMode) {
        this.stops.activeTrafficMode = value;
    }

    ngOnInit(): void {
        this.routeCode = this.paramFromRoute('routeCode') ?? '';
        this.tripCode = this.paramFromRoute('tripCode') ?? '';
        this.tripEditorComponentMode = this._route.snapshot.data['mode'];
        this.stops.editMode = this.tripEditorComponentMode === TripEditorComponentMode.EDIT;
        this.$tripVariants = this.routeDetailsFromSnapshot() ?? this.$tripVariants;

        this._route.data.subscribe((data: Data) => {
            this.tripEditorComponentMode = data['mode'];
            this.stops.editMode = data['mode'] === TripEditorComponentMode.EDIT;
        });

        const resolvedTripDetails: TripsDetails = this._route.snapshot.data['trip'];
        const initialTripMode = this.resolveTripVariantMode(resolvedTripDetails);

        this.modelForm = this.formBuilder.group({
                isMainVariant: [true, [Validators.required]],
                tripVariantMode: [initialTripMode, [Validators.required]],

                variantDesignation: ['', [Validators.required]],
                variantDescription: ['', [Validators.required]],

                headsign: ['', [Validators.required]],

                profiles: this.stops.profiles
            },
            {
                asyncValidators: this.tripIdExistenceValidator.variantExistsValidator(() => ({
                    routeId: this.currentRouteId(),
                    tripId: this.currentTripId(),
                    current: this.currentTripSelection()
                }))
            });

        this.modelForm.get('isMainVariant').valueChanges.pipe(pairwise()).subscribe(([prev, next]: [boolean, boolean]) => this.clickIsMainVariant(next));
        this.modelForm.get('tripVariantMode').valueChanges.subscribe((value: TripMode) => this.onChangeVariantMode(value));
        this.prepareCreateMapView();

        this._route.data.pipe(map((data: Data) => data['trip'])).subscribe((tripDetails: TripsDetails) => {
            this.tripDetails = tripDetails;
            this.tripVariantModeControl.setValue(this.resolveTripVariantMode(tripDetails));

            this.modelForm.controls['isMainVariant'].setValue(tripDetails.isMainVariant);

            this.modelForm.controls['variantDesignation'].setValue(tripDetails.variantDesignation);
            this.modelForm.controls['variantDescription'].setValue(tripDetails.variantDescription);

            if (tripDetails.isMainVariant) {
                this.modelForm.controls["variantDesignation"].setValidators(null);
                this.modelForm.controls["variantDescription"].setValidators(null);
            }

            this.modelForm.controls['headsign'].setValue(tripDetails.headsign);

            this.stops.load(tripDetails);
            this.modelForm.setControl('profiles', this.stops.profiles);

            this._route.data.pipe(map((data: Data) => data['routeDetails'])).subscribe((routeDetails: RouteDetails) => {
                if (routeDetails?.route) {
                    this.$tripVariants = routeDetails;
                }

                if (this.tripEditorComponentMode === TripEditorComponentMode.CREATE) {
                    if (size(this.$tripVariants?.trips) === 0) {
                        this.modelForm.controls["isMainVariant"].setValue(true);
                        this.modelForm.controls["variantDesignation"].setValidators(null);
                        this.modelForm.controls["variantDescription"].setValidators(null);

                        this.modelForm.controls["headsign"].setValue(this.headsignStop()?.name);
                    }

                    this.prepareCreateMapView();
                }

            });
        });
    }

    private resolveTripVariantMode(tripDetails?: TripsDetails): TripMode {
        return tripDetails?.tripId?.variantMode ?? TripMode.Front;
    }

    private currentTripSelection(): { routeId?: RouteId, designation?: string, tripMode?: TripMode, isMainVariant?: boolean } | undefined {
        if (this.tripEditorComponentMode !== TripEditorComponentMode.EDIT || !this.tripDetails) {
            return undefined;
        }
        return {
            routeId: this.tripDetails.tripId?.routeId ?? this.currentRouteId(),
            designation: this.tripDetails.variantDesignation,
            tripMode: this.resolveTripVariantMode(this.tripDetails),
            isMainVariant: !!this.tripDetails.isMainVariant
        };
    }

    private currentTripId(): TripId | undefined {
        if (this.tripEditorComponentMode !== TripEditorComponentMode.EDIT) {
            return undefined;
        }
        const routeId = this.tripDetails?.tripId?.routeId ?? this.currentRouteId();
        const tripCode = this.tripDetails?.tripId?.tripCode ?? this.tripCode;
        if (!routeId && !tripCode) {
            return undefined;
        }
        return {
            ...this.tripDetails?.tripId,
            routeId,
            tripCode
        };
    }

    private paramFromRoute(name: string): string | null {
        let route: ActivatedRoute | null = this._route;
        while (route) {
            const value = route.snapshot.paramMap.get(name);
            if (value) {
                return value;
            }
            route = route.parent;
        }
        return null;
    }

    private currentRouteId(): RouteId | undefined {
        const routeId = this.$tripVariants?.route?.routeId ?? this.tripDetails?.tripId?.routeId;
        if (!routeId && !this.routeCode) {
            return undefined;
        }
        return {
            ...routeId,
            routeCode: routeId?.routeCode ?? this.routeCode
        };
    }

    private routeLineAndName(): string {
        const routeId = this.$tripVariants?.route?.routeId ?? this.tripDetails?.tripId?.routeId;
        return [routeId?.line, routeId?.name].filter(value => !!value).join(' ');
    }


    get tripVariantModeControl(): FormControl {
        return this.modelForm.get('tripVariantMode') as FormControl;
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

    public get isEdit(): boolean {
        return this.tripEditorComponentMode === TripEditorComponentMode.EDIT;
    }

    get formValidationErrors(): AllValidationErrors[] {
        if (!this.modelForm) {
            return [];
        }
        const errors = FormUtils.getFormValidationErrors(this.modelForm);
        const scoped = this.submitScope === 'all'
            ? errors
            : errors.filter(error => this.submitScope === 'details' ? this.isDetailError(error) : this.isStopError(error));
        if (this.isSubmited) {
            return scoped;
        }
        return scoped.filter(error => error.errorName === 'variantExists');
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
                return this.variantExistsMessage();
            default:
                return `${label}: ${error.errorName}`;
        }
    }

    public variantExistsMessage(): string {
        const route = this.currentRouteId();
        const routeLabel = [route?.line, route?.name].filter(value => !!value).join(' ');
        if (this.isMainVariant()) {
            return `Trasa ${routeLabel} posiada już wariant podstawowy.`;
        }
        const designation = this.modelForm?.controls['variantDesignation']?.value;
        return `Trasa ${routeLabel} posiada już kurs oznaczony symbolem ${designation}.`;
    }

    private getControlLabel(controlName: string): string {
        const customizedMinutesMatch = controlName.match(/(?:^|\.)stops\[(\d+)\]\.customizedMinutes$/);
        if (customizedMinutesMatch) {
            return `Czas przejazdu na przystanku ${Number(customizedMinutesMatch[1]) + 1}`;
        }

        const fieldName = controlName.includes('.') ? controlName.split('.').pop()! : controlName;

        const labels: Record<string, string> = {
            isMainVariant: 'Wariant podstawowy',
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

    public saveDetails(): void {
        if (!this.beginSave('details')) {
            return;
        }

        const agency = this.agencyStorageService.getInstance();
        this.tripService.updateTrip(agency, this.routeCode, this.tripCode, this.buildTripMetadataRequest()).subscribe({
            next: () => {
                this.isSaving = false;
                this.notificationService.showSuccess(`Szczegóły linii ${this.routeLineAndName()} zostały zapisane`);
            },
            error: (response: HttpErrorResponse) => this.showHttpError(response)
        });
    }

    public saveStopList(): void {
        if (!this.beginSave('stops')) {
            return;
        }

        this.measureDistance().subscribe({
            next: () => {
                this.stops.clearPendingRefresh();
                const agency = this.agencyStorageService.getInstance();
                this.tripProfileService.updateTripProfiles(agency, this.routeCode, this.tripCode, this.buildTripProfileRequest()).subscribe({
                    next: () => {
                        this.isSaving = false;
                        this.notificationService.showSuccess('Lista przystanków została zapisana');
                    },
                    error: (response: HttpErrorResponse) => this.showHttpError(response)
                });
            },
            error: (response: HttpErrorResponse) => this.showHttpError(response)
        });
    }

    public saveEverything(): void {
        if (!this.beginSave('all')) {
            return;
        }

        this.measureDistance().subscribe({
            next: () => {
                this.stops.clearPendingRefresh();

                if (this.tripEditorComponentMode == TripEditorComponentMode.CREATE) {
                    const tripDetailsRequest: UpdateTripDetailsRequest = this.buildCreateOrUpdateTripRequest();
                    this.tripService.createTrip(this.agencyStorageService.getInstance(), this.$tripVariants.route.routeId.routeCode, tripDetailsRequest).subscribe({
                        next: () => {
                            this.isSaving = false;
                            this.notificationService.showSuccess(`Linia ${this.routeLineAndName()} została utworzona`);
                            this.router.navigate(['/agency/routes', this.routeCode, 'trips', 'variants'], { replaceUrl: true }).then(() => {
                                window.scrollTo({ top: 0, behavior: 'smooth' });
                            });
                        },
                        error: (response: HttpErrorResponse) => this.showHttpError(response)
                    });
                    return;
                }

                const agency = this.agencyStorageService.getInstance();
                this.tripService.updateTrip(agency, this.routeCode, this.tripCode, this.buildTripMetadataRequest()).pipe(
                    switchMap(() => this.tripProfileService.updateTripProfiles(agency, this.routeCode, this.tripCode, this.buildTripProfileRequest()))
                ).subscribe({
                    next: () => {
                        this.isSaving = false;
                        this.notificationService.showSuccess(`Linia ${this.routeLineAndName()} została zaktualizowana`);
                        this.router.navigate(['/agency/routes', this.routeCode, 'trips', 'variants'], {}).then(() => {
                            window.scrollTo({ top: 0, behavior: 'smooth' });
                        });
                    },
                    error: (response: HttpErrorResponse) => this.showHttpError(response)
                });
            },
            error: (response: HttpErrorResponse) => this.showHttpError(response)
        });
    }

    private beginSave(scope: 'details' | 'stops' | 'all'): boolean {
        this.submitScope = scope;
        this.isSubmited = true;
        if (this.modelForm.pending) {
            this.notificationService.showError('Sprawdzanie czy kurs już istnieje');
            return false;
        }
        if ((scope === 'details' && this.detailsInvalid()) || (scope === 'stops' && this.stopsInvalid()) || (scope === 'all' && this.modelForm.invalid)) {
            this.notificationService.showError('Formularz jest niepoprawny uzupełnij braki');
            this.scrollToFirstError();
            return false;
        }
        this.isSaving = true;
        return true;
    }

    private detailsInvalid(): boolean {
        return this.detailControlNames.some(name => this.modelForm.get(name)?.invalid) || !!this.modelForm.errors?.['variantExists'];
    }

    private stopsInvalid(): boolean {
        return this.profiles.invalid;
    }

    private isDetailError(error: AllValidationErrors): boolean {
        return error.controlName === 'form' || this.detailControlNames.includes(error.controlName);
    }

    private isStopError(error: AllValidationErrors): boolean {
        return error.controlName === 'profiles' || error.controlName.startsWith('profiles');
    }

    private showHttpError(response: HttpErrorResponse): void {
        this.isSaving = false;
        const payload: ErrorResponse = response.error;
        this.notificationService.showError(payload?.message || payload?.errorCode);
    }

    private buildCreateOrUpdateTripRequest() {
        const routeId: RouteId = {};
        routeId.name = this.$tripVariants.route.routeId.name;
        routeId.line = this.$tripVariants.route.routeId.line;
        routeId.version = this.$tripVariants.route.routeId.version;
        routeId.routeCode = this.$tripVariants.route.routeId.routeCode;

        const tripDetailsRequest: UpdateTripDetailsRequest = {};

        const updatedTripId: TripId = {};
        updatedTripId.routeId = routeId;
        updatedTripId.variantMode = this.modelForm.controls['tripVariantMode'].value;
        updatedTripId.tripCode = this.currentTripId()?.tripCode;

        tripDetailsRequest.body = {};
        tripDetailsRequest.body.tripId = updatedTripId;
        tripDetailsRequest.body.isMainVariant = this.modelForm.controls['isMainVariant'].value
        // tripDetailsRequest.body.isCustomized = this.modelForm.controls['isCustomized'].value

        tripDetailsRequest.body.variantDesignation = this.modelForm.controls['variantDesignation'].value
        tripDetailsRequest.body.variantDescription = this.modelForm.controls['variantDescription'].value

        tripDetailsRequest.body.headsign = this.modelForm.controls['headsign'].value
        tripDetailsRequest.body.tripProfiles = this.buildTripProfiles();
        tripDetailsRequest.body.geometry = this.geometry;
        return tripDetailsRequest;
    }

    private buildTripMetadataRequest(): UpdateTripDetailsRequest {
        const tripDetailsRequest = this.buildCreateOrUpdateTripRequest();
        tripDetailsRequest.body.tripProfiles = undefined;
        tripDetailsRequest.body.geometry = undefined;
        return tripDetailsRequest;
    }

    private buildTripProfileRequest(): UpdateTripProfileRequest {
        return {
            tripProfiles: this.buildTripProfiles(),
            geometry: this.geometry
        };
    }

    private buildTripProfiles(): TripProfile[] {
        return this.stops.buildTripProfiles();
    }

    public measureDistance(): Observable<TripMeasure> {
        return this.stops.measureDistance();
    }

    public getLastStop(profile: FormGroup): FormGroup | null {
        const stops = this.getStops(profile);
        if (!stops || stops.length === 0) {
            return null;
        }
        return stops.at(stops.length - 1);
    }

    clickIsMainVariant(isMainVariant: boolean): void {
        const variantDesignationControl: AbstractControl = this.modelForm.get('variantDesignation');
        const variantDescriptionControl: AbstractControl = this.modelForm.get('variantDescription');

        if (isMainVariant) {
            variantDesignationControl.setValue('');
            variantDesignationControl.setValidators([]);
            variantDesignationControl.updateValueAndValidity({emitEvent: false});

            variantDescriptionControl.setValue('');
            variantDescriptionControl.setValidators([]);
            variantDescriptionControl.updateValueAndValidity({emitEvent: false});
        } else {
            variantDesignationControl.setValue('');
            variantDesignationControl.setValidators([Validators.required]);
            variantDesignationControl.updateValueAndValidity({emitEvent: false});

            variantDescriptionControl.setValue('');
            variantDescriptionControl.setValidators([Validators.required]);
            variantDescriptionControl.updateValueAndValidity({emitEvent: false});
        }
    }

    public addProfileForTrafficMode(trafficMode: TrafficMode): void {
        this.stops.addProfileForTrafficMode(trafficMode);
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


    public validControl(controlName: string, group: FormGroup = this.modelForm): ValidationErrors | null {
        if (!this.isSubmited || !this.errorApplies(controlName, group)) {
            return null;
        }
        return group?.controls[controlName]?.errors ?? null;
    }

    private errorApplies(controlName: string, group: FormGroup): boolean {
        const onProfile = group !== this.modelForm || controlName === 'profiles';
        if (this.submitScope === 'stops') {
            return onProfile;
        }
        if (this.submitScope === 'details') {
            return !onProfile;
        }
        return true;
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
        if (this.hasAnyStops()) {
            return;
        }
        const center = this.toLatLng(this.startStop(tripMode));
        if (center) {
            this.stops.focus(center);
        }
    }

    private hasAnyStops(): boolean {
        return this.profiles?.controls?.some(profile => this.getStops(profile).length > 0) ?? false;
    }

    private headsignStop(): Stop | undefined {
        return this.isFrontVariantMode()
            ? this.$tripVariants?.route?.destinationStop
            : this.$tripVariants?.route?.originStop;
    }

    private routeDetailsFromSnapshot(): RouteDetails | undefined {
        return this._route.pathFromRoot
            .map(route => route.snapshot.data['routeDetails'] as RouteDetails | undefined)
            .find(details => !!details?.route);
    }

    private startStop(tripMode: TripMode = this.modelForm?.controls['tripVariantMode']?.value): Stop | undefined {
        const route = this.$tripVariants?.route;
        if (!route) {
            return undefined;
        }

        return tripMode === TripMode.Back ? route.destinationStop : route.originStop;
    }

    private toLatLng(stop?: Stop): [number, number] | undefined {
        if (stop?.lat == null || stop?.lon == null) {
            return undefined;
        }

        return [stop.lat, stop.lon];
    }

    private prepareCreateMapView(): void {
        if (this.tripEditorComponentMode !== TripEditorComponentMode.CREATE || this.hasAnyStops()) {
            return;
        }
        const center = this.toLatLng(this.startStop());
        if (center) {
            this.stops.focus(center);
        }
    }
}
