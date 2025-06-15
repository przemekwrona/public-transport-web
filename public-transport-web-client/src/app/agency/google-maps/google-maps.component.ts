import {Component} from '@angular/core';
import {GoogleAgreementsRequest, GoogleAgreementsService, GtfsService} from "../../generated/public-transport";
import moment from "moment";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";

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
        GoogleAgreementsService
    ]
})
export class GoogleMapsComponent {

    public googleAgreementsRequest: GoogleAgreementsRequest = {};

    constructor(private gtfsService: GtfsService, private googleAgreementsService: GoogleAgreementsService) {
    }

    public downloadGtfs() {
        this.gtfsService.downloadGtfs().subscribe((blob: Blob) => {
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
        this.googleAgreementsService.updateGoogleAgreements(this.googleAgreementsRequest).subscribe(() => {
        });
    }

}
