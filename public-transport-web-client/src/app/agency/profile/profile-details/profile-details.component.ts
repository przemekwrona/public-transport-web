import {Component, OnInit} from '@angular/core';
import {AgencyDetails, AgencyService} from "../../../generated/public-transport-api";
import {faGlobe, IconDefinition} from '@fortawesome/free-solid-svg-icons';
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {TranslocoPipe} from "@jsverse/transloco";
import {ActivatedRoute} from "@angular/router";
import {LoginService} from "../../../auth/login.service";

@Component({
    standalone: true,
    selector: 'app-profile',
    templateUrl: './profile-details.component.html',
    styleUrl: './profile-details.component.scss',
    imports: [
        CommonModule,
        FormsModule,
        FontAwesomeModule,
        TranslocoPipe
    ],
    providers: [
        AgencyService
    ]
})
export class ProfileDetailsComponent implements OnInit {

    public faGlobe: IconDefinition = faGlobe;

    public agencyDetails: AgencyDetails = {};

    constructor(private agencyService: AgencyService, private route: ActivatedRoute, private authService: LoginService) {
    }

    ngOnInit(): void {
        this.route.data.subscribe(data => {
            this.agencyDetails = data['agencyDetails'];
        });
    }

    public saveAgencyDetails() {
        this.agencyService.updateAgency(this.authService.getInstance(), this.agencyDetails).subscribe(() => {});
    }

}
