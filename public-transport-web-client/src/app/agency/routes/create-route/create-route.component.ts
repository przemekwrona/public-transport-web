import {Component, ElementRef, Renderer2, ViewChild} from '@angular/core';
import {RoutesService} from "../routes.service";
import {Route, Status} from "../../../generated/public-transport";
import {Router} from "@angular/router";

@Component({
    selector: 'app-create-route',
    templateUrl: './create-route.component.html',
    styleUrl: './create-route.component.scss'
})
export class CreateRouteComponent {

    public route: Route = {
        google: false,
        active: true
    };

    constructor(private routeService: RoutesService, private _router: Router) {
    }

    public createRoute(): void {
        this.routeService.createRoute(this.route).subscribe((status: Status) => {
            this._router.navigate(['/agency/trips/create'], {queryParams: {line: this.route.line, name: this.route.name}}).then();
        });
    }
}
