import {Component, OnInit} from '@angular/core';
import {
    BrigadeService, BrigadeTrip, CalendarSymbolId, ErrorResponse,
    GetAllTripsResponse, GetBrigadeDetailsResponse, GetCalendarSymbolsResponse,
    Trip, TripService
} from "../../../generated/public-transport-api";
import {CdkDrag, CdkDragDrop, CdkDragEnter, CdkDragExit, moveItemInArray} from "@angular/cdk/drag-drop";
import {BrigadeModel} from "./brigade-editor.model";
import moment, {Moment} from "moment";
import {first, last} from "lodash";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {ActivatedRoute, Router} from "@angular/router";
import {BrigadeEditorComponentMode} from "./brigade-editor-component-mode";
import {AgencyStorageService} from "../../../auth/agency-storage.service";
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {MatDialog} from "@angular/material/dialog";
import {
    BrigadeGroupCreatorModalComponent
} from "../brigade-group-creator-modal/brigade-group-creator-modal.component";

@Component({
    selector: 'app-brigade-editor',
    templateUrl: './brigade-editor.component.html',
    styleUrl: './brigade-editor.component.scss',
    animations: [
        trigger('fadeBrigadeAnimation', [
            state('in', style({opacity: 1})),
            transition(':enter', [style({opacity: 0}), animate(500)]),
            transition(':leave', animate(500, style({opacity: 0})))
        ]),
        trigger('fadeDragAndDropAnimation', [
            state('in', style({opacity: 1})),
            transition(':enter', [style({opacity: 0}), animate('500ms 600ms ease-in')])
        ])
    ],
    standalone: false
})
export class BrigadeEditorComponent implements OnInit {

    private componentMode: BrigadeEditorComponentMode = null;

    public tripsResponse: GetAllTripsResponse = {lines: []};
    public calendarsResponse: GetCalendarSymbolsResponse = {};
    public brigaderResponse: GetBrigadeDetailsResponse= {} as GetBrigadeDetailsResponse;

    public queryBrigadeName: string = '';
    public calendarId: CalendarSymbolId = {};
    public brigadeItems: BrigadeModel[] = [];
    public isEntered: boolean = false;

    public saveError: ErrorResponse | null = null;

    public modelForm: FormGroup;
    public isSubmitted: boolean = false;

    get brigadeNameControl(): FormGroup {
        return this.modelForm.get('brigadeName') as FormGroup;
    }

    constructor(private brigadeService: BrigadeService, private tripService: TripService, private agencyStorageService: AgencyStorageService, private _route: ActivatedRoute, private _router: Router, private formBuilder: FormBuilder, private dialog: MatDialog) {
        this.modelForm = this.formBuilder.group({
            brigadeName: ['', [Validators.required]]
        });
    }

    ngOnInit(): void {
        this._route.queryParams.subscribe(params => this.queryBrigadeName = params['name']);

        this._route.data.subscribe(data => this.componentMode = data['mode']);
        this._route.data.subscribe(data => {
            const getBrigadeDetailsResponse: GetBrigadeDetailsResponse = data['brigade'];

            this.brigaderResponse = getBrigadeDetailsResponse;
            // this.calendarId = brigadeV2?.calendarSymbolId;
            this.getBrigadeName().patchValue(this.brigaderResponse.brigade.brigadeName);

            this.brigadeItems = data['brigade'];
            this.brigadeItems = (data['brigade']?.trips || []).map((trip: BrigadeTrip) => {
                const brigadeModel: BrigadeModel = {} as BrigadeModel;
                brigadeModel.line = trip.tripId.routeId.line;
                brigadeModel.name = trip.tripId.routeId.name;
                brigadeModel.variant = trip.tripId.variantName;
                brigadeModel.mode = trip.tripId.variantMode;

                brigadeModel.origin = trip.origin;
                brigadeModel.destination = trip.destination;

                // brigadeModel.travelTimeInSeconds;
                brigadeModel.departureTime = moment().startOf('day').add(trip.departureTime, 'seconds').format('HH:mm');
                brigadeModel.travelTimeInSeconds = trip.travelTimeInSeconds;

                return brigadeModel;
            });

            this.tripService.getTripsByRouteAndFilterLineOrName(this.agencyStorageService.getInstance(), getBrigadeDetailsResponse.brigade.defaultRouteCode).subscribe((response: GetAllTripsResponse) => response === null ? {
                lines: []
            } : this.tripsResponse = response);
        });

        this._route.data.subscribe(data => this.calendarsResponse = data['calendars']);
    }

