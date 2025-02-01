import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {StopService} from "../stops/stop.service";
import {Route, Routes, Stop} from "../../generated/public-transport";
import {ActivatedRoute, Router} from "@angular/router";
import {RouterQueryParams} from "../trips/trips.resolver";

@Component({
    selector: 'app-routes',
    templateUrl: './routes.component.html',
    styleUrl: './routes.component.scss',
    changeDetection: ChangeDetectionStrategy.Default
})
export class RoutesComponent implements OnInit {
    public stops: Stop[] = [];

    public routes: Routes = {};

    constructor(private stopService: StopService, private router: Router, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.routes = this.route.snapshot.data['routes'];
    }

    public clickAddRoute(): void {
        this.router.navigate(['/agency/routes/create']);
    }

    public openRoute(route: Route) {
        const state = {name: route.name, line: route.line};
        console.log(state);
        // this._router.navigateByUrl('/agency/trips/create', {state: state});
        this.router.navigateByUrl('/agency/trips', {state: state});
    }

}
