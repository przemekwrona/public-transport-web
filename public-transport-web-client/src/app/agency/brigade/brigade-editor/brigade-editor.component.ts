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
import {faGlobe} from "@fortawesome/free-solid-svg-icons";

@Component({
  selector: 'app-brigade-editor',
  templateUrl: './brigade-editor.component.html',
  styleUrl: './brigade-editor.component.scss'
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
                brigadeModel.departure = moment().format('HH:mm');
            } else {
                const lastBrigadeItems = last(this.brigadeItems);
                brigadeModel.departure = this.getArrivalTime(lastBrigadeItems).format("HH:mm");
            }

            brigadeModel.travelTimeInSeconds = previousBrigadeModel.travelTimeInSeconds

            this.brigadeItems.push(brigadeModel);
            copyArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex,);
        }
    }

    getArrivalTime(trip: BrigadeModel): moment.Moment {
        return moment(trip.departure, 'HH:mm').add(trip.travelTimeInSeconds, 'seconds');
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

    protected readonly faGlobe = faGlobe;
}
