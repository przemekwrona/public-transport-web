import {Component, Input} from '@angular/core';
import {Territory} from "../../../generated/public-transport-api";
import {ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-territory-details',
    imports: [
        CommonModule,
        ReactiveFormsModule
    ],
    templateUrl: './territory-details.component.html',
    styleUrl: './territory-details.component.scss'
})
export class TerritoryDetailsComponent {

    @Input() territory: Territory = {} as Territory;

}
