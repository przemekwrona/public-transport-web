import {Component, OnInit} from '@angular/core';
import {BrigadeService} from "../brigade.service";
import {GetAllTripsResponse, Trip} from "../../../generated/public-transport";
import {
    CdkDrag,
    CdkDragDrop,
    CdkDragEnter, CdkDragExit,
    copyArrayItem,
    moveItemInArray,
    transferArrayItem
} from "@angular/cdk/drag-drop";
import {BrigadeModel} from "./brigade-editor.model";
import moment from "moment";
import {last} from "lodash";
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

    public trips: GetAllTripsResponse = {};

    public brigadeItems: BrigadeModel[] = [];
    public isEntered: boolean = false;

    items = [];

    constructor(private brigadeService: BrigadeService) {
    }

    ngOnInit(): void {
        this.brigadeService.getRoutes('').subscribe((response: GetAllTripsResponse) => this.trips = response);
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
            brigadeModel.origin = previousBrigadeModel.origin
            brigadeModel.destination = previousBrigadeModel.destination
            brigadeModel.isMainVariant = previousBrigadeModel.isMainVariant
            brigadeModel.variant = previousBrigadeModel.variant
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

    getDiff(currentIndex: number, currnetBrigadeModel: BrigadeModel): number {
        if(currentIndex === 0) {
            return 0;
        }
        const previousIndex = currentIndex - 1;
        const previousBrigadeModel: BrigadeModel = this.brigadeItems[previousIndex];

        const arrivalTime = moment(previousBrigadeModel.departureTime, "HH:mm").add(previousBrigadeModel.travelTimeInSeconds, 'seconds');
        const departureTime = moment(currnetBrigadeModel.departureTime, "HH:mm");

        return  departureTime.diff(arrivalTime, 'minutes');
    }

    remove(brigadeIndex: number): void {
        this.brigadeItems.splice(brigadeIndex, 1);
    }

}
