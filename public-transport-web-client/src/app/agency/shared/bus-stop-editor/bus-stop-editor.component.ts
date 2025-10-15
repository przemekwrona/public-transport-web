import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {StopsPatchRequest, StopsService} from "../../../generated/public-transport-api";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
    standalone: true,
    imports: [
        CommonModule,
        FormsModule
    ],
    providers: [
    ],
    selector: 'app-bus-stop-editor',
    templateUrl: './bus-stop-editor.component.html',
    styleUrl: './bus-stop-editor.component.scss'
})
export class BusStopEditorComponent implements OnChanges {
    @Input() public stopId: number;
    @Input() public stopName: string;
    @Input() public lon: number;
    @Input() public lat: number;

    @Output() public onSaved = new EventEmitter<StopsPatchRequest>();

    public active: boolean = true;

    constructor(private stopsService: StopsService) {
    }

    public saveStop(): void {
        const stopPatchRequest = {} as StopsPatchRequest;
        stopPatchRequest.id = this.stopId;
        stopPatchRequest.name = this.stopName;
        stopPatchRequest.active = this.active;

        this.stopsService.patchStop(stopPatchRequest).subscribe(() => {
            this.onSaved.emit(stopPatchRequest);
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.active = true;
    }

}
