import {
    ChangeDetectionStrategy,
    Component,
    ElementRef,
    EventEmitter,
    inject,
    Input,
    Output,
    Renderer2,
    ViewChild
} from '@angular/core';
import {StopsResponse, StopsService} from "../../../generated/public-transport";
import {Stop} from "../../../generated";
import {faMap, faBus} from "@fortawesome/free-solid-svg-icons";
import {MatDialog} from "@angular/material/dialog";
import {BusStopModalSelectorComponent} from "../bus-stop-modal-selector/bus-stop-modal-selector.component";

export interface BusStopSelectorData {
    stopId: string;
    stopName: string;
    stopLon: number;
    stopLat: number;
}

@Component({
    selector: 'app-bus-stop-selector',
    templateUrl: './bus-stop-selector.component.html',
    styleUrl: './bus-stop-selector.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
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


    @ViewChild('toggleInput') toggleButton: ElementRef;
    @ViewChild('menu') menu: ElementRef;

    busStops: Stop[] = [];
    showOptions = false;
    stopNamePrefix = '';
    lastSearchCity: string = '';

    constructor(private renderer: Renderer2, private stopsService: StopsService) {
        /**
         * This events get called by all clicks on the page
         */
        // this.renderer.listen('window', 'click', (e: Event) => {
        /**
         * Only run when toggleButton is not clicked
         * If we don't check this, all clicks (even on the toggle button) gets into this
         * section which in the result we might never see the menu open!
         * And the menu itself is checked here, and it's where we check just outside of
         * the menu and button the condition abbove must close the menu
         */
        // if (e.target !== this.toggleButton.nativeElement && e.target !== this.menu.nativeElement) {
        //     this.showOptions = false;
        // }
        // });
    }

    onSelect(option: Stop) {
        this.stopNamePrefix = option.name;
        this.showOptions = false;

        const busStopSelectorData: BusStopSelectorData = {} as BusStopSelectorData;
        busStopSelectorData.stopId = option.id;
        busStopSelectorData.stopName = option.name;
        busStopSelectorData.stopLon = option.lon;
        busStopSelectorData.stopLat = option.lat;

        this.busStopIdChange.emit(busStopSelectorData);
    }

    onWrite(): void {
        if (this.stopNamePrefix.length > 2) {
            this.stopsService.findStopsByStopName(this.stopNamePrefix).subscribe((response: StopsResponse) => {
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
                this.stopNamePrefix = busStopSelectorData.stopName;
                this.busStopIdChange.emit(busStopSelectorData);
            }
        });
    }

}
