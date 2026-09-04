import {Component, forwardRef, input, output, OnInit} from '@angular/core';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {ControlValueAccessor, FormControl, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule} from "@angular/forms";
import {MatInputModule} from "@angular/material/input";
import {GetAllTripsResponse, RouteDetails, TripService} from "../../../../generated/public-transport-api";
import {CommonModule} from "@angular/common";
import {debounceTime, distinctUntilChanged, map, Observable, startWith} from "rxjs";
import {AgencyStorageService} from "../../../../auth/agency-storage.service";
import {switchMap} from "rxjs/operators";
import {FormatSecondsPipe} from "./format-seconds.pipe";

@Component({
    selector: 'app-trip-variant-select',
    imports: [
        CommonModule,
        FormatSecondsPipe,
        MatFormFieldModule,
        MatSelectModule, FormsModule, ReactiveFormsModule, MatInputModule
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TripVariantSelectComponent),
            multi: true
        }
    ],
    templateUrl: './trip-variant-select.component.html',
    styleUrl: './trip-variant-select.component.scss'
})
export class TripVariantSelectComponent implements OnInit, ControlValueAccessor {
    // Keep the label as a modern signal
    label = input<string>('');

    // Add onChange as a modern output
    onChange = output<any>();

    // Local control for the search input
    searchControl = new FormControl('');

    // Observable for filtered results
    filteredRouteDetails$!: Observable<RouteDetails[]>;

    // Internal state
    value: any = null;
    isDisabled: boolean = false;

    // CVA Placeholder functions (renamed onChange to _onModelChange to avoid conflict)
    _onModelChange: any = () => {};
    onTouched: any = () => {};

    constructor(private agencyStorageService: AgencyStorageService, private tripService: TripService) {
    }

    ngOnInit(): void {
        const instance = this.agencyStorageService.getInstance();
        // Listen to search input changes and filter the list
        this.filteredRouteDetails$ = this.searchControl.valueChanges.pipe(
            startWith(''),
            // Wait 300ms after each keystroke before considering the term
            debounceTime(300),
            // Ignore new term if it's the same as the previous term
            distinctUntilChanged(),
            // switchMap cancels the previous API request if a new one is made
            switchMap(value => this.tripService.getTripsByRouteAndFilterLineOrName(instance, value || '')),
            map(response => response.lines)
        );
    }

    onSelectionChange(val: any) {
        this.value = val;

        // Notify Angular forms of the value change
        this._onModelChange(val);

        // Emit the value to parent components listening to (onChange)
        this.onChange.emit(val);
    }

    onDropdownClose(isOpen: boolean) {
        if (!isOpen) {
            this.onTouched();
        }
    }

    writeValue(val: any): void {
        this.value = val;
    }

    registerOnChange(fn: any): void {
        this._onModelChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }
}
