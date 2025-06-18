import {Component} from '@angular/core';
import {RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-create-profile',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule
    ],
    templateUrl: './create-profile.component.html',
    styleUrl: './create-profile.component.scss'
})
export class CreateProfileComponent {

}
