import {Component, ElementRef, EventEmitter, Input, Output, Renderer2, ViewChild} from '@angular/core';
import {StopsResponse, StopsService} from "../../../generated/public-transport";
import {Stop} from "../../../generated";

@Component({
    selector: 'app-bus-stop-selector',
    templateUrl: './bus-stop-selector.component.html',
    styleUrl: './bus-stop-selector.component.scss'
})
export class BusStopSelectorComponent {

    /**
     * Holds the current value of the slider
     */
    @Input() busStopId: string = "";

    /**
     * Invoked when the model has been changed
     */
    @Output() busStopIdChange: EventEmitter<string> = new EventEmitter<string>();



    @ViewChild('toggleInput') toggleButton: ElementRef;
    @ViewChild('menu') menu: ElementRef;

    busStops: Stop[] = [];
    showOptions = false;
    stopNamePrefix = '';

    constructor(private renderer: Renderer2, private stopsService: StopsService) {
        /**
         * This events get called by all clicks on the page
         */
        this.renderer.listen('window', 'click', (e: Event) => {
            /**
             * Only run when toggleButton is not clicked
             * If we don't check this, all clicks (even on the toggle button) gets into this
             * section which in the result we might never see the menu open!
             * And the menu itself is checked here, and it's where we check just outside of
             * the menu and button the condition abbove must close the menu
             */
            if (e.target !== this.toggleButton.nativeElement && e.target !== this.menu.nativeElement) {
                this.showOptions = false;
            }
        });
    }

    onSelect(option: Stop) {
        this.stopNamePrefix = option.name;
        this.showOptions = false;
        this.busStopIdChange.emit(option.id);
    }

    onWrite(stopNamePrefix: string): void {
        if (this.stopNamePrefix.length > 2) {
            this.stopsService.findStopsByStopName(stopNamePrefix).subscribe((response: StopsResponse) => {
                this.busStops = response.stops
            });
        }
    }

}
