import {Component, Input, OnInit} from '@angular/core';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {FormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";
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
    templateUrl: './trip-variant-select.component.html',
    styleUrl: './trip-variant-select.component.scss'
})
export class TripVariantSelectComponent implements OnInit {

    selectedTripControl = new FormControl('');
    searchControl = new FormControl('');

    // Observable for filtered results
    filteredRouteDetails$!: Observable<RouteDetails[]>;

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
            switchMap(value => this.tripService.getTripsByLineOrName(instance, value || '')),
            map(response => response.lines)
        );
    }

}
