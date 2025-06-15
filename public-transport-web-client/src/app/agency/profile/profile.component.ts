import {Component, OnInit} from '@angular/core';
import {ProfileService} from "./profile.service";
import {AgencyDetails} from "../../generated/public-transport";
import {faGlobe, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";

@Component({
    standalone: true,
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrl: './profile.component.scss',
    imports: [
        CommonModule,
        FormsModule,
        FontAwesomeModule
    ],
    providers: [
        ProfileService
    ]
})
export class ProfileComponent implements OnInit {

    public faGlobe: IconDefinition = faGlobe;

    public agencyDetails: AgencyDetails = {};

    constructor(private profileService: ProfileService) {
    }

    ngOnInit(): void {
        this.profileService.getAgencyDetails().subscribe((response: AgencyDetails) => {
            this.agencyDetails = response;
        })
    }

    public saveAgencyDetails() {
        this.profileService.saveAgencyDetails(this.agencyDetails).subscribe(() => {});
    }

}
