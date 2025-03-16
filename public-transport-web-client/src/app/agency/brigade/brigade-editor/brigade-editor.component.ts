import {Component, OnInit} from '@angular/core';
import {BrigadeService} from "../brigade.service";
import {
    BrigadeBody,
    BrigadeTrip,
    GetAllTripsResponse,
    Trip,
    TripId
} from "../../../generated/public-transport";
import {
    CdkDrag,
    CdkDragDrop,
    CdkDragEnter, CdkDragExit,
    copyArrayItem,
    moveItemInArray,
    transferArrayItem
} from "@angular/cdk/drag-drop";
import {BrigadeModel} from "./brigade-editor.model";
import moment, {Moment} from "moment";
import {first, last} from "lodash";
import {animate, state, style, transition, trigger} from "@angular/animations";

@Component({
    selector: 'app-brigade-editor',
    templateUrl: './brigade-editor.component.html',
    styleUrl: './brigade-editor.component.scss',
    animations: [
        trigger('fadeBrigadeAnimation', [
            state('in', style({opacity: 1})),
            transition(':enter', [style({opacity: 0}), animate(500)]),
            transition(':leave', animate(500, style({opacity: 0})))
        ])
    ]
})
export class BrigadeEditorComponent implements OnInit {

    public tripsResponse: GetAllTripsResponse = {};

    public brigadeItems: BrigadeModel[] = [];
    public isEntered: boolean = false;

    public items = [];

    constructor(private brigadeService: BrigadeService) {
    }

    ngOnInit(): void {
        this.brigadeService.getRoutes('').subscribe((response: GetAllTripsResponse) => this.tripsResponse = response);
    }

    drop(event: CdkDragDrop<Trip[]>) {
        if (event.previousContainer === event.container) {
            moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
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

            this.brigadeItems.splice(event.currentIndex, 0, brigadeModel);
            copyArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex,);
        }
    }

    getArrivalTime(trip: BrigadeModel): moment.Moment {
        return moment(trip.departureTime, 'HH:mm').add(trip.travelTimeInSeconds, 'seconds');
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
        return moment(lastBrigadeTrip.departureTime, "HH:mm").add(lastBrigadeTrip.travelTimeInSeconds, 'seconds');
    }

    getDifferenceBetweenFirstAndLastTrip(): number {
        if (this.getFirstBrigade() == null) {
            return null;
        }

        const lastTrip: BrigadeModel = this.getLastBrigade();

        const departureTime = moment(this.getFirstBrigade().departureTime, "HH:mm");
        const arrivalTime = moment(lastTrip.departureTime, "HH:mm").add(lastTrip.travelTimeInSeconds, 'seconds');

        return arrivalTime.diff(departureTime, 'minutes');
    }

    saveBrigade(): void {
        let brigadeBody: BrigadeBody = {};
        brigadeBody.brigadeNumber = '';

        brigadeBody.trips = this.brigadeItems.map(brigadeBody => {
            let tripId: TripId = {};
            tripId.line = brigadeBody.line;
            tripId.name = brigadeBody.name;
            tripId.mode = brigadeBody.mode;
            tripId.variant = brigadeBody.variant;

            let brigadeTrip: BrigadeTrip = {};
            brigadeTrip.tripId = tripId;
            brigadeTrip.departureTime = brigadeBody.departureTime;
            brigadeTrip.arrivalTime = moment(brigadeBody.departureTime, "HH:mm").add(brigadeBody.travelTimeInSeconds, 'seconds').format('HH:mm');

            return brigadeTrip;
        });

        this.brigadeService.saveBrigade(brigadeBody).subscribe(response => {
        });
    }

}