    drop(event: CdkDragDrop<Trip[]>) {
        if (event.previousContainer === event.container) {
            moveItemInArray(this.brigadeItems, event.previousIndex, event.currentIndex);
        } else {
            this.isEntered = false;

            const previousBrigadeModel: Trip = event.previousContainer.data[event.previousIndex];

            const brigadeModel: BrigadeModel = {} as BrigadeModel;
            brigadeModel.line = previousBrigadeModel.line
            brigadeModel.name = previousBrigadeModel.name
            brigadeModel.variant = previousBrigadeModel.variant
            brigadeModel.mode = previousBrigadeModel.mode;
            brigadeModel.origin = previousBrigadeModel.origin
            brigadeModel.destination = previousBrigadeModel.destination
            brigadeModel.isMainVariant = previousBrigadeModel.isMainVariant
            brigadeModel.variantDescription = previousBrigadeModel.variantDescription

            if (this.brigadeItems.length === 0) {
                brigadeModel.departureTime = moment().format('HH:mm');
            } else {
                const lastBrigadeItems = last(this.brigadeItems);
                brigadeModel.departureTime = this.getArrivalTime(lastBrigadeItems).format("HH:mm");
            }

            brigadeModel.travelTimeInSeconds = previousBrigadeModel.travelTimeInSeconds

            if (event.currentIndex === 0 && this.brigadeItems.length > 0) {
                const firstBrigadeItems = first(this.brigadeItems);
                brigadeModel.departureTime = this.getDepartureTime(firstBrigadeItems).subtract(firstBrigadeItems.travelTimeInSeconds, 'seconds').startOf('minute').format('HH:mm');
            }

            this.brigadeItems.splice(event.currentIndex, 0, brigadeModel);
        }
    }

    getDepartureTime(trip: BrigadeModel): moment.Moment {
        return moment(trip.departureTime, 'HH:mm');
    }

    getArrivalTime(trip: BrigadeModel): moment.Moment {
        return moment(trip.departureTime, 'HH:mm').add(trip.travelTimeInSeconds, 'seconds').startOf('minute').add(1, 'minute');
    }

    entered(event: CdkDragEnter<any[]>) {
        this.isEntered = true;
    }

    exited(event: CdkDragExit) {
        this.isEntered = false;
    }

    /** Predicate function that doesn't allow items to be dropped into a list. */
    noReturnPredicate(): boolean {
        return false;
    }

    oneElementPredicate(item: CdkDrag<Trip>): boolean {
        return true;
    }

    remove(brigadeIndex: number): void {
        this.brigadeItems.splice(brigadeIndex, 1);
    }

    public addCalendar(): void {
        const brigade = this.brigaderResponse.brigade;
        const dialogRef = this.dialog.open(BrigadeGroupCreatorModalComponent, {
            data: {
                calendarCode: brigade.calendarCode,
                brigadeCode: brigade.brigadeCode,
                brigadeName: brigade.brigadeName
            }
        });

        dialogRef.afterClosed().subscribe((response) => {
            if (!response) {
                return;
            }

            const instance: string = this.agencyStorageService.getInstance();
            this.brigadeService.getBrigadeDetails(instance, brigade.brigadeCode)
                .subscribe(details => this.brigaderResponse = details);
        });
    }

    compareByCalendarId(current: CalendarSymbolId, option: CalendarSymbolId): boolean {
        return current && option
            ? current.calendarItemId.code === option.calendarItemId.code
            && current.symbol === option.symbol
            : current === option;
    }

    public getControl(control: string): FormControl {
        return this.modelForm.get(control) as FormControl;
    }

    public getBrigadeName(): FormControl<string> {
        return this.getControl("brigadeName") as FormControl<string>;
    }

}
