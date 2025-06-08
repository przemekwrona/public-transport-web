import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {StopsPatchRequest, StopsService} from "../../../generated/public-transport";

@Component({
    selector: 'app-bus-stop-editor',
    templateUrl: './bus-stop-editor.component.html',
    styleUrl: './bus-stop-editor.component.scss'
})
export class BusStopEditorComponent implements OnChanges {
    @Input() public stopId: number;
    @Input() public stopName: string;
    @Input() public lon: number;
    @Input() public lat: number;

    public active: boolean = true;

    constructor(private stopsService: StopsService) {
    }

    public saveStop(): void {
        const stopPatchRequest = {} as StopsPatchRequest;
        stopPatchRequest.id = this.stopId;
        stopPatchRequest.name = this.stopName;
        stopPatchRequest.active = this.active;

        this.stopsService.patchStop(stopPatchRequest).subscribe(() => {
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.active = true;
    }

}
