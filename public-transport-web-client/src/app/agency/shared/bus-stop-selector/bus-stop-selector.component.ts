import {
    Component,
    inject,
    Input, OnInit
} from '@angular/core';
import {Stop, StopsResponse, StopsService} from "../../../generated/public-transport-api";
import {faMap, faBus, IconDefinition} from "@fortawesome/free-solid-svg-icons";
import {MatDialog} from "@angular/material/dialog";
import {BusStopModalSelectorComponent} from "../bus-stop-modal-selector/bus-stop-modal-selector.component";
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged, map, Observable, of, startWith} from "rxjs";
import {switchMap} from "rxjs/operators";
import {DomEvent} from "leaflet";
import stopPropagation = DomEvent.stopPropagation;

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
export class BusStopSelectorComponent implements OnInit {
    readonly dialog: MatDialog = inject(MatDialog);

    readonly faMap: IconDefinition = faMap;
    readonly faBus: IconDefinition = faBus;

    @Input() stopControl: FormGroup;
    @Input() placeholderLabel: string = '';

    get stopIdControl(): FormControl {
        return this.stopControl.get('id') as FormControl;
    }

    searchStopControl: FormControl<string> = new FormControl<string>('');
    filteredStops: Stop[];

    constructor(private stopsService: StopsService, private formBuilder: FormBuilder) {
        this.searchStopControl.valueChanges.pipe(
            startWith(''),
            debounceTime(300),
            distinctUntilChanged(),
            switchMap((value: string): Observable<Stop[]> => this.searchStops(value))
        ).subscribe((filteredStops: Stop[]) => this.filteredStops = filteredStops);
    }

    ngOnInit(): void {
    }

    searchStops(filter: string): Observable<Stop[]> {
        if (filter.length >= 3) {
            return this.stopsService.findStopsByStopName(filter)
                .pipe(map((stopResponse: StopsResponse): Stop[] => stopResponse.stops));
        } else {
            return of(this.filteredStops);
        }
    }

    openDialog($event: MouseEvent): void {
        stopPropagation($event);

        const busStopSelectedData: BusStopSelectorData = {} as BusStopSelectorData;
        busStopSelectedData.stopId = this.stopControl.get('id')?.value;
        busStopSelectedData.stopName = this.stopControl.get('name')?.value;
        busStopSelectedData.stopLon = this.stopControl.get('lon')?.value;
        busStopSelectedData.stopLat = this.stopControl.get('lat')?.value;

        const dialogRef = this.dialog.open(BusStopModalSelectorComponent, {
            width: '90%',
            height: '70%',
            data: busStopSelectedData,
        });

        dialogRef.afterClosed().subscribe((busStopSelectorData: BusStopSelectorData | undefined) => {
            if (busStopSelectorData !== undefined) {
                const stop: Stop = {};
                stop.id = busStopSelectorData.stopId;
                stop.name = busStopSelectorData.stopName;
                stop.lon = busStopSelectorData.stopLon;
                stop.lat = busStopSelectorData.stopLat;

                this.filteredStops = [stop];

                this.stopControl.get('id').setValue(busStopSelectorData.stopId);
                this.stopControl.get('name').setValue(busStopSelectorData.stopName);
                this.stopControl.get('lon').setValue(busStopSelectorData.stopLon);
                this.stopControl.get('lat').setValue(busStopSelectorData.stopLat);
            }
        });
    }

    public onClickStop(busStop: Stop): void {
        this.filteredStops = [busStop];

        this.stopControl.get('id').setValue(busStop.id);
        this.stopControl.get('name').setValue(busStop.name);
        this.stopControl.get('lon').setValue(busStop.lon);
        this.stopControl.get('lat').setValue(busStop.lat);
    }

    public stopPropagation($event: MouseEvent): void {
        $event.stopPropagation();
    }

    compareBusStops = (a: Stop, b: Stop) => a && b ? a.id === b.id : a === b;

}
