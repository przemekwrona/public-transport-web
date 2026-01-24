import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {Route, RouteId, Routes, RouteService, Status, Stop} from "../../../generated/public-transport-api";
import {ActivatedRoute, Router} from "@angular/router";
import {CommonModule} from "@angular/common";
import {PdfService} from "../../../generated/public-transport-pdf-api";
import {LoginService} from "../../../auth/login.service";
import {NotificationService} from "../../../shared/notification.service";
import {size} from "lodash";
import {GoogleAnalyticsService} from "../../../google-analytics.service";

@Component({
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

    constructor(private pdfService: PdfService, private routeService: RouteService, private authService: LoginService, private notificationService: NotificationService, private router: Router, private route: ActivatedRoute, private googleAnalyticsService: GoogleAnalyticsService) {
    }

    ngOnInit(): void {
        this.routes = this.route.snapshot.data['routes'];
        this.googleAnalyticsService.pageView(this.router.url);
    }

    public clickAddRoute(): void {
        this.router.navigate(['/agency/routes/create']).then();
    }

    public openRoute(route: Route) {
        const state = {line: route.routeId.line, name: route.routeId.name, version: route.routeId.version};
        this.router.navigate(['/agency/trips'], {queryParams: state}).then();
    }

    public downloadPdf(route: Route): void {
        this.pdfService.downloadTripPdf(route.routeId.line, route.routeId.name).subscribe((response: Blob) => {
            const link = document.createElement('a');
            link.href = URL.createObjectURL(response);
            link.download = `linia-${route.routeId.line}-${route.routeId.name}-plan.pdf`.replaceAll(' ', '_');
            link.click();
        });
    }

    public deleteRoute(route: Route): void {
        const agency = this.authService.getInstance();
        const routeId: RouteId = {} as RouteId;
        routeId.line = route.routeId.line;
        routeId.name = route.routeId.name;
        routeId.version = route.routeId.version;

        this.routeService.deleteRoute(agency, routeId).subscribe(response => {
            if (response.status.status === Status.StatusEnum.Deleted) {
                this.notificationService.showSuccess(`Linia ${routeId.line} (${routeId.name}) została usunięta`);
                this.routeService.getRoutes(agency).subscribe(response => this.routes = response);
            }
        })
    }

    public hasRoutes(): boolean {
        return size(this.routes.items) !== 0;
    }

    public isEmpty(value: string | null): boolean {
        return [null, undefined, ''].includes(value)
    }

}
