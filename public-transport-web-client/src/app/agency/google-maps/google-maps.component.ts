import {Component} from '@angular/core';
import {GtfsService} from "../../generated/public-transport";
import moment from "moment";

@Component({
    selector: 'app-google-maps',
    templateUrl: './google-maps.component.html',
    styleUrl: './google-maps.component.scss'
})
export class GoogleMapsComponent {

    constructor(private gtfsService: GtfsService) {
    }

    public downloadGtfs() {
        this.gtfsService.gtfsDownloadGet().subscribe((blob: Blob) => {
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

}
