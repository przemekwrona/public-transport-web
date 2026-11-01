import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {Territory} from "../../../generated/public-transport-api";
import {ReactiveFormsModule} from "@angular/forms";


@Component({
    selector: 'app-territory-details',
    imports: [
    ReactiveFormsModule
],
    templateUrl: './territory-details.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './territory-details.component.scss'
})
export class TerritoryDetailsComponent {

    @Input() territory: Territory = {} as Territory;

}
