import {Component, Input} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {
    LocationSearch,
    LocationSearchResponse,
    LocationService, Stop
} from "../../../generated/public-transport-api";
import {debounceTime, distinctUntilChanged, map, Observable, of, startWith} from "rxjs";
import {switchMap} from "rxjs/operators";
import {CommonModule} from "@angular/common";
import {MatSelectSearchComponent} from "ngx-mat-select-search";

@Component({
    selector: 'app-city-selector',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatSelectSearchComponent
    ],
    templateUrl: './city-selector.component.html',
    styleUrl: './city-selector.component.scss'
})
export class CitySelectorComponent {

    @Input() cityControl: FormGroup;
    @Input() placeholderLabel: string = '';

    searchCityControl: FormControl<string> = new FormControl<string>('');
    filteredCities: LocationSearch[];

    get cityIdControl(): FormControl {
        return this.cityControl.get('id') as FormControl;
    }

    constructor(private locationService: LocationService) {
        this.searchCityControl.valueChanges.pipe(
            startWith(''),
            debounceTime(300),
            distinctUntilChanged(),
            switchMap((value: string): Observable<LocationSearch[]> => this.searchCities(value))
        ).subscribe((filteredCities: LocationSearch[]) => this.filteredCities = filteredCities);
    }

    public searchCities(filter: string): Observable<LocationSearch[]> {
        if (filter.length >= 2) {
            return this.locationService.findLocationByName(filter)
                .pipe(map((locationResponse: LocationSearchResponse): LocationSearch[] => locationResponse.locations));
        } else {
            return of(this.filteredCities);
        }
    }

    public onClickCity(location: LocationSearch): void {
        this.filteredCities = [location];

        this.cityControl.get('id').setValue(location.territorial_unit_id);
        this.cityControl.get('name').setValue(location.name);
    }

    public lower(v: string): string {
        return v.toLowerCase();
    }

    compareCities = (a: LocationSearch, b: LocationSearch) => a && b ? a.territorial_unit_id === b.territorial_unit_id : a === b;

}
