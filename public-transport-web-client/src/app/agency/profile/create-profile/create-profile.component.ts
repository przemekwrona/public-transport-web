import {Component} from '@angular/core';
import {RouterModule} from "@angular/router";
import {CommonModule} from "@angular/common";
import {AgencyAdminCreateAccountRequest, AgencyService} from "../../../generated/public-transport";
import {FormsModule} from "@angular/forms";

@Component({
    selector: 'app-create-profile',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        FormsModule
    ],
    providers: [],
    templateUrl: './create-profile.component.html',
    styleUrl: './create-profile.component.scss'
})
export class CreateProfileComponent {

    public agency: AgencyAdminCreateAccountRequest = {} as AgencyAdminCreateAccountRequest;

    public repeatedPassword: string = '';

    constructor(private agencyService: AgencyService) {
    }

    public createProfile(): void {
        this.agencyService.createNewAccount(this.agency).subscribe(response => {
        });
    }

}
