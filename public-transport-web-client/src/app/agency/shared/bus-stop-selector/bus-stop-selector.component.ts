import {
    Component,
    EventEmitter,
    inject,
    Input,
    Output,
    Renderer2,
} from '@angular/core';
import {Stop, StopsResponse, StopsService} from "../../../generated/public-transport-api";
import {faMap, faBus} from "@fortawesome/free-solid-svg-icons";
import {MatDialog} from "@angular/material/dialog";
import {BusStopModalSelectorComponent} from "../bus-stop-modal-selector/bus-stop-modal-selector.component";

export interface BusStopSelectorData {
    stopId: number;
    stopName: string;
    stopLon: number;
    stopLat: number;
}

@Component({
    selector: 'app-bus-stop-selector',
    templateUrl: './bus-stop-selector.component.html',
    styleUrl: './bus-stop-selector.component.scss'
})
export class BusStopSelectorComponent {
    readonly dialog = inject(MatDialog);

    readonly faMap = faMap;
    readonly faBus = faBus;

    /**
     * Holds the current value of the slider
     */
    @Input() busStopId: BusStopSelectorData = {} as BusStopSelectorData;

    /**
     * Invoked when the model has been changed
     */
    @Output() busStopIdChange: EventEmitter<BusStopSelectorData> = new EventEmitter<BusStopSelectorData>();

    busStops: Stop[] = [];
    showOptions = false;

    constructor(private renderer: Renderer2, private stopsService: StopsService) {
    }

    onSelect(option: Stop) {
        this.showOptions = false;

        const busStopSelectorData: BusStopSelectorData = {} as BusStopSelectorData;
        busStopSelectorData.stopId = option.id;
        busStopSelectorData.stopName = option.name;
        busStopSelectorData.stopLon = option.lon;
        busStopSelectorData.stopLat = option.lat;
        this.busStopIdChange.emit(busStopSelectorData);
    }

    onWrite(): void {
        if (this.busStopId.stopName.length >= 3) {
            this.stopsService.findStopsByStopName(this.busStopId.stopName).subscribe((response: StopsResponse) => {
                this.busStops = response.stops
                this.showOptions = true;
            });
        }
    }

    openDialog(): void {
        const dialogRef = this.dialog.open(BusStopModalSelectorComponent, {
            width: '90%',
            height: '70%',
            data: this.busStopId,
        });

        dialogRef.afterClosed().subscribe((busStopSelectorData: BusStopSelectorData | undefined) => {
            if (busStopSelectorData !== undefined) {
                this.busStopIdChange.emit(busStopSelectorData);
            }
        });
    }

}
