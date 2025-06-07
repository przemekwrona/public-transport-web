import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Stop, StopsPatchRequest, StopsService} from "../../../generated/public-transport";

@Component({
    selector: 'app-bus-stop-editor',
    templateUrl: './bus-stop-editor.component.html',
    styleUrl: './bus-stop-editor.component.scss'
})
export class BusStopEditorComponent implements OnChanges {

    @Input() public stop: Stop;

    public active: boolean = true;

    constructor(private stopsService: StopsService) {
    }

    public saveStop(): void {
        const stopPatchRequest = {} as StopsPatchRequest;
        stopPatchRequest.id = this.stop.id;
        stopPatchRequest.name = this.stop.name;
        stopPatchRequest.active = this.active;

        this.stopsService.patchStop(stopPatchRequest).subscribe(() => {
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.active = true;
    }

}
