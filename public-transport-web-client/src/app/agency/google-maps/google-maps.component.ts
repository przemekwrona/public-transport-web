import {Component, OnInit} from '@angular/core';
import {
    GoogleAgreementsRequest,
    GoogleAgreementsResponse,
    GoogleAgreementsService,
    GtfsService
} from "../../generated/public-transport-api";
import moment from "moment";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ActivatedRoute} from "@angular/router";
import {LoginService} from "../../auth/login.service";
import {AgencyStorageService} from "../../auth/agency-storage.service";

@Component({
    standalone: true,
    selector: 'app-google-maps',
    templateUrl: './google-maps.component.html',
    styleUrl: './google-maps.component.scss',
    imports: [
        CommonModule,
        FormsModule
    ],
    providers: [
        GtfsService,
        GoogleAgreementsService,
        LoginService
    ]
})
export class GoogleMapsComponent implements OnInit {

    public googleAgreementsResponse: GoogleAgreementsResponse = {};

    constructor(private route: ActivatedRoute, private gtfsService: GtfsService, private googleAgreementsService: GoogleAgreementsService, private authService: LoginService, private agencyStorageService: AgencyStorageService,) {
    }

    ngOnInit(): void {
        this.googleAgreementsResponse = this.route.snapshot.data['googleAgreements'];
    }

    public downloadGtfs() {
        this.gtfsService.downloadGtfs(this.agencyStorageService.getInstance()).subscribe((blob: Blob) => {
            const a: any = document.createElement("a");
            document.body.appendChild(a);
            a.style = "display: none";

            const url = window.URL.createObjectURL(blob);

            const date = moment().format('yyyyMMDD')

            a.href = url;
            a.download = `${date}.gtfs.zip`;
            a.click();
            window.URL.revokeObjectURL(url);
        });
    }

    public updateGoogleAgreements(): void {
        const googleAgreementsRequest: GoogleAgreementsRequest = {} as GoogleAgreementsRequest;
        googleAgreementsRequest.accessibilityStatement = this.googleAgreementsResponse.accessibilityStatement;
        googleAgreementsRequest.repeatabilityStatement = this.googleAgreementsResponse.repeatabilityStatement;
        googleAgreementsRequest.ticketSalesStatement = this.googleAgreementsResponse.ticketSalesStatement;
        this.googleAgreementsService.updateGoogleAgreements(googleAgreementsRequest).subscribe(() => {
        });
    }

    public isAdmin(): boolean {
        return this.authService.hasRoleSuperAdmin();
    }

}
