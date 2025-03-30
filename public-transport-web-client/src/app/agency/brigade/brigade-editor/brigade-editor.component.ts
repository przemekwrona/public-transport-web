import {Component, OnInit} from '@angular/core';
import {BrigadeService} from "../brigade.service";
import {
    BrigadeBody, BrigadePatchBody,
    BrigadePayload,
    BrigadeTrip,
    GetAllTripsResponse,
    Trip,
    TripId
} from "../../../generated/public-transport";
import {CdkDrag, CdkDragDrop, CdkDragEnter, CdkDragExit, moveItemInArray} from "@angular/cdk/drag-drop";
import {BrigadeModel} from "./brigade-editor.model";
import moment, {Moment} from "moment";
import {first, last} from "lodash";
import {animate, state, style, transition, trigger} from "@angular/animations";
import {ActivatedRoute} from "@angular/router";
import {BrigadeEditorComponentMode} from "./brigade-editor-component-mode";

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
    ]
})
export class BrigadeEditorComponent implements OnInit {

    private componentMode: BrigadeEditorComponentMode = null;

    public tripsResponse: GetAllTripsResponse = {};

    public queryBrigadeName: string = '';
    public brigadeName = '';
    public brigadeItems: BrigadeModel[] = [];
    public isEntered: boolean = false;

    constructor(private brigadeService: BrigadeService, private _route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.brigadeService.getRoutes('').subscribe((response: GetAllTripsResponse) => this.tripsResponse = response);

        this._route.queryParams.subscribe(params => this.brigadeName = params['name']);
        this._route.queryParams.subscribe(params => this.queryBrigadeName = params['name']);

        this._route.data.subscribe(data => this.componentMode = data['mode']);
        this._route.data.subscribe(data => {
            this.brigadeItems = data['brigade'].trips.map((trip: BrigadeTrip) => {
                const brigadeModel: BrigadeModel = {} as BrigadeModel;
                brigadeModel.line = trip.tripId.line;
                brigadeModel.name = trip.tripId.name;
                brigadeModel.mode = trip.tripId.mode;

                brigadeModel.origin = trip.origin;
                brigadeModel.destination = trip.destination;

                // brigadeModel.travelTimeInSeconds;
                brigadeModel.departureTime = moment().startOf('day').add(trip.departureTime, 'seconds').format('HH:mm');
                brigadeModel.travelTimeInSeconds = trip.travelTimeInSeconds;

                return brigadeModel;
            })
        });
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

    getDifferenceBetweenBrigades(previousBrigadeModel: BrigadeModel, currentBrigadeModel: BrigadeModel): number {
        const departureTime = moment(previousBrigadeModel.departureTime, "HH:mm").add(previousBrigadeModel.travelTimeInSeconds, 'seconds');
        const arrivalTime = moment(currentBrigadeModel.departureTime, "HH:mm");

        return arrivalTime.diff(departureTime, 'minutes');
    }

    getDiff(currentIndex: number, currnetBrigadeModel: BrigadeModel): number {
        if (currentIndex === 0) {
            return 0;
        }
        const previousIndex = currentIndex - 1;
        const previousBrigadeModel: BrigadeModel = this.brigadeItems[previousIndex];

        return this.getDifferenceBetweenBrigades(previousBrigadeModel, currnetBrigadeModel);
    }

    remove(brigadeIndex: number): void {
        this.brigadeItems.splice(brigadeIndex, 1);
    }

    getFirstBrigade(): BrigadeModel {
        return first(this.brigadeItems);
    }

    getLastBrigade(): BrigadeModel {
        return last(this.brigadeItems);
    }

    getLastArrivalBrigade(): Moment {
        const lastBrigadeTrip = this.getLastBrigade();
        if (lastBrigadeTrip == null) {
            return null;
        }
        return moment(lastBrigadeTrip.departureTime, "HH:mm").add(lastBrigadeTrip.travelTimeInSeconds, 'seconds').startOf('minute').add(1, 'minute');
    }

    getDifferenceBetweenFirstAndLastTrip(): number {
        if (this.getFirstBrigade() == null) {
            return null;
        }

        const lastTrip: BrigadeModel = this.getLastBrigade();

        const departureTime = moment(this.getFirstBrigade().departureTime, "HH:mm");
        const arrivalTime = moment(lastTrip.departureTime, "HH:mm").add(lastTrip.travelTimeInSeconds, 'seconds');

        return arrivalTime.diff(departureTime, 'seconds');
    }

    isBrigadesEmpty(): boolean {
        return this.brigadeItems.length === 0;
    }

    saveOrEditBrigade(): void {
        let brigadePayload: BrigadePayload = {};
        brigadePayload.brigadeName = this.queryBrigadeName;

        let brigadeBody: BrigadeBody = {};
        brigadeBody.brigadeName = this.brigadeName;

        brigadeBody.trips = this.brigadeItems.map(brigadeBody => {
            let tripId: TripId = {};
            tripId.line = brigadeBody.line;
            tripId.name = brigadeBody.name;
            tripId.mode = brigadeBody.mode;
            tripId.variant = brigadeBody.variant;

            let brigadeTrip: BrigadeTrip = {};
            brigadeTrip.tripId = tripId;
            brigadeTrip.origin = brigadeBody.origin;
            brigadeTrip.destination = brigadeBody.destination;

            const midnight = moment().startOf('day');
            brigadeTrip.departureTime = moment(brigadeBody.departureTime, "HH:mm").diff(midnight, 'seconds');
            brigadeTrip.arrivalTime = moment(brigadeBody.departureTime, "HH:mm").add(brigadeBody.travelTimeInSeconds, 'seconds').diff(midnight, 'seconds');
            brigadeTrip.travelTimeInSeconds = brigadeBody.travelTimeInSeconds;

            return brigadeTrip;
        });

        if (this.componentMode === BrigadeEditorComponentMode.CREATE) {
            this.brigadeService.saveBrigade(brigadeBody).subscribe(response => {
            });
        }

        if (this.componentMode === BrigadeEditorComponentMode.EDIT) {
            let brigadePatchBody: BrigadePatchBody = {};
            brigadePatchBody.brigadePayload = brigadePayload;
            brigadePatchBody.brigadeBody = brigadeBody;

            this.brigadeService.putBrigade(brigadePatchBody).subscribe(response => {
            });
        }

    }

}
