import {ChangeDetectionStrategy, Component, Input} from "@angular/core";
import {FormGroup} from "@angular/forms";
import {CdkDragDrop} from "@angular/cdk/drag-drop";
import {TripStopListStore} from "./trip-stop-list.store";

@Component({
    selector: "app-trip-stop-list",
    templateUrl: "./trip-stop-list.component.html",
    styleUrl: "./trip-stop-list.component.scss",
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
export class TripStopListComponent {
    @Input({required: true}) profile!: FormGroup;
    @Input() showStopCountError = false;

    constructor(readonly store: TripStopListStore) {
    }

    get isRefreshingExpanded(): boolean {
        return this.store.isRefreshingExpanded;
    }

    get isRefreshExpanded(): boolean {
        return this.store.isRefreshExpanded;
    }

    drop(event: CdkDragDrop<string[]>): void {
        this.store.drop(event);
    }

    remove(index: number): void {
        this.store.remove(index);
    }

    openDialogEditStop(stopTime: FormGroup): void {
        this.store.openDialogEditStop(stopTime);
    }

    refreshMap(): void {
        this.store.refreshMap();
    }

    refreshDistance(): void {
        this.store.measureDistance().subscribe();
    }

    isCustomized(profile: FormGroup): boolean {
        return this.store.isCustomized(profile);
    }

    getStops(profile: FormGroup) {
        return this.store.getStops(profile);
    }
}
