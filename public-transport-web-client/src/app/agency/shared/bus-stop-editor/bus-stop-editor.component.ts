import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ChangeDetectionStrategy} from '@angular/core';
import {AgencyStopNameRequest, AgencyStopsService, StopsPatchRequest, StopsService} from "../../../generated/public-transport-api";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

import {FormsModule} from "@angular/forms";

@Component({
    imports: [
    FormsModule
],
    providers: [],
    selector: 'app-bus-stop-editor',
    templateUrl: './bus-stop-editor.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './bus-stop-editor.component.scss'
})
export class BusStopEditorComponent implements OnChanges {
    @Input() public stopId: number;
    @Input() public stopName: string;
    @Input() public lon: number;
    @Input() public lat: number;
    @Input() public isBdot: boolean;
    @Input() public isOsm: boolean;

    @Output() public onSaved = new EventEmitter<StopsPatchRequest>();

    public active: boolean = true;

    constructor(private stopsService: StopsService,
                private agencyStopsService: AgencyStopsService,
                private agencyStorageService: AgencyStorageService) {
    }

    public saveStop(): void {
        const stopPatchRequest = {
            id: this.stopId,
            name: this.stopName,
            active: this.active
        } as StopsPatchRequest;
        const request = {name: this.stopName} as AgencyStopNameRequest;

        this.agencyStopsService.updateAgencyStopName(this.agencyStorageService.getInstance(), this.stopId, request).subscribe(() => {
            this.stopsService.patchStop({id: this.stopId, active: this.active}).subscribe(() => {
                this.onSaved.emit(stopPatchRequest);
            });
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.active = true;
    }

    protected readonly name = name;
}
