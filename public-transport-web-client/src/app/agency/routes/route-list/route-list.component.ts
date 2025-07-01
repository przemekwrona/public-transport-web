import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Route, Routes, Stop} from "../../../generated/public-transport";
import {ActivatedRoute, Router} from "@angular/router";
import {CommonModule} from "@angular/common";
import {PdfService} from "../../../generated/public-transport-pdf";
import {size} from "lodash";

@Component({
    standalone: true,
    imports: [
        CommonModule
    ],
    providers: [
        PdfService
    ],
    selector: 'app-routes',
    templateUrl: './route-list.component.html',
    styleUrl: './route-list.component.scss',
    changeDetection: ChangeDetectionStrategy.Default
})
export class RouteListComponent implements OnInit {
    public stops: Stop[] = [];

    public routes: Routes = {};

    constructor(private pdfService: PdfService, private _router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.routes = this.route.snapshot.data['routes'];
    }

    public clickAddRoute(): void {
        this._router.navigate(['/agency/routes/create']).then();
    }

    public openRoute(route: Route) {
        const state = {line: route.line, name: route.name};
        this._router.navigate(['/agency/trips'], {queryParams: state}).then();
    }

    public downloadPdf(route: Route): void {
        this.pdfService.downloadTripPdf(route.line, route.name).subscribe((response: Blob) => {
            console.log(response);
            const link = document.createElement('a');
            link.href = URL.createObjectURL(response);
            link.download = `linia-${route.line}-${route.name}-plan.pdf`.replaceAll(' ', '_');
            link.click();
        });
    }

    public hasRoutes(): boolean {
        return size(this.routes.items) !== 0;
    }

}
