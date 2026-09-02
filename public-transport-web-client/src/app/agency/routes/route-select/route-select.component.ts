import {Component, forwardRef, input, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
    ControlValueAccessor,
    FormControl,
    FormsModule,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule
} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatSelectSearchComponent} from 'ngx-mat-select-search';
import {debounceTime, distinctUntilChanged, startWith} from 'rxjs';
import {Route, RouteId, RouteService, Routes} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';
import {RouteNameNormPipe} from '../../timetable/create-timetable/route-id-normalization.pipe';

@Component({
    selector: 'app-route-select',
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatSelectModule,
        MatSelectSearchComponent,
        RouteNameNormPipe
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => RouteSelectComponent),
            multi: true
        }
    ],
    templateUrl: './route-select.component.html',
    styleUrl: './route-select.component.scss'
})
export class RouteSelectComponent implements OnInit, ControlValueAccessor {
    label = input<string>('Numer i nazwa linii');

    searchControl = new FormControl<string>('', {nonNullable: true});

    allRoutes: Route[] = [];
    filteredRoutes: Route[] = [];
    value: RouteId | null = null;
    isDisabled = false;

    _onModelChange: (value: RouteId | null) => void = () => {};
    onTouched: () => void = () => {};

    constructor(private agencyStorageService: AgencyStorageService, private routeService: RouteService) {
    }

    ngOnInit(): void {
        this.routeService.getRoutes(this.agencyStorageService.getInstance())
            .subscribe((response: Routes) => {
                this.allRoutes = response.items ?? [];
                this.applyFilter(this.searchControl.value);
            });

        this.searchControl.valueChanges.pipe(
            startWith(this.searchControl.value),
            debounceTime(300),
            distinctUntilChanged()
        ).subscribe((query: string) => this.applyFilter(query));
    }

    get selectedRoute(): Route | undefined {
        return this.allRoutes.find(route => this.compareByRouteId(route.routeId, this.value));
    }

    onSelectionChange(routeId: RouteId | null): void {
        this.value = routeId;
        this._onModelChange(routeId);
    }

    onDropdownClose(isOpen: boolean): void {
        if (!isOpen) {
            this.onTouched();
        }
    }

    writeValue(value: RouteId | null): void {
        this.value = value;
    }

    registerOnChange(fn: (value: RouteId | null) => void): void {
        this._onModelChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }

    compareByRouteId = (a: RouteId, b: RouteId): boolean =>
        a && b ? a.line === b.line && a.name === b.name && a.version === b.version : a === b;

    trackRoute(route: Route): string {
        return `${route.routeId?.line ?? ''}-${route.routeId?.name ?? ''}-${route.routeId?.version ?? ''}`;
    }

    private applyFilter(query: string): void {
        const normalizedQuery = (query ?? '').trim().toLowerCase();
        if (!normalizedQuery) {
            this.filteredRoutes = this.allRoutes;
            return;
        }

        this.filteredRoutes = this.allRoutes.filter(route => {
            const line = (route.routeId?.line ?? '').toLowerCase();
            const name = (route.routeId?.name ?? '').replaceAll('_', ' ').toLowerCase();
            return `${line} ${name}`.includes(normalizedQuery);
        });
    }
}
